// fake cordova shim so the boys will like us
var cordova;

(function() {
  "use strict";
  
  function createEvent(type, data) {
    var event = document.createEvent('Events');
    event.initEvent(type, false, false);
    if (data) {
      for (var i in data) {
        if (data.hasOwnProperty(i)) {
          event[i] = data[i];
        }
      }
    }
    return event;
  }

  cordova = {

    fireWindowEvent: function(type, data) {
      var evt = createEvent(type, data);
      if (typeof windowEventHandlers[type] != 'undefined') {
        setTimeout(function() {
          windowEventHandlers[type].fire(evt);
        }, 0);
      } else {
        window.dispatchEvent(evt);
      }
    }
  };
})();
