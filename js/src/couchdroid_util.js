(function(){
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
})();