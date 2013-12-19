package com.nolanlawson.couchdroid.pouch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Looper;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.pouch.callback.AllDocsCallback;
import com.nolanlawson.couchdroid.pouch.callback.BulkCallback;
import com.nolanlawson.couchdroid.pouch.callback.GetCallback;
import com.nolanlawson.couchdroid.pouch.callback.ReplicateCallback;
import com.nolanlawson.couchdroid.pouch.callback.StandardCallback;
import com.nolanlawson.couchdroid.pouch.model.AllDocsInfo;
import com.nolanlawson.couchdroid.pouch.model.PouchError;
import com.nolanlawson.couchdroid.pouch.model.PouchInfo;
import com.nolanlawson.couchdroid.pouch.model.ReplicateInfo;
import com.nolanlawson.couchdroid.util.Maps;

public class PouchDB<T extends PouchDocumentInterface> {

    private AsyncPouchDB<T> delegate;

    /* package */ PouchDB(Class<T> documentClass, CouchDroidRuntime runtime, String name,
            boolean autoCompaction) {
        delegate = new AsyncPouchDB<T>(documentClass, runtime, name, autoCompaction);
    }
    
    /**
     * Creates or opens a new (asynchronous) PouchDB.
     * 
     * <br/>{@code autoCompaction} defaults to false. 
     *
     * @see AsyncPouchDB#newPouchDB(documentClass, runtime, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> AsyncPouchDB<T> newAsyncPouchDB(Class<T> documentClass, CouchDroidRuntime runtime,
            String name) {
        return newAsyncPouchDB(documentClass, runtime, name, false);
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
     * @see AsyncPouchDB#newSynchronousPouchDB(documentClass, runtime, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> PouchDB<T> newPouchDB(Class<T> documentClass, 
            CouchDroidRuntime runtime,
            String name) {
        return newPouchDB(documentClass, runtime, name, false);
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
     * @see AsyncPouchDB#newPouchDB(documentClass, runtime, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> PouchDB<T> newPouchDB(Class<T> documentClass, 
            CouchDroidRuntime runtime,
            String name, boolean autoCompaction) {
        return new PouchDB<T>(documentClass, runtime, name, autoCompaction);
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
    public static <T extends PouchDocumentInterface> AsyncPouchDB<T> newAsyncPouchDB(Class<T> documentClass, CouchDroidRuntime runtime,
            String name, boolean autoCompaction) {
        return new AsyncPouchDB<T>(documentClass, runtime, name, autoCompaction);
    }
    
    public String getName() {
        return delegate.getName();
    }

    public boolean isDestroyed() {
        return delegate.isDestroyed();
    }

    public PouchInfo destroy(Map<String, Object> options) throws PouchException {
        BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.destroy(options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    public PouchInfo destroy() throws PouchException {
        return destroy(null);
    }

    public PouchInfo put(T doc, Map<String, Object> options) throws PouchException {
        BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.put(doc, options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    public PouchInfo put(T doc) throws PouchException {
        return put(doc, null);
    }

    public PouchInfo post(T doc, Map<String, Object> options) throws PouchException {
        BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.post(doc, options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    public PouchInfo post(T doc) throws PouchException {
        return post(doc, null);
    }

    public T get(String docid, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<T>> lock = createLock();

        delegate.get(docid, options, new GetCallback<T>() {

            @Override
            public void onCallback(PouchError err, T info) {

                lock.offer(new PouchResponse<T>(err, info));
            }
        });
        return waitAndReturn(lock);
    }

    public T get(String docid) throws PouchException {
        return get(docid, null);
    }

    public PouchInfo remove(T doc, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.remove(doc, options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    public PouchInfo remove(T doc) throws PouchException {
        return remove(doc, null);
    }

    public List<PouchInfo> bulkDocs(List<T> docs, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<List<PouchInfo>>> lock = createLock();

        delegate.bulkDocs(docs, options, new BulkCallback() {

            @Override
            public void onCallback(PouchError err, List<PouchInfo> info) {

                lock.offer(new PouchResponse<List<PouchInfo>>(err, info));
            }
        });
        return waitAndReturn(lock);
    }

    public List<PouchInfo> bulkDocs(List<T> docs) throws PouchException {
        return bulkDocs(docs, null);
    }

    public AllDocsInfo<T> allDocs(Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<AllDocsInfo<T>>> lock = createLock();

        delegate.allDocs(options, new AllDocsCallback<T>() {

            @Override
            public void onCallback(PouchError err, AllDocsInfo<T> info) {

                lock.offer(new PouchResponse<AllDocsInfo<T>>(err, info));
            }
        });
        return waitAndReturn(lock);
    }

    public AllDocsInfo<T> allDocs() throws PouchException {
        return allDocs(null);
    }
    
    public AllDocsInfo<T> allDocs(boolean includeDocs) throws PouchException {
        return allDocs(Maps.quickMap("include_docs", includeDocs));
    }

    public ReplicateInfo replicateTo(String remoteDB, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<ReplicateInfo>> lock = createLock();

        delegate.replicateTo(remoteDB, options, new ReplicateCallback() {

            @Override
            public void onCallback(PouchError err, ReplicateInfo info) {

                lock.offer(new PouchResponse<ReplicateInfo>(err, info));
            }
        });
        return waitAndReturn(lock);
    }

    public ReplicateInfo replicateTo(String remoteDB) throws PouchException {
        return replicateTo(remoteDB, null);
    }
    
    public ReplicateInfo replicateTo(String remoteDB, boolean continuous) throws PouchException {
        return replicateTo(remoteDB, Maps.quickMap("continuous", continuous));
    }

    public ReplicateInfo replicateFrom(String remoteDB, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<ReplicateInfo>> lock = createLock();

        delegate.replicateFrom(remoteDB, options, new ReplicateCallback() {

            @Override
            public void onCallback(PouchError err, ReplicateInfo info) {

                lock.offer(new PouchResponse<ReplicateInfo>(err, info));
            }
        });
        return waitAndReturn(lock);
    }

    public ReplicateInfo replicateFrom(String remoteDB) throws PouchException {
        return replicateFrom(remoteDB, null);
    }
    
    public ReplicateInfo replicateFrom(String remoteDB, boolean continuous) throws PouchException {
        return replicateFrom(remoteDB, Maps.quickMap("continuous", continuous));
    }
    
    private StandardCallback createStandardCallback(final BlockingQueue<PouchResponse<PouchInfo>> lock) {
        return new StandardCallback() {

            @Override
            public void onCallback(PouchError err, PouchInfo info) {
                lock.offer(new PouchResponse<PouchInfo>(err, info));
            }
        };
    }
    
    private static <T> BlockingQueue<PouchResponse<T>> createLock() {
        
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            // on UI thread
            throw new IllegalStateException("PouchDB cannot be called from the UI thread, because it will block! " +
            		"Wrap your code in an AsyncTask.doInBackground().");
        }
        
        return new ArrayBlockingQueue<PouchResponse<T>>(1);
    }
    
    private static <T> T waitAndReturn(BlockingQueue<PouchResponse<T>> lock) throws PouchException {
        PouchResponse<T> response;
        try {
            response = lock.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new PouchException(new PouchError(500, "interrupted", e.getMessage()));
        }

        if (response.err != null) {
            throw new PouchException(response.err);
        }
        return response.response;
    }

    private static class PouchResponse<T> {

        PouchResponse(PouchError err, T response) {
            this.err = err;
            this.response = response;
        }

        private PouchError err;
        private T response;
    }
}