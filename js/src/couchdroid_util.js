(function(){
    'use strict';

    PouchDroid.Util = {
        debug : function(className, str) {
            if (PouchDroid.DEBUG_MODE && str) {
                var validClass = (!PouchDroid.DEBUG_CLASSES || PouchDroid.DEBUG_CLASSES.indexOf(className) !== -1);
                if (!validClass) {
                    return;
                }
                window.console.log(className + ': ' + str);
            }
        }
    };
})();