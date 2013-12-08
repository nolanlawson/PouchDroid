package com.nolanlawson.couchdroid.pouch;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.text.TextUtils;
import android.util.SparseArray;
import android.webkit.JavascriptInterface;

import com.nolanlawson.couchdroid.PouchDB.Callback;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class PouchJavascriptInterface {

    public static final PouchJavascriptInterface INSTANCE = new PouchJavascriptInterface();

    private static UtilLogger log = new UtilLogger(PouchJavascriptInterface.class);

    private AtomicInteger callbackIds = new AtomicInteger(0);
    private SparseArray<Callback<?>> callbacks = new SparseArray<Callback<?>>();
    private ObjectMapper objectMapper = new ObjectMapper();

    private PouchJavascriptInterface() {
    }

    @JavascriptInterface
    public void callback(int callbackId) {
        callback(callbackId, null, null);
    }
    
    @JavascriptInterface
    public void callback(int callbackId, String errObjJson) {
        callback(callbackId, errObjJson, null);
    }
    
    @JavascriptInterface
    public void callback(int callbackId, String errObjJson, String infoObjJson) {
        log.d("callback(%s, %s, %s)", callbackId, errObjJson, infoObjJson);
        
        try {
            callbackAndPossiblyThrow(callbackId, errObjJson, infoObjJson);
        } catch (Exception e) {
            log.e(e, "unexpected exception");
            // what to do?
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void callbackAndPossiblyThrow(int callbackId, String errObjJson, String infoObjJson) throws IOException {
        Callback callback = callbacks.get(callbackId);
        if (callback == null) {
            log.i("callback was null: %s", callbackId);
            return;
        }

        Map<String, Object> errObj = null;
        if (!TextUtils.isEmpty(errObjJson)) {
            errObj = objectMapper.readValue(errObjJson, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        }
        Object infoObj = null;
        if (!TextUtils.isEmpty(infoObjJson)) {
            infoObj = objectMapper.readValue(infoObjJson, callback.getDeserializedClass());
        }
        
        callback.onCallback(errObj, infoObj);
    }

    /**
     * add a callback and return its ID.
     * @param callback
     * @return
     */
    public int addCallback(Callback<?> callback) {
        int callbackId = callbackIds.incrementAndGet();
        callbacks.put(callbackId, callback);
        return callbackId;
    }
}
