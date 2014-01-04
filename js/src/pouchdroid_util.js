(function () {
  'use strict';

  // From http://stackoverflow.com/questions/14967647/encode-decode-image-with-base64-breaks-image (2013-04-21)
  function fixBinary(bin) {
    var length = bin.length;
    var buf = new ArrayBuffer(length);
    var arr = new Uint8Array(buf);
    for (var i = 0; i < length; i++) {
      arr[i] = bin.charCodeAt(i);
    }
    return buf;
  }

  var blobSupport = true; // TODO

  PouchDroid.Util = {
    debug: function (className, str) {
      if (PouchDroid.DEBUG_MODE && str) {
        var validClass = (!PouchDroid.DEBUG_CLASSES || PouchDroid.DEBUG_CLASSES.indexOf(className) !== -1);
        if (!validClass) {
          return;
        }
        window.console.log(className + ': ' + str);
      }
    },

    blobToBase64: function (blob, callback) {
      var reader = new FileReader();
      reader.onloadend = function () {
        var result = btoa(this.result);
        callback.apply(null, [null, result]);
      };
      reader.readAsBinaryString(blob);
    },

    base64ToBlob : function(base64Str, contentType) {

      var data = atob(base64Str);
      if (blobSupport) {
        var type = contentType;
        data = fixBinary(data);
        return PouchDB.utils.createBlob([data], {type: type});
      }
    }
  };
})();