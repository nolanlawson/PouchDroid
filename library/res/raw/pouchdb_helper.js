/* export PouchDBHelper */

var PouchDBHelper;

(function () {
    'use strict';

    function debug(str) {
        if (DEBUG_MODE) {
            window.console.log('PouchDBHelper: ' + str);
        }
    }

    function attachRevIdsToDocuments(documents, rows) {
        var idsToDocuments = {};
        documents.forEach(function(document){
            idsToDocuments[document._id] = document;
        });
        rows.forEach(function(row){
            if (row.doc) { // doc does indeed already exist
                idsToDocuments[row.doc._id]._rev = row.doc._rev;
            }
        });
    }

    PouchDBHelper = function (dbId) {

        var self = this;
        self.queue = [];
        self.batchInProgress = false;

        try {
            self.db = new PouchDB(dbId);
        } catch (err) {
            debug('error: ' + JSON.stringify(err));
        }

        if (DEBUG_MODE) {
            debug('created new PouchDBHelper with dbId ' + dbId);
        }

    };

    /**
     * Put all documents into the database, overwriting any existing ones with the same IDs.
     *
     * I.e., we assume that whatever is currently in your SQLite database is the latest and greatest!
     *
     * @param documents
     */
    PouchDBHelper.prototype.putAll = function (documents) {
        var self = this;
        debug('putAll()');

        self.queue.push({docs : documents});

        self.processNextBatch();

    };

    PouchDBHelper.prototype.processNextBatch = function() {
        var self = this;
        debug('processNextBatch()');

        if (self.queue.length && !self.batchInProgress) {
            self.batchInProgress = true;

            var batch = self.queue[0];
            self.queue.shift();
            self.processBatch(batch.docs, function onDone(){
                self.batchInProgress = false;
                self.processNextBatch();
            });
        }

    };

    PouchDBHelper.prototype.processBatch = function(documents, onDone) {
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

        self.db.allDocs({include_docs: true, keys : keys}, onBulkGet);
    };

})();