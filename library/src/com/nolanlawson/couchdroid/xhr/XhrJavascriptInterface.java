package com.nolanlawson.couchdroid.xhr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.nolanlawson.couchdroid.util.UtilLogger;

/**
 * Interface that pretends to be an XMLHTTPRequest, so that we can bypass
 * CORS/JSON/all that security bs.
 * 
 * Like the SQLiteJavascriptInterface, this only implements a subset of the API,
 * in order to satisfy PouchDB.
 * 
 * @author nolan
 * 
 */
public class XhrJavascriptInterface {

    private static UtilLogger log = new UtilLogger(XhrJavascriptInterface.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    
    private WebView webView;
    
    public XhrJavascriptInterface(WebView webView) {
        this.webView = webView;
    }

    private Set<Integer> aborted = new HashSet<Integer>();
    
    @JavascriptInterface
    public void abort(String xhrJsonObj) {
        log.d("abort()");
        try{
            
            JsonNode xhrAsJsonNode = objectMapper.readTree(xhrJsonObj);
            // TODO: memory leak
            aborted.add(xhrAsJsonNode.get("id").asInt());
        } catch (IOException e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }
    
    @JavascriptInterface
    public void send(String xhrJsonObj) {
        log.d("send()");
        try {
            this.send(xhrJsonObj, "");
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }
    
    @JavascriptInterface
    public void send(String xhrJsonObj, String body) {
        log.d("send()");
        try {
            JsonNode xhrAsJsonNode = objectMapper.readTree(xhrJsonObj);
    
            int xhrId = xhrAsJsonNode.get("id").asInt();
            
            try {    
                send(xhrAsJsonNode, xhrId, body);
    
            } catch (IOException e) {
                log.e(e, "");
                
                ObjectNode error = objectMapper.createObjectNode();
                error.put("type", "error");
                error.put("message", "Error connecting with XhrJavascriptInterface, most likely an HTTP timeout");
                callback(xhrId, objectMapper.writeValueAsString(error), -1, null);
            }
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "uncatchable exception");
        }
    }

    private void send(JsonNode xhrAsJsonNode, final int xhrId, String body) throws IOException {

        if (aborted.contains(xhrId)) {
            log.i("aborted %d", xhrId);
            return;
        }
        
        Map<String, String> requestHeaders = objectMapper.readValue(
                xhrAsJsonNode.get("requestHeaders"), new TypeReference<HashMap<String,String>>(){});

        String method = xhrAsJsonNode.get("method").asText();
        String url = xhrAsJsonNode.get("url").asText();
        log.d("xhrId: %s, method: %s, url: %s, body: %s", xhrId, method, url, body);
        
        JsonNode timeoutValue = xhrAsJsonNode.get("timeout");
        int timeout = timeoutValue != null ? (int)timeoutValue.asLong(0) : 0;
        
        JsonNode binaryValue = xhrAsJsonNode.get("binary");
        boolean binary = binaryValue != null && binaryValue.asBoolean();
        
        if (binary) {
            // TODO: implement binary
            throw new IllegalArgumentException("Client asked for binary, but we haven't implemented binary yet!");
        }
        
        final HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.socket.timeout", timeout);
        final HttpUriRequest request = createRequest(method, url);
        
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        
        if (!TextUtils.isEmpty(body)) {
            ((HttpEntityEnclosingRequestBase)request).setEntity(new ByteArrayEntity(
                    body.toString().getBytes("UTF8")));
        }
        
        new AsyncTask<Void, Void, SimpleHttpResponse>() {

            @Override
            protected SimpleHttpResponse doInBackground(Void... params) {
                try {
                    HttpResponse response = client.execute(request);
                    
                    HttpEntity entity = response.getEntity();
                    String content = readInput(entity.getContent());
                    
                    return new SimpleHttpResponse(content, response.getStatusLine().getStatusCode());
                } catch (Exception e) {
                    log.e(e, "exception within doInBackground");
                    return null;
                }
            }

            @Override
            protected void onPostExecute(SimpleHttpResponse response) {
                super.onPostExecute(response);
                
                if (response == null) {
                    log.e("http response is null.  Did you remember to add " +
                    		"<uses-permission android:name=\"android.permission.INTERNET\"/> " +
                    		"to your AndroidManifest.xml?");
                    return;
                }
                
                try {
                    
                    if (aborted.contains(xhrId)) {
                        log.i("aborted %d", xhrId);
                        return;
                    }
                    callback(xhrId, null, response.statusCode, response.body);
                    
                } catch (Exception e) {
                    log.e(e, "exception within onPostExecute");
                }
            }
            
        }.execute((Void)null);
    }

    private void callback(int xhrId, String error, int statusCode, String content) throws IOException {
        log.d("callback()");
        
        final String js  = new StringBuilder("javascript:(function(){")
            .append("CouchDroid.NativeXMLHttpRequests[")
            .append(xhrId)
            .append("].onNativeCallback(")
            .append(error)
            .append(",")
            .append(statusCode)
            .append(",")
            .append(TextUtils.isEmpty(content) ? "\"null\"" : objectMapper.writeValueAsString(content))
            .append(");})();").toString();
        
        log.d("javascript is %s", js);
        
        webView.post(new Runnable() {
            
            @Override
            public void run() {
                webView.loadUrl(js);
            }
        });
    }

    private static String readInput(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte bytes[] = new byte[1024];

        int n = in.read(bytes);

        while (n != -1) {
            out.write(bytes, 0, n);
            n = in.read(bytes);
        }

        return new String(out.toString());
    }

    private static HttpUriRequest createRequest(String method, String url) {
        method = method.toUpperCase(Locale.US);

        if (TextUtils.isEmpty(method)) {
            throw new IllegalArgumentException("we don't understand blank http methods");
        }

        // we're kickin' it NodeJS style
        switch (method.charAt(0)) {
            case 'G':
                return new HttpGet(url);
            case 'D':
                return new HttpDelete(url);
            case 'O':
                return new HttpOptions(url);
            case 'H':
                return new HttpHead(url);
            case 'P':
                switch (method.charAt(1)) {
                    case 'O':
                        return new HttpPost(url);
                    case 'U':
                        return new HttpPut(url);
                }
        }
        throw new IllegalArgumentException("we don't understand the http method: " + method);
    }
    
    private static class SimpleHttpResponse {
        String body;
        int statusCode;
        
        SimpleHttpResponse(String body, int statusCode) {
            this.body = body;
            this.statusCode = statusCode;
        }
        
        
    }
}
