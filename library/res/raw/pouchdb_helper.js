var PouchDBHelper;

(function() {
  "use strict";

  PouchDBHelper = function(packageName, debugMode) {
    
    var self = this;
    
    self.db = new PouchDB(packageName);
    self.debugMode = debugMode;
    
    if (self.debugMode) {
      window.console.log("created new PouchDBHelper with packageName " + packageName);
    }
  };
  
  PouchDBHelper.prototype.putAll = function(documents) {
    var self = this;
    
    if (self.debugMode) {
      window.console.log("attempting to put " + documents.length + " documents into PouchDB...");
    }
    
    for (var i = 0, len = documents.length; i < len; i++) {
      self.db.put(documents[i], function(err, response) {
        window.console.log(err);
        window.console.log(response);
      });
    }
    if (self.debugMode) {
      window.console.log("put " + documents.length + " documents into PouchDB.");
    }
  };

})();