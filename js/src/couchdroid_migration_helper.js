(function () {
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

})();