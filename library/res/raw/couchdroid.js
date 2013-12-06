/* export CouchDroid */
var CouchDroid;

(function(){
    'use strict';
    CouchDroid = {
        DEBUG_MODE       : true,
        //DEBUG_CLASSES : ['NativeXMLHttpRequest', 'PouchDBHelper', 'SQLiteNativeDB'],
        DEBUG_CLASSES : ['NativeXMLHttpRequest', 'PouchDBHelper'],
        fakeLocalStorage : {} // for pouchdb
    };
})();