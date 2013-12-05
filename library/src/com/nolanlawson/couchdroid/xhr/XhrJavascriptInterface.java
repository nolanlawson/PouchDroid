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
import org.codehaus.jackson.type.TypeReference;

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
            aborted.add(xhrAsJsonNode.findValue("id").asInt());
        } catch (IOException e) {
            log.e(e, "");
        }
    }
    
    @JavascriptInterface
    public void send(String xhrJsonObj) {
        log.d("send()");
        this.send(xhrJsonObj, "");
    }
    
    @JavascriptInterface
    public void send(String xhrJsonObj, String body) {
        log.d("send()");
        try {
            JsonNode xhrAsJsonNode = objectMapper.readTree(xhrJsonObj);
    
            int xhrId = xhrAsJsonNode.findValue("id").asInt();
            
            try {    
                send(xhrAsJsonNode, xhrId, body);
    
            } catch (IOException e) {
                log.e(e, "");
                callback(xhrId, 500, "Internal Java error");
            }
        } catch (IOException e) {
            log.e(e, "uncatchable exception");
            // TODO: what to do?
        }
    }

    private void send(JsonNode xhrAsJsonNode, int xhrId, String body) throws IOException {

        Map<String, String> requestHeaders = objectMapper.readValue(
                xhrAsJsonNode.findValue("requestHeaders"), new TypeReference<HashMap<String,String>>(){});

        String method = xhrAsJsonNode.findValue("method").asText();
        String url = xhrAsJsonNode.findValue("url").asText();

        HttpClient client = new DefaultHttpClient();
        HttpUriRequest request = createRequest(method, url);
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        if (!TextUtils.isEmpty(body)) {
            ((HttpEntityEnclosingRequestBase)request).setEntity(new ByteArrayEntity(
                    body.toString().getBytes("UTF8")));
        }
        HttpResponse response = client.execute(request);

        HttpEntity entity = response.getEntity();
        String content = readInput(entity.getContent());
        
        callback(xhrId, response.getStatusLine().getStatusCode(), content);
    }

    private void callback(int xhrId, int statusCode, String content) throws IOException {
        log.d("callback()");
        
        final String js  = new StringBuilder("javascript:(function(){")
            .append("NativeXMLHttpRequests[")
            .append(xhrId)
            .append("].onNativeCallback(")
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
        log.d("method is %s, url is %s", method, url);
        
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
}
