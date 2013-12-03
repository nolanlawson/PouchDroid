/* export PouchDBHelper */

var PouchDBHelper;

(function () {
    'use strict';

    function debug(str) {
        if (DEBUG_MODE) {
            window.console.log(str);
        }
    }

    PouchDBHelper = function (dbId) {

        var self = this;

        try {
            self.db = new PouchDB(dbId);
        } catch (err) {
            debug('error: ' + JSON.stringify(err));
        }

        if (DEBUG_MODE) {
            debug('created new PouchDBHelper with dbId ' + dbId);
        }

    };

    PouchDBHelper.prototype.putAll = function (documents) {
        var self = this;

        debug('attempting to put ' + documents.length + ' documents into PouchDB...');

        function onPut(err, response) {
            debug('got err from pouch: ' + JSON.stringify(err));
            debug('got response from pouch: ' + JSON.stringify(response));

            if (DEBUG_MODE && response && response.ok) {
                debug('getting doc: ' + response.id + '...');
                self.db.get(response.id, function(err, doc){
                    debug('got err from pouch: ' + err);
                    debug('got doc from pouch: ' + JSON.stringify(doc));
                });
            }
        }

        for (var i = 0, len = documents.length; i < len; i++) {
            self.db.put(documents[i], onPut);
        }
        debug('put ' + documents.length + ' documents into PouchDB.');
    };

})();