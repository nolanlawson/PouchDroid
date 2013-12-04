/* export SQLiteNativeDB */

var SQLiteNativeDB;

(function(){
    'use strict';

    SQLiteNativeDB = {
        transactionIds : 0,
        callbacks : {},
        callbackIds : 0,
        nativeDBs : {}
    };

    function createCallback(fn) {
        fn = fn || function(){};

        var callbackId = 'callback_' + (SQLiteNativeDB.callbackIds++);

        var newFn = function() {
            debug('executing callback with id: ' + callbackId);
            fn.apply(null, arguments);
            // TODO: intelligently clean up callbacks

        };

        SQLiteNativeDB.callbacks[(callbackId)] = newFn;

        return callbackId;
    }

    function debug(str) {
        if (DEBUG_MODE && str) {
            window.console.log('SQLiteNativeDB: ' + str);
        }
    }

    var SqliteQuery = function(sql, selectArgs, querySuccess, queryError) {
        var self = this;

        self.sql = sql;
        self.selectArgs = selectArgs;
        self.querySuccess = querySuccess;
        self.queryError = queryError;
    };

    var SqliteTransaction = function(callback, error, success, nativeDB) {
        var self = this;

        self.callback = callback;
        self.success = self.runSuccessFunctionAndCleanup(success);
        self.error = self.runErrorFunctionAndCleanup(error);
        self.errorId = createCallback(self.error);
        self.nativeDB = nativeDB;
        self.queries = [];
        self.numQueriesStarted = 0;
        self.numQueriesEnded = 0;
        self.transactionId = SQLiteNativeDB.transactionIds++;
    };

    SqliteTransaction.prototype.runSuccessFunctionAndCleanup = function(fn) {
        var self = this;

        return function() {
            debug('transactionId ' + self.transactionId + ': runSuccessFunctionAndCleanup()');
            if (fn && typeof fn === 'function') {
                fn.apply(null, arguments);
            }
            self.nativeDB.transactionInProgress = false;
            self.nativeDB.processNextTransaction();
        };
    };

    SqliteTransaction.prototype.runErrorFunctionAndCleanup = function(fn) {
        var self = this;


        return function() {
            debug('transactionId ' + self.transactionId + ': runErrorFunctionAndCleanup()');

            if (fn && typeof fn === 'function') {
                fn.apply(null, arguments);
            }

            self.endAsFailure();
        };
    };

    SqliteTransaction.prototype.wrapQuerySuccess = function(querySuccess) {
        var self = this;

        return function(transaction, payload) {
            if (querySuccess && typeof querySuccess === 'function') {
                querySuccess(transaction, payload);
            }
            self.numQueriesEnded++;
            self.runNextQueryOrEnd(); // if new queries were pushed, process those
        };
    };

    SqliteTransaction.prototype.wrapQueryError = function(queryError) {
        var self = this;

        return function(sqlErrorObj) {

            debug('running queryError');

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
                var failedToCorrectError = queryError(self, sqlErrorObj);
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

            self.numQueriesEnded++;
            self.runNextQueryOrEnd(); // if new queries were pushed, process those
        };
    };

    SqliteTransaction.prototype.runNextQueryOrEnd = function() {
        var self = this;
        if (self.markTransactionInError) {
            // ran into an error
            debug('ending this transaction unsuccessfully for id ' + self.transactionId);
            self.endAsFailure();
        } else if (self.queries.length) {
            // more queries remain
            debug('transactionId ' + self.transactionId + ': there are ' +
                self.queries.length + '; popping one off the top...');
            var query = self.queries[0];
            self.queries.shift();
            self.numQueriesStarted++;
            self.nativeDB.executeSql(query, self);
        } else {
            // no more queries; end the transaction ?
            debug('transactionId ' + self.transactionId + ': there are 0 queries, self.numQueriesStarted is ' + self.numQueriesStarted +
                ', self.numQueriesEnded is ' + self.numQueriesEnded);
            var allQueriesComplete = self.numQueriesStarted && self.numQueriesStarted === self.numQueriesEnded;
            if (allQueriesComplete) {
                debug('ending this transaction with id ' + self.transactionId);
                self.endAsSuccessful();
            }
        }
    };

    SqliteTransaction.prototype.endAsFailure = function() {
        var self = this;

        var endTransactionDoneId = createCallback(function(){
            debug('transactionId ' + self.transactionId + ': cleaning up after failure.');
            self.nativeDB.transactionInProgress = false;
            self.nativeDB.processNextTransaction();
        });
        SQLiteJavascriptInterface.endTransaction(self.nativeDB.name, endTransactionDoneId, endTransactionDoneId, false);
    };

    SqliteTransaction.prototype.endAsSuccessful = function() {
        var self = this;

        var endTransactionSuccessId = createCallback(function(){
            // mark the entire transaction as successful
            debug('executing transaction success for transactionId ' + self.transactionId);
            self.success();
        });
        SQLiteJavascriptInterface.endTransaction(self.nativeDB.name, endTransactionSuccessId, self.errorId, true);
    };

    /**
     * Called by users of the transaction, not us.
     */
    SqliteTransaction.prototype.executeSql = function(sql, selectArgs, querySuccess, queryError) {
        var self = this;

        var query = new SqliteQuery(sql, selectArgs, self.wrapQuerySuccess(querySuccess), self.wrapQueryError(queryError));

        self.queries.push(query);

        self.runNextQueryOrEnd();
    };

    // override the traditional websql database
    var NativeDB = function(name) {
        var self = this;

        self.name = name;
        self.transactions = [];
        self.transactionInProgress = false;
    };

    NativeDB.prototype.init = function(success) {
        var self = this;
        debug('init()');

        var callbackId = createCallback(function(){
            if (success && typeof success === 'function') {
                success();
            }
        });

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

        if (self.transactions.length && !self.transactionInProgress) {

            self.transactionInProgress = false;

            var transaction = self.transactions[0];
            self.transactions.shift();

            var startTransactionErrorId = createCallback(transaction.error);
            var startTransactionSuccessId = createCallback(function (){
                transaction.callback(transaction);
            });
            SQLiteJavascriptInterface.startTransaction(self.name, startTransactionSuccessId, startTransactionErrorId);
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
            query.querySuccess(transaction, payload);
            debug('querySuccess called.');
        });
        var queryErrorId = createCallback(query.queryError);

        var selectArgsAsJson = query.selectArgs ? JSON.stringify(query.selectArgs) : null;
        SQLiteJavascriptInterface.executeSql(
            self.name, query.sql, selectArgsAsJson, querySuccessId, queryErrorId);
    };

    window.openDatabase = function(name, version, description, size, success) {
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

})();