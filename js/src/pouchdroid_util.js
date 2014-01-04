(function () {
  'use strict';

  /**
   *
   * shamelessly stolen from Mozilla:
   * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Base64_encoding_and_decoding#Appendix.3A_Decode_a_Base64_string_to_Uint8Array_or_ArrayBuffer
   */
  function b64ToUint6(nChr) {

    return nChr > 64 && nChr < 91 ?
      nChr - 65
      : nChr > 96 && nChr < 123 ?
      nChr - 71
      : nChr > 47 && nChr < 58 ?
      nChr + 4
      : nChr === 43 ?
      62
      : nChr === 47 ?
      63
      :
      0;
  }

  function uint6ToB64(nUint6) {

    return nUint6 < 26 ?
      nUint6 + 65
      : nUint6 < 52 ?
      nUint6 + 71
      : nUint6 < 62 ?
      nUint6 - 4
      : nUint6 === 62 ?
      43
      : nUint6 === 63 ?
      47
      :
      65;
  }

  var base64 = {
    /**
     *
     * shamelessly stolen from Mozilla:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Base64_encoding_and_decoding#Appendix.3A_Decode_a_Base64_string_to_Uint8Array_or_ArrayBuffer
     */
    base64DecToArr: function (sBase64, nBlocksSize) {
      /* jshint bitwise : false */
      var
        sB64Enc = sBase64.replace(/[^A-Za-z0-9\+\/]/g, ''), nInLen = sB64Enc.length,
        nOutLen = nBlocksSize ? Math.ceil((nInLen * 3 + 1 >> 2) / nBlocksSize) * nBlocksSize : nInLen * 3 + 1 >> 2, taBytes = new Uint8Array(nOutLen);

      for (var nMod3, nMod4, nUint24 = 0, nOutIdx = 0, nInIdx = 0; nInIdx < nInLen; nInIdx++) {
        nMod4 = nInIdx & 3;
        nUint24 |= b64ToUint6(sB64Enc.charCodeAt(nInIdx)) << 18 - 6 * nMod4;
        if (nMod4 === 3 || nInLen - nInIdx === 1) {
          for (nMod3 = 0; nMod3 < 3 && nOutIdx < nOutLen; nMod3++, nOutIdx++) {
            taBytes[nOutIdx] = nUint24 >>> (16 >>> nMod3 & 24) & 255;
          }
          nUint24 = 0;

        }
      }

      return taBytes;
    },

    base64EncArr: function (aBytes) {
      /* jshint bitwise : false */
      var nMod3, sB64Enc = '';

      for (var nLen = aBytes.length, nUint24 = 0, nIdx = 0; nIdx < nLen; nIdx++) {
        nMod3 = nIdx % 3;
        if (nIdx > 0 && (nIdx * 4 / 3) % 76 === 0) {
          sB64Enc += '\r\n';
        }
        nUint24 |= aBytes[nIdx] << (16 >>> nMod3 & 24);
        if (nMod3 === 2 || aBytes.length - nIdx === 1) {
          sB64Enc += String.fromCharCode(uint6ToB64(nUint24 >>> 18 & 63), uint6ToB64(nUint24 >>> 12 & 63), uint6ToB64(nUint24 >>> 6 & 63), uint6ToB64(nUint24 & 63));
          nUint24 = 0;
        }
      }

      return sB64Enc.replace(/A(?=A$|$)/g, '=');
    }
  };


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

    base64ToArrayBuffer : function(str) {
      return base64.base64DecToArr(str).buffer;
    },

    arrayBufferToBase64: function (buffer) {
      var arr = new Uint8Array(buffer);
      return base64.base64EncArr(arr);
    },

    /**
     * build a blob with the deprecated BlobBuilder if available, else the Blob constructor
     * @param base64Str
     */
    buildBlob : function(base64Str, contentType) {

      var buffer = PouchDroid.Util.base64ToArrayBuffer(base64Str);

      var MyBlobBuilder = window.WebKitBlobBuilder || window.BlobBuilder;
      if (MyBlobBuilder) {
        var bb = new MyBlobBuilder();
        bb.append(buffer);
        return bb.getBlob(contentType);
      }
      // else use regular constructor
      return new Blob([buffer], {type : contentType});
    }
  };
})();