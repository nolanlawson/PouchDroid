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
