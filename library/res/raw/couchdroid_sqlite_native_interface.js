/**
 * Pretends to be the WebSQL interface while secretly siphoning requests off to the native Android SQL interface.
 * Sneaky!
 */

(function(){
    'use strict';

    function debug(str) {
        CouchDroid.Util.debug('SQLiteNativeDB', str);
    }

    var transactionIds = 0;
    var queryIds = 0;
    var callbackIds = 0;

    var SQLiteNativeDB = {
        callbacks : {},
        nativeDBs : {}
    };

    SQLiteNativeDB.clearCache = function() {

        // allows us to save memory by deleting callbacks in the hashmap
        // TODO: is it necessary to keep the "important" ones?
        Object.keys(SQLiteNativeDB.callbacks).forEach(function(key){

            if (key.indexOf('!cb_') !== 0) { //not important
                delete SQLiteNativeDB.callbacks[key];
            }
        });

        SQLiteNativeDB.nativeDBs = {};

    };

    SQLiteNativeDB.onNativeCallback = function(callbackId, argument) {
        debug('onNativeCallback(' + callbackId + ', ' + argument + ')');

        var callback = SQLiteNativeDB.callbacks[callbackId];
        if (!callback) {
            window.console.log('callback not found for id ' + callbackId + '! ' + callback);
        } else {
            callback.apply(null, argument ? [argument] : null);
        }
    };

    function createCallback(fn, important) {

        fn = fn || function(){};

        var callbackId = (important ? '!cb_' : 'cb_') + (callbackIds++);

        var newFn = function() {
            debug('executing callback with id: ' + callbackId);
            fn.apply(null, arguments);
        };

        SQLiteNativeDB.callbacks[callbackId] = newFn;

        return callbackId;
    }

    var SqliteQuery = function(sql, selectArgs) {
        var self = this;

        self.sql = sql;
        self.selectArgs = selectArgs;
        self.queryId = queryIds++;
    };

    var SqliteTransaction = function(callback, error, success, nativeDB) {
        var self = this;

        self.callback = callback;
        self.success = success;
        self.error = error;
        self.nativeDB = nativeDB;
        self.queriesIn = [];
        self.queriesStarted = [];
        self.queriesDone = [];
        self.sentEndAsFailure = false;
        self.transactionId = transactionIds++;
        debug('created new transaction with id ' + self.transactionId);
    };

    SqliteTransaction.prototype.debugQueryStatus = function() {
        var self = this;

        if (CouchDroid.DEBUG_MODE) {
            debug('transactionId ' + self.transactionId + ': (queriesIn: ' + self.queriesIn.length + ', queriesStarted: ' + self.queriesStarted.length +
                ', queriesDone: ' + self.queriesDone.length + ')');
        }
    };

    SqliteTransaction.prototype.wrapQuerySuccess = function(querySuccess, query) {
        var self = this;

        return function(transaction, payload) {
            debug('wrapQuerySuccess(), transactionId ' + self.transactionId);
            if (querySuccess && typeof querySuccess === 'function') {
                querySuccess(transaction, payload);
            }
            self.queriesDone.push(query);
            self.runNextQueryOrEnd(); // if new queries were pushed, process those
        };
    };

    SqliteTransaction.prototype.wrapQueryError = function(queryError, query) {
        var self = this;

        return function(sqlErrorObj) {
            debug('wrapQueryError(), transactionId ' + self.transactionId);

            /**
             * Per the W3C spec:
             *
             * In case of error (or more specifically, if the above substeps say to jump to the "in case of error"
             * steps), run the following substeps:
             * 1. If the statement had an associated error callback that is not null, then queue a task to invoke that
             *    error callback with the SQLTransaction object and a newly constructed SQLError object that represents
             *    the error that caused these substeps to be run as the two arguments, respectively, and wait for the
             *    task to be run.
             * 2. If the error callback returns false, then move on to the next statement, if any, or onto the next
             *    overall step otherwise.
             * 3. Otherwise, the error callback did not return false, or there was no error callback. Jump to the last
             *    step in the overall steps.
             */
            if (queryError && typeof queryError === 'function') {
                debug('running queryError');
                self.debugQueryStatus();
                var failedToCorrectError = queryError(self, sqlErrorObj);
                debug('ran queryError');
                self.debugQueryStatus();
                if (failedToCorrectError) {
                    debug('failed to correct error, entire transaction is in error');
                    self.markTransactionInError = true;
                } else {
                    debug('successfully corrected error, may proceed');
                }
            } else {
                debug('no fallback to correct error, entire transaction is in error');
                self.markTransactionInError = true;
            }



            self.queriesDone.push(query);
            self.runNextQueryOrEnd(); // if new queries were pushed, process those
        };
    };

    SqliteTransaction.prototype.runNextQueryOrEnd = function() {
        var self = this;
        debug('runNextQueryOrEnd(), transactionId ' + self.transactionId);

        if (self.markTransactionInError) {
            // ran into an error
            debug('ending this transaction unsuccessfully for id ' + self.transactionId);
            if (!self.sentEndAsFailure) {
                self.endAsFailure();
                self.sentEndAsFailure = true;
            }
        } else if (self.queriesIn.length) {
            // more queries remain
            debug('transactionId ' + self.transactionId + ': there are ' +
                self.queriesIn.length + '; popping one off the top...');

            var query = self.queriesIn.shift();
            self.queriesStarted.push(query);
            self.debugQueryStatus();
            self.nativeDB.executeSql(query, self);
        } else {
            // no more queries; end the transaction ?
            debug('transactionId ' + self.transactionId + ': no more queries; end the transaction, maybe?');
            self.debugQueryStatus();
            var allQueriesComplete = (self.queriesIn.length === 0 && self.queriesStarted.length > 0 &&
                self.queriesStarted.length === self.queriesDone.length);
            if (allQueriesComplete) {
                debug('ending this transaction successfully with id ' + self.transactionId);
                self.endAsSuccessful();
            }
        }
    };

    SqliteTransaction.prototype.endAsFailure = function() {
        var self = this;

        var endTransactionDoneId = createCallback(function(){
            debug('transactionId ' + self.transactionId + ': cleaning up after failure.');
            self.error();
            self.nativeDB.processNextTransaction();
        }, true);
        SQLiteJavascriptInterface.endTransaction(self.transactionId, self.nativeDB.name, endTransactionDoneId,
            endTransactionDoneId, false);
    };

    SqliteTransaction.prototype.endAsSuccessful = function() {
        var self = this;

        var endTransactionSuccessId = createCallback(function(){
            // mark the entire transaction as successful
            debug('executing transaction success for transactionId ' + self.transactionId);
            if (self.success && typeof self.success === 'function') {
                self.success();
            }
            self.nativeDB.processNextTransaction(); // todo: do we need this?
        }, true);
        var errorId = createCallback(function(){
            debug('executing transaction error for transactionId ' + self.transactionId);
            if (self.error && typeof self.error === 'function') {
                self.error();
            }
            self.nativeDB.processNextTransaction(); // todo: do we need this?
        }, true);
        SQLiteJavascriptInterface.endTransaction(self.transactionId, self.nativeDB.name, endTransactionSuccessId,
            errorId, true);
    };

    /**
     * Called by users of the transaction, not us.
     */
    SqliteTransaction.prototype.executeSql = function(sql, selectArgs, querySuccess, queryError) {
        var self = this;

        var query = new SqliteQuery(sql, selectArgs);
        query.querySuccess = self.wrapQuerySuccess(querySuccess, query);
        query.queryError = self.wrapQueryError(queryError, query);

        self.queriesIn.push(query);
        debug('transaction ' + self.transactionId + ' got a new query');
        self.debugQueryStatus();

        self.runNextQueryOrEnd();
    };

    // override the traditional websql database
    var NativeDB = function(name) {
        var self = this;

        self.name = name;
        self.transactions = [];
    };

    NativeDB.prototype.init = function(success) {
        var self = this;
        debug('init()');

        var callbackId = createCallback(function(){
            if (success && typeof success === 'function') {
                success();
            }
        }, true);

        SQLiteJavascriptInterface.open(self.name, callbackId);
    };

    NativeDB.prototype.transaction = function(fn, error, success) {
        var self = this;
        debug('transaction()');

        self.transactions.push(new SqliteTransaction(fn,  error, success, self));

        self.processNextTransaction();
    };

    NativeDB.prototype.processNextTransaction = function() {
        var self = this;
        debug('processTransaction()');

        if (self.transactions.length) {

            var transaction = self.transactions.shift();

            debug('processing transaction with id ' + transaction.transactionId);
            debug('remaining transactions are: ' + JSON.stringify(self.transactions.map(function(transaction){return transaction.transactionId;})));

            var transactionErrorId = createCallback(transaction.error, true);
            var startTransactionSuccessId = createCallback(function (){
                transaction.callback(transaction);
            }, true);
            SQLiteJavascriptInterface.startTransaction(transaction.transactionId, self.name, startTransactionSuccessId,
                transactionErrorId);
        }
    };

    NativeDB.prototype.executeSql = function(query, transaction) {
        var self = this;
        debug('executeSql()');


        var querySuccessId = createCallback(function(response){

            debug('query success!');

            // convert java response into websql-like response
            var rows = (response && response.rows) ? response.rows : [];
            var payload = {
                rows: {
                    item: function(i) {
                        return rows[i];
                    },
                    length: rows.length
                },
                rowsAffected: (response && response.rowsAffected) ? response.rowsAffected  : 0,
                insertId: (response && response.insertId) ? response.insertId : 0
            };

            debug('calling querySuccess function...');
            transaction.debugQueryStatus();

            query.querySuccess(transaction, payload);
            debug('querySuccess called.');
            transaction.debugQueryStatus();
        }, false);
        var queryErrorId = createCallback(query.queryError, false);

        var selectArgsAsJson = query.selectArgs ? JSON.stringify(query.selectArgs) : null;
        SQLiteJavascriptInterface.executeSql(query.queryId, transaction.transactionId,
            self.name, query.sql, selectArgsAsJson, querySuccessId, queryErrorId);
    };

    SQLiteNativeDB.openNativeDatabase = function(name, version, description, size, success) {
        var nativeDB = SQLiteNativeDB.nativeDBs[name];
        if (!nativeDB) {
            // doesn't exist yet
            nativeDB =  new NativeDB(name);
            nativeDB.init(success);

            SQLiteNativeDB.nativeDBs[name] = nativeDB;
        } else {
            setTimeout(function(){
                if (success && typeof success === 'function') {
                    success();
                }
            }, 0);

        }

        return nativeDB;
    };

    CouchDroid.SQLiteNativeDB = SQLiteNativeDB;
})();