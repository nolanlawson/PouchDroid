/* export NativeDB */

(function(){
    'use strict';

    window.nativeDBCallbacks = window.nativeDBCallbacks || {};

    var callbacks = window.nativeDBCallbacks;

    // override the traditional websql database
    var NativeDB = function(name) {
        var self = this;

        self.name = name;
        self.transactions = [];
        self.transactionInProgress = false;
    };

    NativeDB.prototype.debug = function(str) {
        if (DEBUG_MODE && str) {
            window.console.log(str);
        }
    };

    NativeDB.prototype.init = function(success) {
        var self = this;
        self.debug('init()');

        var callbackId = self.createCallback(success);

        SQLiteJavascriptInterface.open(self.name, callbackId);
    };

    NativeDB.prototype.createCallback = function(fn) {
        var self = this;
        self.debug('createCallback()');

        fn = fn || function(){};

        var callbackId = self.name + '-' + Object.keys(callbacks).length;
        callbacks[callbackId] = fn;

        return callbackId;
    };

    NativeDB.prototype.transaction = function(fn, error, success) {
        var self = this;
        self.debug('transaction()');

        self.transactions.push({fn : fn, error : error, success: success, queryCompleteCount : 0, queryCount : 0});

        self.processTransaction();
    };

    NativeDB.prototype.processTransaction = function() {
        var self = this;
        self.debug('processTransaction()');

        if (self.transactions.length && !self.transactionInProgress) {

            var transaction = self.transactions[0];
            self.transactions.shift();

            var transactionErrorId = self.createCallback(transaction.error);
            var transactionSuccessId = self.createCallback(function (){
                self.onTransactionStarted(transaction, transactionErrorId);
            });
            SQLiteJavascriptInterface.startTransaction(self.name, transactionSuccessId, transactionErrorId);
        }
    };

    NativeDB.prototype.onTransactionStarted = function(transaction, transactionErrorId) {
        var self = this;
        self.debug('onTransactionStarted()');

        function TxObject() {
        }

        TxObject.prototype.executeSql = function(sql, selectArgs, queryCallback) {
            var txObject = this;
            self.executeSql(sql, selectArgs, queryCallback, transaction, transactionErrorId, txObject);

        };

        transaction.fn(new TxObject());
    };

    NativeDB.prototype.executeSql = function(sql, selectArgs, queryCallback, transaction, transactionErrorId, txObject) {
        var self = this;
        self.debug('executeSql()');

        transaction.queryCount++;

        var querySuccessId = self.createCallback(function(response){

            self.debug('query success!');

            // convert java response into websql-like response
            var rows = response.rows || [];
            var payload = {
                rows: {
                    item: function(i) {
                        return rows[i];
                    },
                    length: rows.length
                },
                rowsAffected: response.rowsAffected || 0,
                insertId: response.insertId || void 0
            };

            self.debug('calling queryCallback ' + queryCallback + 'query...');
            if (queryCallback) {
                queryCallback(txObject, payload);
            }
            self.debug('queryCallback called.');
            transaction.queryCompleteCount++;
            self.debug('transaction.queryCompleteCount is' + transaction.queryCompleteCount +
                ' and transaction.queryCount is ' + transaction.queryCompleteCount);
            if (transaction.queryCompleteCount && transaction.queryCompleteCount === transaction.queryCount) {

                var endTransactionSuccessId = self.createCallback(function(){
                    // mark the entire transaction as successful
                    if (transaction.success) {
                        self.debug('executing success: ' + transaction.success);
                        transaction.success();
                    }
                    self.transactionInProgress = false;
                    self.processTransaction();
                });
                SQLiteJavascriptInterface.endTransaction(self.name, endTransactionSuccessId, transactionErrorId);
            }
        });

        var selectArgsAsJson = selectArgs ? JSON.stringify(selectArgs) : null;
        SQLiteJavascriptInterface.executeSql(self.name, sql, selectArgsAsJson, querySuccessId, transactionErrorId);
    };

    window.openDatabase = function(name, version, description, size, success) {
        var nativeDB =  new NativeDB(name);
        nativeDB.init(success);
        return nativeDB;
    };

})();