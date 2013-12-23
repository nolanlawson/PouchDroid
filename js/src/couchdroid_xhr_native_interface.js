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
        self.upload = {};
    }

    NativeXMLHttpRequest.prototype.onNativeProgress = function(isUpload) {
        // TODO: actually implement the progress event spec: http://www.w3.org/TR/progress-events/
        // TODO: actually call this method!  For now this method is never called
        var self = this;

        if (isUpload) {
            if (self.onprogress && typeof self.onprogress === 'function') {
                self.onprogress.call(null);
            }
        } else { // download
            if (self.upload.onprogress && typeof self.upload.onprogress === 'function') {
                self.upload.onprogress.call(null);
            }
        }
    };

    NativeXMLHttpRequest.prototype.callOnReadyStateChange = function() {
        var self = this;

        debug('calling onreadystatechange...');
        try {
            self.onreadystatechange();
        } catch (err2) {
            window.console.log('onreadystatechange threw error: ' + JSON.stringify(err2));
        }
        debug('called onreadystatechange.');
    };

    NativeXMLHttpRequest.prototype.onNativeCallback = function (err, statusCode, content) {
        var self = this;
        debug('onNativeCallback(' + statusCode +', ' + content +')');

        if (err) {
            // TODO: do something better?
            window.console.log('XHR error: ' + JSON.stringify(err));
        }

        self.readyState = STATES.DONE;
        self.status = statusCode;
        self.responseText = content;

        self.callOnReadyStateChange();

        // we don't need the xhr callback anymore; we can delete it
        delete CouchDroid.NativeXMLHttpRequests[self.id];
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

        // notify Java
        var selfStringified = JSON.stringify(self);
        try {
            XhrJavascriptInterface.abort(selfStringified);
        } catch (error) {
            window.console.log('failed to call XhrJavascriptInterface.abort() with selfStringified ' + selfStringified);
        }

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

        body = body || '';

        if (typeof body !== 'string') {
            // TODO
            /*
             * according to the xhr spec, this could be:
             *   void send();
                 void send(ArrayBuffer data);
                 void send(ArrayBufferView data);
                 void send(Blob data);
                 void send(Document data);
                 void send(DOMString? data);
                 void send(FormData data);
             */
            window.console.log('body isn\'t a string!  we don\'t know what to do!: ' + JSON.stringify(body));
            body = JSON.stringify(body);
        }

        CouchDroid.NativeXMLHttpRequests[self.id] = self;

        var selfStringified = JSON.stringify(self);

        debug('send(' + selfStringified + ',' + body + ')');

        self.state = STATES.LOADING;
        try {
            XhrJavascriptInterface.send(selfStringified, body);
        } catch (error) {
            window.console.log('failed to call XhrJavascriptInterface with selfStringified' +
                selfStringified + ' and body ' + body);
        }
    };

    CouchDroid.NativeXMLHttpRequest = NativeXMLHttpRequest;
    CouchDroid.NativeXMLHttpRequests = {};
})();