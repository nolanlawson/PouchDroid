package com.nolanlawson.couchdroid.pouch;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.type.TypeReference;

import android.app.Activity;
import android.text.TextUtils;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.util.JsonUtil;
import com.nolanlawson.couchdroid.util.Maps;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class PouchDB<T extends PouchDocumentInterface> extends AbstractPouchDB<T> {

    private static UtilLogger log = new UtilLogger(PouchDB.class);

    private static final AtomicInteger POUCH_IDS = new AtomicInteger(0);

    private int id;
    private CouchDroidRuntime runtime;
    private String name;
    private boolean destroyed = false;

    private Class<T> documentClass;
    
    private PouchDB(Class<T> documentClass, CouchDroidRuntime runtime, String name, boolean autoCompaction) {
        this.id = POUCH_IDS.incrementAndGet();
        this.documentClass = documentClass;
        this.runtime = runtime;
        this.name = name;
        
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null/empty");
        }

        runtime.loadJavascript(new StringBuilder("CouchDroid.pouchDBs[")
                .append(id).append("] = new PouchDB(")
                .append(JsonUtil.simpleMap("name", name, "adapter", "websql", "autoCompaction", autoCompaction))
                .append(");"));
    }
    
    /**
     * Creates or opens a new (asynchronous) PouchDB.
     * 
     * <br/>{@code autoCompaction} defaults to false. 
     *
     * @see PouchDB#newPouchDB(documentClass, runtime, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> PouchDB<T> newPouchDB(Class<T> documentClass, CouchDroidRuntime runtime,
            String name) {
        return newPouchDB(documentClass, runtime, name, false);
    }
    
    /**
     * Creates or opens a new synchronous PouchDB.
     * 
     * <p/><strong>Warning: synchronous calls look good in Java, but they will block the current thread! We assume
     * you're wrapping your calls in an AsyncTask.doInBackground()!</strong>
     * 
     * <p/>{@code name} defaults to the global PouchDb database name.
     * <br/>{@code autoCompaction} defaults to false. 
     *
     * @see PouchDB#newSynchronousPouchDB(documentClass, runtime, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> SynchronousPouchDB<T> newSynchronousPouchDB(Class<T> documentClass, 
            CouchDroidRuntime runtime,
            String name) {
        return newSynchronousPouchDB(documentClass, runtime, name, false);
    }

    /**
     * 
     * Creates or opens a new synchronous PouchDB.
     * 
     * <p/><strong>Warning: synchronous calls look good in Java, but they will block the current thread! We assume
     * you're wrapping your calls in an AsyncTask.doInBackground()!</strong>
     * 
     * <p/>{@code name} defaults to the global PouchDb database name.
     * <br/>{@code autoCompaction} defaults to false. 
     *
     * @see PouchDB#newPouchDB(documentClass, runtime, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> SynchronousPouchDB<T> newSynchronousPouchDB(Class<T> documentClass, 
            CouchDroidRuntime runtime,
            String name, boolean autoCompaction) {
        return new SynchronousPouchDB<T>(documentClass, runtime, name, autoCompaction);
    }
    
    /**
     * <p>
     * This method creates a database or opens an existing one. If you use a
     * <code>http://domain.com/dbname</code> then PouchDB will work as a client
     * to an online CouchDB instance, otherwise it will create a local database
     * using a backend that is present.
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong>
     * </p>
     * 
     * <ol>
     * <li>If you are also using indexedDB directly, PouchDB will use
     * <code>_pouch_</code> to prefix the internal database names, dont manually
     * create databases with the same prefix.</li>
     * <li>When acting as a client on Node any other options given will be
     * passed to <a href="https://github.com/mikeal/request">request</a>.</li>
     * </ol>
     * 
     * <ul>
     * <li><code>options.name</code>: You can omit the name argument and specify
     * it via options.</li>
     * <li><code>options.auto_compaction</code>: This turns on auto compaction
     * (experimental).</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">);</span>
     * <span class="c1">// or</span>
     * <span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;http://localhost:5984/dbname&#39;</span><span class="p">);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href='http://pouchdb.com/api.html#create_database'>http://pouchdb.com/api.html#create_database</a>
     */
    public static <T extends PouchDocumentInterface> PouchDB<T> newPouchDB(Class<T> documentClass, CouchDroidRuntime runtime,
            String name, boolean autoCompaction) {
        return new PouchDB<T>(documentClass, runtime, name, autoCompaction);
    }
    
    public String getName() {
        return name;
    }

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
     * @see PouchDB#destroy(options, callback)
     */
    public void destroy(StandardCallback callback) {
        destroy(null, callback);
    }
    
    /**
     * @see PouchDB#destroy(options, callback)
     */
    public void destroy(Map<String, Object> options) {
        destroy(options, null);
    }

    /**
     * @see PouchDB#destroy(options, callback)
     */
    public void destroy() {
        destroy(null, null);
    }    

    public void put(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("put", PouchDocumentMapper.toJson(doc), options, callback);
    }

    /**
     * @see PouchDB#put(doc, options, callback)
     */
    public void put(T doc, Map<String, Object> options) {
        put(doc, options, null);
    }

    /**
     * @see PouchDB#put(doc, options, callback)
     */
    public void put(T doc, StandardCallback callback) {
        put(doc, null, callback);
    }

    /**
     * @see PouchDB#put(doc, options, callback)
     */
    public void put(T doc) {
        put(doc, null, null);
    }

    public void post(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("post", PouchDocumentMapper.toJson(doc), options, callback);
    }

    /**
     * @see PouchDB#post(doc, options, callback)
     */
    public void post(T doc, Map<String, Object> options) {
        post(doc, options, null);
    }

    /**
     * @see PouchDB#post(doc, options, callback)
     */
    public void post(T doc, StandardCallback callback) {
        post(doc, null, callback);
    }

    /**
     * @see PouchDB#post(doc, options, callback)
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
     * @see PouchDB#get(docid, options, callback)
     */
    public void get(String docid, Map<String, Object> options) {
        get(docid, options, null);
    }

    /**
     * @see PouchDB#get(docid, options, callback)
     */
    public void get(String docid, GetCallback<T> callback) {
        get(docid, null, callback);
    }

    /**
     * @see PouchDB#get(docid, options, callback)
     */
    public void get(String docid) {
        get(docid, null, null);
    }


    public void remove(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("remove", PouchDocumentMapper.toJson(doc), options, callback);
    }
    
    /**
     * @see PouchDB#remove(doc, options, callback)
     */
    public void remove(T doc, Map<String, Object> options) {
        remove(doc, options, null);
    }

    /**
     * @see PouchDB#remove(doc, options, callback)
     */
    public void remove(T doc, StandardCallback callback) {
        remove(doc, null, callback);
    }

    /**
     * @see PouchDB#remove(doc, options, callback)
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
     * @see PouchDB#bulkDocs(docs, options, callback)
     */
    public void bulkDocs(List<T> docs, Map<String, Object> options) {
        bulkDocs(docs, options, null);
    }

    /**
     * @see PouchDB#bulkDocs(docs, options, callback)
     */
    public void bulkDocs(List<T> docs, BulkCallback callback) {
        bulkDocs(docs, null, callback);
    }


    /**
     * @see PouchDB#bulkDocs(docs, options, callback)
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
     * @see PouchDB#allDocs(options, callback)
     */
    public void allDocs(AllDocsCallback<T> callback) {
        allDocs(null, callback);
    }

    /**
     * @see PouchDB#allDocs(options, callback)
     */
    public void allDocs(boolean includeDocs, Map<String, Object> otherOptions, final AllDocsCallback<T> callback) {
        // included as a convenience method, because I'm sure otherwise people
        // will forge to set include_docs=true
        Map<String, Object> options = otherOptions != null ? otherOptions : new LinkedHashMap<String, Object>();
        options.put("include_docs", includeDocs);
        allDocs(options, callback);
    }

    /**
     * @see PouchDB#allDocs(options, callback)
     */
    public void allDocs(boolean includeDocs, AllDocsCallback<T> callback) {
        allDocs(includeDocs, null, callback);
    }


    public void replicateTo(String remoteDB, Map<String, Object> options, ReplicateCallback complete) {
        loadAction("replicate.to", JsonUtil.simpleString(remoteDB), options, complete, "complete");
    }
    
    /**
     * @see PouchDB#replicateTo(remoteDB, options, complete)
     */
    public void replicateTo(String remoteDB, boolean continuous, ReplicateCallback complete) {
        replicateTo(remoteDB, Maps.quickMap("continuous", continuous), complete);
    }
    
    /**
     * @see PouchDB#replicateTo(remoteDB, options, complete)
     */
    public void replicateTo(String remoteDB, boolean continuous) {
        replicateTo(remoteDB, Maps.quickMap("continuous", continuous), null);
    }
    
    /**
     * @see PouchDB#replicateTo(remoteDB, options, complete)
     */
    public void replicateTo(String remoteDB, ReplicateCallback complete) {
        replicateTo(remoteDB, null, complete);
    }
    
    /**
     * @see PouchDB#replicateTo(remoteDB, options, complete)
     */    
    public void replicateTo(String remoteDB) {
        replicateTo(remoteDB, null);
    }
    
    public void replicateFrom(String remoteDB, Map<String, Object> options, ReplicateCallback complete) {
        loadAction("replicate.from", JsonUtil.simpleString(remoteDB), options, complete, "complete");
    }
    
    /**
     * @see PouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB, boolean continuous, ReplicateCallback complete) {
        replicateFrom(remoteDB, Maps.quickMap("continuous", continuous), complete);
    }
    
    /**
     * @see PouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB, boolean continuous) {
        replicateFrom(remoteDB, Maps.quickMap("continuous", continuous), null);
    }

    /**
     * @see PouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB, ReplicateCallback complete) {
        replicateFrom(remoteDB, null, complete);
    }
    
    /**
     * @see PouchDB#replicateFrom(remoteDB, options, complete)
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

    /**
     * A generic callback for interacting with PouchDB.
     * 
     * @author nolan
     * 
     */
    public static interface Callback<E> {

        /**
         * Callback method, which runs on the UI thread.
         * 
         * @param err
         *            if null, there was no error
         * @param info
         *            contains additional information given by PouchDB
         */
        public void onCallback(PouchError err, E info);

        public Object getPrimaryClass();

        public Class<?> getGenericClass();
    }

    public static abstract class BulkCallback implements Callback<List<PouchInfo>> {
        public Object getPrimaryClass() {

            return new TypeReference<List<PouchInfo>>() {
            };
        }

        public Class<?> getGenericClass() {
            return null;
        }
    }

    public static abstract class AllDocsCallback<T extends PouchDocumentInterface> implements Callback<AllDocsInfo<T>> {

        @Override
        public Object getPrimaryClass() {
            return AllDocsInfo.class;
        }

        public Class<?> getGenericClass() {
            return null; // overridden
        }

    }

    public static abstract class StandardCallback implements Callback<PouchInfo> {
        public Object getPrimaryClass() {

            return PouchInfo.class;
        }

        public Class<?> getGenericClass() {
            return null;
        }
    }

    public static abstract class GetCallback<T> implements Callback<T> {

        public GetCallback() {
        }

        public Object getPrimaryClass() {
            return null; // overridden
        }

        public Class<?> getGenericClass() {
            return null;
        }
    }

    public static abstract class ReplicateCallback implements Callback<ReplicateInfo> {

        @Override
        public Object getPrimaryClass() {
            return ReplicateInfo.class;
        }

        @Override
        public Class<?> getGenericClass() {
            return null;
        }
    }
}
