/* export PouchDBHelper DEBUG_MODE */

var PouchDBHelper, DEBUG_MODE;

(function () {
    'use strict';

    PouchDBHelper = function (dbId, debugMode) {

        var self = this;

        try {
            self.db = new PouchDB(dbId);
        } catch (err) {
            window.console.log('error: ' + JSON.stringify(err));
        }
        self.debugMode = debugMode;

        if (self.debugMode) {
            window.console.log('created new PouchDBHelper with dbId ' + dbId);
        }

        DEBUG_MODE = debugMode;
    };

    PouchDBHelper.prototype.putAll = function (documents) {
        var self = this;

        if (self.debugMode) {
            window.console.log('attempting to put ' + documents.length + ' documents into PouchDB...');
        }

        function onPut(err, response) {
            window.console.log(err);
            window.console.log(response);
        }

        for (var i = 0, len = documents.length; i < len; i++) {
            self.db.put(documents[i], onPut);
        }
        if (self.debugMode) {
            window.console.log('put ' + documents.length + ' documents into PouchDB.');
        }
    };

})();