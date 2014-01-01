/* some small shims for es5; I don't want to have to include all of underscore.js */
/* much of this is copied from https://github.com/kriskowal/es5-shim/blob/master/es5-shim.js */
(function () {
  'use strict';

  if (!Object.keys) {
    Object.keys = function keys(object) {

      if ((typeof object !== 'object' && typeof object !== 'function') ||
        object === null) {
        throw new TypeError('Object.keys called on a non-object');
      }

      var mykeys = [];
      for (var name in object) {
        if (Object.prototype.hasOwnProperty.call(object, name)) {
          mykeys.push(name);
        }
      }
      return mykeys;
    };
  }

  if (!Array.isArray) {
    Array.isArray = function isArray(obj) {
      return Object.prototype.toString.call(obj) === '[object Array]';
    };
  }

  if (!('forEach' in Array.prototype)) {
    Array.prototype.forEach = function (action, that /*opt*/) {
      for (var i = 0, n = this.length; i < n; i++) {
        if (i in this) {
          action.call(that, this[i], i, this);
        }
      }
    };
  }
  if (!('map' in Array.prototype)) {
    Array.prototype.map = function (mapper, that /*opt*/) {
      var other = new Array(this.length);
      for (var i = 0, n = this.length; i < n; i++) {
        if (i in this) {
          other[i] = mapper.call(that, this[i], i, this);
        }
      }
      return other;
    };
  }

})();