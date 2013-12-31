/* export PouchDroid */
var PouchDroid;

(function(){
    'use strict';
    PouchDroid = {
        DEBUG_MODE       : false,
        //DEBUG_CLASSES : ['NativeXMLHttpRequest', 'PouchDBHelper', 'SQLiteNativeDB'],
        DEBUG_CLASSES : ['NativeXMLHttpRequest', 'PouchDBHelper'],
        fakeLocalStorage : {}, // for pouchdb
        pouchDBs: {} // Java user created pouch dbs
    };
})();
