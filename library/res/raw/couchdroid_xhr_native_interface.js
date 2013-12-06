/**
 * pretends to be an XHR while actually calling native Java code.
 *
 */

(function () {
    'use strict';

    function debug(str) {
        CouchDroid.Util.debug('NativeXMLHttpRequest', str);
    }

    var ids = 0;

    var STATES = {
        UNSENT: 0,
        OPENED: 1,
        HEADERS_RECEIVED: 2,
        LOADING: 3,
        DONE: 4
    };

    function NativeXMLHttpRequest() {
        var self = this;

        self.id = ids++;
        self.withCredentials = false;
        self.responseType = null;
        self.onreadystatechange = null;
        self.readyState = STATES.UNSENT;
        self.status = 0;
        self.timeout = 0;
        self.response = null; // used if binary;
        self.responseText = null; // used if non-binary;
        self.requestHeaders = {};

    }

    NativeXMLHttpRequest.prototype.onNativeCallback = function (err, statusCode, content) {
        var self = this;
        debug('onNativeCallback(' + statusCode +', ' + content +')');

        if (err) {
            // TODO: do something?
            window.console.log(JSON.stringify(err));
        } else {
            self.status = statusCode;
            self.readyState = STATES.DONE;

            self.responseText = content;

            debug('calling onreadystatechange...');
            self.onreadystatechange();
            debug('called onreadystatechange.');
        }


    };

    NativeXMLHttpRequest.prototype.open = function (method, url) {
        var self = this;

        debug('open()');

        self.state = STATES.OPENED;
        self.method = method;
        self.url = url;
    };

    NativeXMLHttpRequest.prototype.abort = function () {
        var self = this;

        debug('abort()');
        self.state = STATES.DONE;
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

        CouchDroid.NativeXMLHttpRequests[self.id] = self;

        var selfStringified = JSON.stringify(self);

        debug('send(' + selfStringified + ',' + body + ')');

        self.state = STATES.LOADING;

        XhrJavascriptInterface.send(selfStringified, body);
    };

    CouchDroid.NativeXMLHttpRequest = NativeXMLHttpRequest;
    CouchDroid.NativeXMLHttpRequests = {};
})();