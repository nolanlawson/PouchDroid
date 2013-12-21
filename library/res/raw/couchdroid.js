/* export CouchDroid */
var CouchDroid;

(function(){
    'use strict';
    CouchDroid = {
        DEBUG_MODE       : false,
        //DEBUG_CLASSES : ['NativeXMLHttpRequest', 'PouchDBHelper', 'SQLiteNativeDB'],
        DEBUG_CLASSES : ['NativeXMLHttpRequest', 'PouchDBHelper'],
        fakeLocalStorage : {}, // for pouchdb
        pouchDBs: {} // Java user created pouch dbs
    };
})();
;(function () {
    'use strict';


    function debug(str) {
        CouchDroid.Util.debug('MigrationHelper', str);
    }

    function attachRevIdsToDocuments(documents, rows) {
        var idsToDocuments = {};
        documents.forEach(function(document){
            idsToDocuments[document._id] = document;
        });
        rows.forEach(function(row){
            if (row.value && row.value.rev) {
                // doc does indeed already exist
                idsToDocuments[row.id]._rev = row.value.rev;
            }
        });
    }

    function uncompress(compressedDocs) {
        // convert docs to a normal CouchDB format from the format
        // we used to pass them from Java to javascript

        /**
         * the compressed batch object looks like this:
         * {
         *  table : "MyTable",
         *  user: "Bobby B",
         *  ...
         *  columns : ["id", "name", "date", ...],
         *  uuids:['foo','bar','baz'...],
         *  docs : [[..values...], [...values...]...]
         *  }
         */
        var docs = [];
        for (var i = 0, iLen = compressedDocs.docs.length; i < iLen; i++) {
            var compressedDoc = compressedDocs.docs[i];
            var uuid = compressedDocs.uuids[i];
            var content = {};
            for (var j = 0, jLen = compressedDocs.columns.length; j < jLen; j++) {
                var column = compressedDocs.columns[j];
                var value = compressedDoc[j];
                content[column] = value;
            }
            var doc = {
                _id : uuid,
                table       : compressedDocs.table,
                user        : compressedDocs.user,
                sqliteDB    : compressedDocs.sqliteDB,
                appPackage  : compressedDocs.appPackage,
                content : content
            };

            docs.push(doc);
        }
        return docs;
    }

    function MigrationHelper(dbId) {

        var self = this;
        self.queue = [];
        self.batchInProgress = false;
        self.knownDocIds = {};

        debug('attempting to create new PouchDBHelper with dbId ' + dbId);
        try {
            self.db = new PouchDB(dbId);
        } catch (err) {
            debug('ERROR from new PouchDB(): ' + JSON.stringify(err));
            throw err;
        }

        if (CouchDroid.DEBUG_MODE) {
            debug('created new PouchDBHelper with dbId ' + dbId);
        }

    }

    /**
     * Register doc ids, so we can know which ones NOT to delete later
     * @param documents
     */
    MigrationHelper.prototype.registerDocIds = function(documents) {
        var self = this;
        documents.forEach(function(doc) {
            self.knownDocIds[doc._id] = true;
        });
    };

    /**
     * Delete all unregistered doc ids, report the number deleted to the callback.
     */
    MigrationHelper.prototype.deleteAllExceptKnownDocIds = function(callback) {
        var self = this;

        self.db.allDocs({include_docs : false}, function(err, info){

            var docsToRemove = [];

            if (info && info.rows) {
                info.rows.forEach(function(row){
                    if (!self.knownDocIds[row.id]) { // unknown, so remove
                        docsToRemove.push({_id : row.id, _rev : row.value.rev});
                    }
                });
            }
            var numDeleted = 0;
            function deleteAndContinue(idx) {

                if (idx > docsToRemove.length - 1) {
                    // base case
                    callback(numDeleted);
                } else {
                    //recursive case
                    self.db.remove(docsToRemove[idx], function(err, info){
                        if (info) {
                            numDeleted++;
                        }
                        deleteAndContinue(idx + 1);
                    });
                }
            }
            deleteAndContinue(0);
        });
    };

    /**
     * Put all documents into the database, overwriting any existing ones with the same IDs.
     *
     * I.e., we assume that whatever is currently in your SQLite database is the latest and greatest!
     *
     * @param documents
     */
    MigrationHelper.prototype.putAll = function (compressedDocs, onProgress) {
        var self = this;
        debug('putAll()');

        var documents = uncompress(compressedDocs);

        self.queue.push({docs : documents, onProgress: onProgress});

        self.processNextBatch();

    };

    MigrationHelper.prototype.processNextBatch = function() {
        var self = this;
        debug('processNextBatch()');

        if (self.queue.length && !self.batchInProgress) {
            self.batchInProgress = true;

            var batch = self.queue[0];
            self.queue.shift();
            self.processBatch(batch.docs, function onDone(){
                self.batchInProgress = false;
                if (batch.onProgress && typeof batch.onProgress === 'function') {
                    batch.onProgress(batch.docs.length); // progress listener
                }
                self.processNextBatch();
            });
        }

    };

    MigrationHelper.prototype.processBatch = function(documents, onDone) {
        var self = this;
        debug('processBatch()');


        function onBulkGet(err, response) {
            debug('onBulkGet(): got err from pouch: ' + err);
            debug('onBulkGet(): got response from pouch: ' + response);

            if (!response || !response.rows) {
                onDone();
                return;
            }

            attachRevIdsToDocuments(documents, response.rows);
            self.registerDocIds(documents);

            debug('onBulkGet(): attempting to put ' + documents.length + ' documents into PouchDB...');

            function onBulkPut(err, response) {
                debug('onBulkPut(): put ' + documents.length + ' documents into PouchDB.');
                debug('onBulkPut(): got err from pouch: ' + JSON.stringify(err));
                debug('onBulkPut(): got response from pouch: ' + JSON.stringify(response));
                onDone();
            }

            self.db.bulkDocs({docs : documents}, onBulkPut);
        }

        var keys = documents.map(function(document){return document._id;});

        self.db.allDocs({include_docs: false, keys : keys}, onBulkGet);
    };

    CouchDroid.MigrationHelper = MigrationHelper;

})();;/**
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

    SQLiteNativeDB.clearCallbacks = function(callbackIds) {

        // allows us to save memory by deleting callbacks in the hashmap
        callbackIds.forEach(function(callbackId){
            delete SQLiteNativeDB.callbacks[callbackId];
        });
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

    function createCallback(fn) {

        fn = fn || function(){};

        var callbackId = callbackIds++;

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
        });
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
        });
        var errorId = createCallback(function(){
            debug('executing transaction error for transactionId ' + self.transactionId);
            if (self.error && typeof self.error === 'function') {
                self.error();
            }
            self.nativeDB.processNextTransaction(); // todo: do we need this?
        });
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

        if (self.transactions.length) {

            var transaction = self.transactions.shift();

            debug('processing transaction with id ' + transaction.transactionId);
            debug('remaining transactions are: ' + JSON.stringify(self.transactions.map(function(transaction){return transaction.transactionId;})));

            var transactionErrorId = createCallback(transaction.error);
            var startTransactionSuccessId = createCallback(function (){
                transaction.callback(transaction);
            });
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
        });
        var queryErrorId = createCallback(query.queryError);

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
})();;(function(){
    'use strict';

    CouchDroid.Util = {
        debug : function(className, str) {
            if (CouchDroid.DEBUG_MODE && str) {
                var validClass = (!CouchDroid.DEBUG_CLASSES || CouchDroid.DEBUG_CLASSES.indexOf(className) !== -1);
                if (!validClass) {
                    return;
                }
                window.console.log(className + ': ' + str);
            }
        }
    };
})();;/**
 * pretends to be an XHR while actually calling native Java code.
 *
 */

