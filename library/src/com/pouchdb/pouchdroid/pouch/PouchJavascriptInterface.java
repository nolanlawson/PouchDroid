package com.pouchdb.pouchdroid.pouch;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import android.text.TextUtils;
import android.util.SparseArray;
import android.webkit.JavascriptInterface;

import com.pouchdb.pouchdroid.pouch.callback.Callback;
import com.pouchdb.pouchdroid.pouch.model.PouchError;
import com.pouchdb.pouchdroid.util.UtilLogger;

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

        PouchError errObj = null;
        if (!TextUtils.isEmpty(errObjJson)) {
            errObj = objectMapper.readValue(errObjJson, PouchError.class);
        }
        Object infoObj = null;
        if (!TextUtils.isEmpty(infoObjJson)) {
            Object primaryClass = callback.getPrimaryClass();
            Class<?> genericClass = callback.getGenericClass();
            if (genericClass != null) {
                // the primary class has a generic
                JavaType javaType = objectMapper.getTypeFactory().constructParametricType(
                        (Class)primaryClass, genericClass);
                infoObj = objectMapper.readValue(infoObjJson, javaType);
                
            } else if (primaryClass instanceof Class) {
                // the primary class is a simple class
                infoObj = objectMapper.readValue(infoObjJson, (Class)primaryClass);
            } else {
                // the primary class uses TypeReference for more complex types, e.g. collections
                infoObj = objectMapper.readValue(infoObjJson, (TypeReference)primaryClass);
            }
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
