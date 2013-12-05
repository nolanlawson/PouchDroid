/**
 * prentends to be an XHR while actually calling native Java code.
 *
 * export NativeXMLHttpRequest, NativeXMLHttpRequests
 */

(function () {
    'use strict';

    /*
    for debugging:
     pouchDBHelper.db.replicate.to('http://admin:password@192.168.0.3:5984/pokemon', {continuous : false, complete: function complete(err, response){
     console.log('complete, with err: ' + JSON.stringify(err));
     console.log('complete, with response: ' + JSON.stringify(response));
     }, onChange: function onChange(change) {
     console.log('onChange, with change: ' + JSON.stringify(change));
     }})
     */
    var ids = 0;

    var STATES = {
        UNSENT: 0,
        OPENED: 1,
        HEADERS_RECEIVED: 2,
        LOADING: 3,
        DONE: 4
    };

    function debug(str) {
        if (DEBUG_MODE && str) {
            window.console.log('NativeXMLHttpRequest: ' + str);
        }
    }

    function NativeXMLHttpRequest() {
        var self = this;

        self.id = ids++;
        self.withCredentials = false;
        self.responseType = null;
        self.onreadystatechange = null;
        self.readyState = STATES.UNSENT;
        self.status = 0;
        self.response = null; // used if binary;
        self.responseText = null; // used if non-binary;
        self.requestHeaders = {};

    }

    NativeXMLHttpRequest.prototype.onNativeCallback = function (statusCode, content) {
        var self = this;
        debug('onNativeCallback(' + statusCode +', ' + content +')');

        self.status = statusCode;
        self.readyState = STATES.DONE;

        self.responseText = content;

        debug('calling onreadystatechange...');
        self.onreadystatechange();
        debug('called onreadystatechange.');
    };

    NativeXMLHttpRequest.prototype.open = function (method, url) {
        var self = this;

        debug('open()');

        self.method = method;
        self.url = url;
    };

    NativeXMLHttpRequest.prototype.abort = function () {
        debug('abort()');
        XhrJavascriptInterface.abort();
    };

    NativeXMLHttpRequest.prototype.setRequestHeader = function (key, value) {
        var self = this;

        debug('setRequestHeader()');

        self.requestHeaders[key] = value;
    };

    NativeXMLHttpRequest.prototype.getRequestHeader = function (key) {
        var self = this;

        debug('getRequestHeader()');

        return self.requestHeaders[key];
    };

    NativeXMLHttpRequest.prototype.send = function (body) {
        var self = this;

        window.NativeXMLHttpRequests[self.id] = self;

        var selfStringified = JSON.stringify(self);

        debug('send(' + selfStringified + ',' + body + ')');

        XhrJavascriptInterface.send(selfStringified, body);
    };

    window.NativeXMLHttpRequest = NativeXMLHttpRequest;
    window.NativeXMLHttpRequests = {};

})();