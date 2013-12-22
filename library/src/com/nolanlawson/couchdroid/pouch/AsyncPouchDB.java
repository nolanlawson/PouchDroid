package com.nolanlawson.couchdroid.pouch;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import android.app.Activity;
import android.text.TextUtils;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.pouch.callback.AllDocsCallback;
import com.nolanlawson.couchdroid.pouch.callback.BulkCallback;
import com.nolanlawson.couchdroid.pouch.callback.Callback;
import com.nolanlawson.couchdroid.pouch.callback.GetCallback;
import com.nolanlawson.couchdroid.pouch.callback.ReplicateCallback;
import com.nolanlawson.couchdroid.pouch.callback.StandardCallback;
import com.nolanlawson.couchdroid.pouch.model.AllDocsInfo;
import com.nolanlawson.couchdroid.pouch.model.PouchError;
import com.nolanlawson.couchdroid.util.JsonUtil;
import com.nolanlawson.couchdroid.util.Maps;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class AsyncPouchDB<T extends PouchDocumentInterface> extends AbstractPouchDB<T> {

    private static UtilLogger log = new UtilLogger(AsyncPouchDB.class);

    private static final AtomicInteger POUCH_IDS = new AtomicInteger(0);

    private int id;
    private CouchDroidRuntime runtime;
    private String name;
    private boolean destroyed = false;

    private Class<T> documentClass;
    
    /* package */ AsyncPouchDB(Class<T> documentClass, CouchDroidRuntime runtime, String name, boolean autoCompaction) {
        this.id = POUCH_IDS.incrementAndGet();
        this.documentClass = documentClass;
        this.runtime = runtime;
        this.name = name;
        
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null/empty");
        }

        runtime.loadJavascript(new StringBuilder("CouchDroid.pouchDBs[")
                .append(id).append("] = new PouchDB(")
                .append(JsonUtil.simpleMap("name", name, "autoCompaction", autoCompaction))
                .append(");"));
    }
    
    /**
     * Returns the name of the database that was provided in the constructor call.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if this database was destroyed, i.e. the database was deleted.
     * @return
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void destroy(Map<String, Object> options, StandardCallback callback) {
        
        // need to call it statically, so can't use loadAction()
        options = options == null ? new LinkedHashMap<String, Object>() : options;
        options.put("name", name);
        runtime.loadJavascript(new StringBuilder()
            .append("PouchDB.destroy(")
            .append(JsonUtil.simpleMap(options))
            .append(",")
            .append(createFunctionForCallback(callback))
            .append(");")
            // won't need this database anymore
            .append("delete CouchDroid.pouchDBs[").append(id).append("];"));
        
        destroyed = true;
    }
    
    /**
     * @see AsyncPouchDB#destroy(options, callback)
     */
    public void destroy(StandardCallback callback) {
        destroy(null, callback);
    }
    
    /**
     * @see AsyncPouchDB#destroy(options, callback)
     */
    public void destroy(Map<String, Object> options) {
        destroy(options, null);
    }

    /**
     * @see AsyncPouchDB#destroy(options, callback)
     */
    public void destroy() {
        destroy(null, null);
    }    

    public void put(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("put", PouchDocumentMapper.toJson(doc), options, callback);
    }

    /**
     * @see AsyncPouchDB#put(doc, options, callback)
     */
    public void put(T doc, Map<String, Object> options) {
        put(doc, options, null);
    }

    /**
     * @see AsyncPouchDB#put(doc, options, callback)
     */
    public void put(T doc, StandardCallback callback) {
        put(doc, null, callback);
    }

    /**
     * @see AsyncPouchDB#put(doc, options, callback)
     */
    public void put(T doc) {
        put(doc, null, null);
    }

    public void post(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("post", PouchDocumentMapper.toJson(doc), options, callback);
    }

    /**
     * @see AsyncPouchDB#post(doc, options, callback)
     */
    public void post(T doc, Map<String, Object> options) {
        post(doc, options, null);
    }

    /**
     * @see AsyncPouchDB#post(doc, options, callback)
     */
    public void post(T doc, StandardCallback callback) {
        post(doc, null, callback);
    }

    /**
     * @see AsyncPouchDB#post(doc, options, callback)
     */
    public void post(T doc) {
        post(doc, null, null);
    }

    
    public void get(String docid, Map<String, Object> options, final GetCallback<T> callback) {
        loadAction("get", JsonUtil.simpleString(docid), options, callback == null ? null : new Callback<T>() {

            @Override
            public void onCallback(PouchError err, T info) {
                callback.onCallback(err, info);
            }

            @Override
            public Class<?> getPrimaryClass() {
                return documentClass;
            }

            @Override
            public Class<?> getGenericClass() {
                return null;
            }
        });
    }


    /**
     * @see AsyncPouchDB#get(docid, options, callback)
     */
    public void get(String docid, Map<String, Object> options) {
        get(docid, options, null);
    }

    /**
     * @see AsyncPouchDB#get(docid, options, callback)
     */
    public void get(String docid, GetCallback<T> callback) {
        get(docid, null, callback);
    }

    /**
     * @see AsyncPouchDB#get(docid, options, callback)
     */
    public void get(String docid) {
        get(docid, null, null);
    }


    public void remove(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("remove", PouchDocumentMapper.toJson(doc), options, callback);
    }
    
    /**
     * @see AsyncPouchDB#remove(doc, options, callback)
     */
    public void remove(T doc, Map<String, Object> options) {
        remove(doc, options, null);
    }

    /**
     * @see AsyncPouchDB#remove(doc, options, callback)
     */
    public void remove(T doc, StandardCallback callback) {
        remove(doc, null, callback);
    }

    /**
     * @see AsyncPouchDB#remove(doc, options, callback)
     */
    public void remove(T doc) {
        remove(doc, null, null);
    }

    public void bulkDocs(List<T> docs, Map<String, Object> options, BulkCallback callback) {

        String mapAsJson = new StringBuilder("{\"docs\":").append(PouchDocumentMapper.toJson(docs)).append("}")
                .toString();

        loadAction("bulkDocs", mapAsJson, options, callback);
    }


    /**
     * @see AsyncPouchDB#bulkDocs(docs, options, callback)
     */
    public void bulkDocs(List<T> docs, Map<String, Object> options) {
        bulkDocs(docs, options, null);
    }

    /**
     * @see AsyncPouchDB#bulkDocs(docs, options, callback)
     */
    public void bulkDocs(List<T> docs, BulkCallback callback) {
        bulkDocs(docs, null, callback);
    }


    /**
     * @see AsyncPouchDB#bulkDocs(docs, options, callback)
     */
    public void bulkDocs(List<T> docs) {
        bulkDocs(docs, null, null);
    }

    public void allDocs(Map<String, Object> options, final AllDocsCallback<T> callback) {
        loadAction("allDocs", options, callback == null ? null : new AllDocsCallback<T>() {

            @Override
            public void onCallback(PouchError err, AllDocsInfo<T> info) {
                callback.onCallback(err, info);
            }

            @Override
            public Class<?> getGenericClass() {
                return documentClass;
            }
        });
    }


    /**
     * @see AsyncPouchDB#allDocs(options, callback)
     */
    public void allDocs(AllDocsCallback<T> callback) {
        allDocs(null, callback);
    }

    /**
     * @see AsyncPouchDB#allDocs(options, callback)
     */
    public void allDocs(boolean includeDocs, Map<String, Object> otherOptions, final AllDocsCallback<T> callback) {
        // included as a convenience method, because I'm sure otherwise people
        // will forge to set include_docs=true
        Map<String, Object> options = otherOptions != null ? otherOptions : new LinkedHashMap<String, Object>();
        options.put("include_docs", includeDocs);
        allDocs(options, callback);
    }

    /**
     * @see AsyncPouchDB#allDocs(options, callback)
     */
    public void allDocs(boolean includeDocs, AllDocsCallback<T> callback) {
        allDocs(includeDocs, null, callback);
    }


    public void replicateTo(String remoteDB, Map<String, Object> options, ReplicateCallback complete) {
        loadAction("replicate.to", JsonUtil.simpleString(remoteDB), options, complete, "complete");
    }
    
    /**
     * @see AsyncPouchDB#replicateTo(remoteDB, options, complete)
     */
    public void replicateTo(String remoteDB, boolean continuous, ReplicateCallback complete) {
        replicateTo(remoteDB, Maps.quickMap("continuous", continuous), complete);
    }
    
    /**
     * @see AsyncPouchDB#replicateTo(remoteDB, options, complete)
     */
    public void replicateTo(String remoteDB, boolean continuous) {
        replicateTo(remoteDB, Maps.quickMap("continuous", continuous), null);
    }
    
    /**
     * @see AsyncPouchDB#replicateTo(remoteDB, options, complete)
     */
    public void replicateTo(String remoteDB, ReplicateCallback complete) {
        replicateTo(remoteDB, null, complete);
    }
    
    /**
     * @see AsyncPouchDB#replicateTo(remoteDB, options, complete)
     */    
    public void replicateTo(String remoteDB) {
        replicateTo(remoteDB, null);
    }
    
    public void replicateFrom(String remoteDB, Map<String, Object> options, ReplicateCallback complete) {
        loadAction("replicate.from", JsonUtil.simpleString(remoteDB), options, complete, "complete");
    }
    
    /**
     * @see AsyncPouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB, boolean continuous, ReplicateCallback complete) {
        replicateFrom(remoteDB, Maps.quickMap("continuous", continuous), complete);
    }
    
    /**
     * @see AsyncPouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB, boolean continuous) {
        replicateFrom(remoteDB, Maps.quickMap("continuous", continuous), null);
    }

    /**
     * @see AsyncPouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB, ReplicateCallback complete) {
        replicateFrom(remoteDB, null, complete);
    }
    
    /**
     * @see AsyncPouchDB#replicateFrom(remoteDB, options, complete)
     */    
    public void replicateFrom(String remoteDB) {
        replicateFrom(remoteDB, null);
    }

    private void loadAction(String action, Map<String, Object> options, Callback<?> callback) {
        loadAction(action, null, options, callback, null);
    }

    private void loadAction(String action, String arg1, Map<String, Object> options, Callback<?> callback) {
        loadAction(action, arg1, options, callback, null);
    }

    private void loadAction(String action, String arg1, Map<String, Object> options, Callback<?> callback,
            String callbackOptionKey) {
        log.d("loadAction(%s, %s, %s, %s, %s", action, arg1, options, callback, callbackOptionKey);

        if (destroyed) {
            throw new RuntimeException("PouchDB destroyed!  Can't do any further actions.");
        }
        
        List<CharSequence> arguments = new LinkedList<CharSequence>();
        if (!TextUtils.isEmpty(arg1)) {
            arguments.add(arg1);
        }
        if (callbackOptionKey != null) {
            // callback is an option, encode it properly in the options map
            // TODO: this is hacky; do it properly
            options = options == null ? new LinkedHashMap<String, Object>()
                    : new LinkedHashMap<String, Object>(options);
            options.remove(callbackOptionKey);
            arguments.add(new StringBuilder(JsonUtil.simpleMap(options)).insert(
                    1,
                    new StringBuilder()
                            // insert after open brace
                            .append(JsonUtil.simpleString(callbackOptionKey)).append(":")
                            .append(createFunctionForCallback(callback))
                            .append(options.isEmpty() ? "" : ",")
                            ));

        } else {
            // callback is an arg, not an option in the map
            if (options != null && !options.isEmpty()) {
                arguments.add(JsonUtil.simpleMap(options));
            }
            if (callback != null) {
                arguments.add(createFunctionForCallback(callback));
            }
        }

        StringBuilder js = new StringBuilder("CouchDroid.pouchDBs[").append(id).append("].").append(action).append("(")
                .append(TextUtils.join(",", arguments)).append(");");

        runtime.loadJavascript(js);
    }

    @SuppressWarnings("rawtypes")
    private CharSequence createFunctionForCallback(final Callback innerCallback) {

        if (innerCallback == null) {
            // user doesn't give a shit
            return "function(){}";
        }

        // begin cone of death
        int callbackId = PouchJavascriptInterface.INSTANCE.addCallback(new Callback<Object>() {

            @Override
            public void onCallback(final PouchError err, final Object info) {
                Activity activity = runtime.getActivity();
                if (activity != null) {
                    // ensure it runs on the ui thread
                    activity.runOnUiThread(new Runnable() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public void run() {
                            try {
                                innerCallback.onCallback(err, info);
                            } catch (Exception e) {
                                log.e(e, "User-created callback threw an exception");
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }

            @Override
            public Object getPrimaryClass() {
                return innerCallback.getPrimaryClass();
            }

            @Override
            public Class<?> getGenericClass() {
                return innerCallback.getGenericClass();
            }
        });

        return new StringBuilder("function(err, info){PouchJavascriptInterface.callback(").append(callbackId).append(
                ", err ? JSON.stringify(err) : null, info ? JSON.stringify(info) : null);}");
    }
}
