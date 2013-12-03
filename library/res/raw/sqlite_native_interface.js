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

    var SqliteQuery = function(sql, selectArgs, queryCallback) {
        var self = this;

        self.sql = sql;
        self.selectArgs = selectArgs;
        self.queryCallback = queryCallback;
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

            var endTransactionDoneId = createCallback(function(){
                debug('transactionId ' + self.transactionId + ': cleaning up after failure.');
                self.nativeDB.transactionInProgress = false;
                self.nativeDB.processNextTransaction();
            });
            SQLiteJavascriptInterface.endTransaction(self.nativeDB.name, endTransactionDoneId, endTransactionDoneId, false);
        };
    };

    SqliteTransaction.prototype.wrapCallback = function(queryCallback) {
        var self = this;

        return function(transaction, payload) {
            self.numQueriesEnded++;
            if (queryCallback && typeof queryCallback === 'function') {
                queryCallback(transaction, payload);
            }
            self.runNextQueryOrEnd(); // if new queries were pushed, process those
        };
    };

    SqliteTransaction.prototype.runNextQueryOrEnd = function() {
        var self = this;
        if (self.queries.length) {
            debug('transactionId ' + self.transactionId + ': there are ' +
                self.queries.length + '; popping one off the top...');
            var query = self.queries[0];
            self.queries.shift();
            self.numQueriesStarted++;
            self.nativeDB.executeSql(query, self);
        } else {
            // no more queries; end the transaction
            debug('transactionId ' + self.transactionId + ': there are 0 queries, self.numQueriesStarted is ' + self.numQueriesStarted +
                ', self.numQueriesEnded is ' + self.numQueriesEnded);
            if (self.numQueriesStarted && self.numQueriesStarted === self.numQueriesEnded) {
                debug('ending this transaction with id ' + self.transactionId);
                self.end();
            }

        }
    };

    SqliteTransaction.prototype.end = function() {
        var self = this;

        var endTransactionSuccessId = createCallback(function(){
            // mark the entire transaction as successful
            debug('executing transaction success for transactionId ' + self.transactionId);
            self.success();
        });
        SQLiteJavascriptInterface.endTransaction(self.nativeDB.name, endTransactionSuccessId, self.errorId, true);
    };

    SqliteTransaction.prototype.executeSql = function(sql, selectArgs, queryCallback) {
        var self = this;

        var query = new SqliteQuery(sql, selectArgs, self.wrapCallback(queryCallback));

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

            debug('calling queryCallback function...');
            query.queryCallback(transaction, payload);
            debug('queryCallback called.');
        });

        var selectArgsAsJson = query.selectArgs ? JSON.stringify(query.selectArgs) : null;
        SQLiteJavascriptInterface.executeSql(
            self.name, query.sql, selectArgsAsJson, querySuccessId,
            transaction.errorId);
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