(function () {
    'use strict';

    function debug(str) {
        CouchDroid.Util.debug('NativeXMLHttpRequest', str);
    }

    var ids = 0;

    var STATES = {
        UNSENT: 0,
        OPENED: 1,
        HEADERS_RECEIVED: 2,
        LOADING: 3,
        DONE: 4
    };

    function NativeXMLHttpRequest() {
        var self = this;

        self.id = ids++;
        self.withCredentials = false;
        self.responseType = null;
        self.onreadystatechange = null;
        self.readyState = STATES.UNSENT;
        self.status = 0;
        self.timeout = 0;
        self.response = null; // used if binary;
        self.responseText = null; // used if non-binary;
        self.requestHeaders = {};
        self.upload = {};
    }

    NativeXMLHttpRequest.prototype.onNativeProgress = function(isUpload) {
        // TODO: actually implement the progress event spec: http://www.w3.org/TR/progress-events/
        // TODO: actually call this method!  For now this method is never called
        var self = this;

        if (isUpload) {
            if (self.onprogress && typeof self.onprogress === 'function') {
                self.onprogress.call(null);
            }
        } else { // download
            if (self.upload.onprogress && typeof self.upload.onprogress === 'function') {
                self.upload.onprogress.call(null);
            }
        }
    };

    NativeXMLHttpRequest.prototype.onNativeCallback = function (err, statusCode, content) {
        var self = this;
        debug('onNativeCallback(' + statusCode +', ' + content +')');

        if (err) {
            // TODO: do something?
            window.console.log(JSON.stringify(err));
        } else {
            self.status = statusCode;
            self.readyState = STATES.DONE;

            self.responseText = content;

            debug('calling onreadystatechange...');
            try {
                self.onreadystatechange();
            } catch (err2) {
                window.console.log('onreadystatechange threw error: ' + JSON.stringify(err2));
            }
            debug('called onreadystatechange.');
        }

        // we don't need the xhr callback anymore; we can delete it
        delete CouchDroid.NativeXMLHttpRequests[self.id];
    };

    NativeXMLHttpRequest.prototype.open = function (method, url) {
        var self = this;

        debug('open()');

        self.state = STATES.OPENED;
        self.method = method;
        self.url = url;
    };

    NativeXMLHttpRequest.prototype.abort = function () {
        var self = this;

        debug('abort()');
        self.state = STATES.DONE;
        var selfStringified = JSON.stringify(self);
        try {
            XhrJavascriptInterface.abort(selfStringified);
        } catch (error) {
            window.console.log('failed to call XhrJavascriptInterface.abort() with selfStringified ' + selfStringified);
        }

    };

    NativeXMLHttpRequest.prototype.setRequestHeader = function (key, value) {
        var self = this;

        debug('setRequestHeader()');

        self.requestHeaders[key] = value;
    };

    NativeXMLHttpRequest.prototype.getRequestHeader = function (key) {
        var self = this;

        debug('getRequestHeader()');

        return self.requestHeaders[key];
    };

    NativeXMLHttpRequest.prototype.send = function (body) {
        var self = this;

        body = body || '';

        if (typeof body !== 'string') {
            // TODO
            /*
             * according to the xhr spec, this could be:
             *   void send();
                 void send(ArrayBuffer data);
                 void send(ArrayBufferView data);
                 void send(Blob data);
                 void send(Document data);
                 void send(DOMString? data);
                 void send(FormData data);
             */
            window.console.log('body isn\'t a string!  we don\'t know what to do!: ' + JSON.stringify(body));
            body = JSON.stringify(body);
        }

        CouchDroid.NativeXMLHttpRequests[self.id] = self;

        var selfStringified = JSON.stringify(self);

        debug('send(' + selfStringified + ',' + body + ')');

        self.state = STATES.LOADING;
        try {
            XhrJavascriptInterface.send(selfStringified, body);
        } catch (error) {
            window.console.log('failed to call XhrJavascriptInterface with selfStringified' +
                selfStringified + ' and body ' + body);
        }
    };

    CouchDroid.NativeXMLHttpRequest = NativeXMLHttpRequest;
    CouchDroid.NativeXMLHttpRequests = {};
})();;/* some small shims; I don't want to have to include all of underscore.js */
/* much of this is copied from https://github.com/kriskowal/es5-shim/blob/master/es5-shim.js */
(function() {
    'use strict';

    if (!Object.keys) {
        Object.keys = function keys(object) {

            if ((typeof object !== 'object' && typeof object !== 'function') ||
                    object === null) {
                throw new TypeError('Object.keys called on a non-object');
            }

            var mykeys = [];
            for (var name in object) {
                if (Object.prototype.hasOwnProperty.call(object, name)) {
                    mykeys.push(name);
                }
            }
            return mykeys;
        };
    }

    if (!Array.isArray) {
        Array.isArray = function isArray(obj) {
            return Object.prototype.toString.call(obj) === '[object Array]';
        };
    }

    if (!('forEach' in Array.prototype)) {
        Array.prototype.forEach= function(action, that /*opt*/) {
            for (var i= 0, n= this.length; i<n; i++) {
                if (i in this) {
                    action.call(that, this[i], i, this);
                }
            }
        };
    }
    if (!('map' in Array.prototype)) {
        Array.prototype.map= function(mapper, that /*opt*/) {
            var other= new Array(this.length);
            for (var i= 0, n= this.length; i<n; i++) {
                if (i in this) {
                    other[i]= mapper.call(that, this[i], i, this);
                }
            }
            return other;
        };
    }

})();