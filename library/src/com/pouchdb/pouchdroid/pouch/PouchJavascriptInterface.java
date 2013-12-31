package com.pouchdb.pouchdroid.pouch;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import android.text.TextUtils;
import android.util.SparseArray;
import android.webkit.JavascriptInterface;

import com.pouchdb.pouchdroid.PouchDroid;
import com.pouchdb.pouchdroid.pouch.MapFunction.EmitListener;
import com.pouchdb.pouchdroid.pouch.callback.Callback;
import com.pouchdb.pouchdroid.pouch.model.PouchError;
import com.pouchdb.pouchdroid.util.UtilLogger;

public class PouchJavascriptInterface {

    private static UtilLogger log = new UtilLogger(PouchJavascriptInterface.class);

    private PouchDroid pouchDroid;
    private AtomicInteger mrIds = new AtomicInteger(0);
    private AtomicInteger callbackIds = new AtomicInteger(0);
    private SparseArray<Callback<?>> callbacks = new SparseArray<Callback<?>>();
    private SparseArray<MapReduceFunction<?>> mapReduceCallbacks = new SparseArray<MapReduceFunction<?>>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public PouchJavascriptInterface(PouchDroid pouchDroid) {
        this.pouchDroid = pouchDroid;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @JavascriptInterface
    public void mapCallback(final int mapReduceCallbackId, String docObjJson) {
        MapReduceFunction mrFunction = mapReduceCallbacks.get(mapReduceCallbackId);
        PouchDocumentInterface doc = PouchDocumentMapper.fromJson(docObjJson, mrFunction.getDocumentClass());
        mrFunction.getMap().map(doc, new EmitListener() {

            @Override
            public void onEmit(Object key, Object value) {
                StringBuilder js;
                try {
                    js = new StringBuilder()
                        .append("window.PouchDroid.emitFunctions[")
                        .append(mapReduceCallbackId)
                        .append("].apply(null, [JSON.parse(")
                        .append(objectMapper.writeValueAsString(key))
                        .append("),JSON.parse(")
                        .append(objectMapper.writeValueAsString(value))
                        .append(")]);");
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e); // shouldn't happen
                }
                pouchDroid.loadJavascript(js);
            }
        });
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
    
    public int addMapReduceFunction(MapReduceFunction<?> mrFunction) {
        int mrId = mrIds.incrementAndGet();
        mapReduceCallbacks.put(mrId, mrFunction);
        return mrId;
    }
}
