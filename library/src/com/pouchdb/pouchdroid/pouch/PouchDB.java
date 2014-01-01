package com.pouchdb.pouchdroid.pouch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Looper;

import com.pouchdb.pouchdroid.PouchDroid;
import com.pouchdb.pouchdroid.pouch.callback.AllDocsCallback;
import com.pouchdb.pouchdroid.pouch.callback.BulkCallback;
import com.pouchdb.pouchdroid.pouch.callback.DatabaseInfoCallback;
import com.pouchdb.pouchdroid.pouch.callback.GetCallback;
import com.pouchdb.pouchdroid.pouch.callback.ReplicateCallback;
import com.pouchdb.pouchdroid.pouch.callback.StandardCallback;
import com.pouchdb.pouchdroid.pouch.model.AllDocsInfo;
import com.pouchdb.pouchdroid.pouch.model.DatabaseInfo;
import com.pouchdb.pouchdroid.pouch.model.MapFunction;
import com.pouchdb.pouchdroid.pouch.model.PouchError;
import com.pouchdb.pouchdroid.pouch.model.PouchInfo;
import com.pouchdb.pouchdroid.pouch.model.ReduceFunction;
import com.pouchdb.pouchdroid.util.PouchOptions;

public class PouchDB<T extends PouchDocumentInterface> {

    private AsyncPouchDB<T> delegate;

    /*
     *******************************************
     * Constructors
     *******************************************
     */    
    
    /* package */ PouchDB(Class<T> documentClass, PouchDroid pouchDroid, String name,
            boolean autoCompaction) {
        delegate = new AsyncPouchDB<T>(documentClass, pouchDroid, name, autoCompaction);
    }
    
    /**
     * Creates or opens a new (asynchronous) PouchDB.
     * 
     * <br/>{@code autoCompaction} defaults to false. 
     *
     * @see AsyncPouchDB#newPouchDB(documentClass, pouchDroid, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> AsyncPouchDB<T> newAsyncPouchDB(Class<T> documentClass, PouchDroid pouchDroid,
            String name) {
        return newAsyncPouchDB(documentClass, pouchDroid, name, false);
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
     * @see AsyncPouchDB#newSynchronousPouchDB(documentClass, pouchDroid, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> PouchDB<T> newPouchDB(Class<T> documentClass, 
            PouchDroid pouchDroid,
            String name) {
        return newPouchDB(documentClass, pouchDroid, name, false);
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
     * @see AsyncPouchDB#newPouchDB(documentClass, pouchDroid, name, autoCompaction)
     */
    public static <T extends PouchDocumentInterface> PouchDB<T> newPouchDB(Class<T> documentClass, 
            PouchDroid pouchDroid,
            String name, boolean autoCompaction) {
        return new PouchDB<T>(documentClass, pouchDroid, name, autoCompaction);
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
    public static <T extends PouchDocumentInterface> AsyncPouchDB<T> newAsyncPouchDB(Class<T> documentClass, PouchDroid pouchDroid,
            String name, boolean autoCompaction) {
        return new AsyncPouchDB<T>(documentClass, pouchDroid, name, autoCompaction);
    }
    
    /*
     *******************************************
     * Public methods
     *******************************************
     */
    
    /**
     * @see AsyncPouchDB#getName()
     */
    public String getName() {
        return delegate.getName();
    }
    
    /**
     * @see AsyncPouchDB#isDestroyed()
     */
    public boolean isDestroyed() {
        return delegate.isDestroyed();
    }
    
    /**
     * Returns the underlying AsyncPouchDB, in case you want to do some asynchronous calls as well.
     * @return
     */
    public AsyncPouchDB<T> getAsyncPouchDB() {
        return delegate;
    }
    
    /*
     *******************************************
     * Public overrides (implicit or otherwise)
     *******************************************
     */
    /**
     * @see AsyncPouchDB#destroy(options, callback)
     */
    public PouchInfo destroy(Map<String, Object> options) throws PouchException {
        BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.destroy(options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    /**
     * @see AsyncPouchDB#destroy(options, callback)
     */
    public PouchInfo destroy() throws PouchException {
        return destroy(null);
    }

    /**
     * @see AsyncPouchDB#put(doc, options, callback)
     */
    public PouchInfo put(T doc, Map<String, Object> options) throws PouchException {
        BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.put(doc, options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    /**
     * @see AsyncPouchDB#put(doc, options, callback)
     */
    public PouchInfo put(T doc) throws PouchException {
        return put(doc, null);
    }

    /**
     * @see AsyncPouchDB#post(doc, options, callback)
     */
    public PouchInfo post(T doc, Map<String, Object> options) throws PouchException {
        BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.post(doc, options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    /**
     * @see AsyncPouchDB#post(doc, options, callback)
     */
    public PouchInfo post(T doc) throws PouchException {
        return post(doc, null);
    }

    /**
     * @see AsyncPouchDB#get(docid, options, callback)
     */
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

    /**
     * @see AsyncPouchDB#get(docid, options, callback)
     */
    public T get(String docid) throws PouchException {
        return get(docid, null);
    }

    /**
     * @see AsyncPouchDB#remove(doc, options, callback)
     */
    public PouchInfo remove(T doc, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<PouchInfo>> lock = createLock();
        delegate.remove(doc, options, createStandardCallback(lock));
        return waitAndReturn(lock);
    }

    /**
     * @see AsyncPouchDB#remove(doc, options, callback)
     */
    public PouchInfo remove(T doc) throws PouchException {
        return remove(doc, null);
    }

    /**
     * @see AsyncPouchDB#bulkDocs(docs, options, callback)
     */
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

    /**
     * @see AsyncPouchDB#bulkDocs(docs, options, callback)
     */
    public List<PouchInfo> bulkDocs(List<T> docs) throws PouchException {
        return bulkDocs(docs, null);
    }

    /**
     * @see AsyncPouchDB#allDocs(includeDocs, options)
     */
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

    /**
     * @see AsyncPouchDB#allDocs(includeDocs, options)
     */
    public AllDocsInfo<T> allDocs() throws PouchException {
        return allDocs(null);
    }
    
    /**
     * @see AsyncPouchDB#allDocs(includeDocs, options)
     */
    public AllDocsInfo<T> allDocs(boolean includeDocs) throws PouchException {
        return allDocs(PouchOptions.from(PouchOptions.INCLUDE_DOCS, includeDocs));
    }
    
    /**
     * @see AsyncPouchDB#allDocs(includeDocs, options)
     */
    public AllDocsInfo<T> allDocs(boolean includeDocs, List<String> keys) throws PouchException {
        return allDocs(PouchOptions.from(PouchOptions.INCLUDE_DOCS, includeDocs, "keys", keys));
    }
    
    /**
     * 
     * Replicates to the remote database.
     * 
     * <p/>Note that this call returns when the request is <em>sent</em>.  The request
     * may or may not be fulfilled by the remote CouchDB; use the Async api's "complete" listener to verify
     * replication.
     * 
     * @see AsyncPouchDB#replicateTo(remoteDB, options, complete)
     */    
    public void replicateTo(String remoteDB, Map<String, Object> options) throws PouchException {
        delegate.replicateTo(remoteDB, options, (ReplicateCallback)null);
        // replication cannot block, because we can't be 100% sure that "complete" will be called back
        // (even if it's not continuous)
        // TODO: this is unexpected
    }
    
    /**
     * 
     * Replicates to the remote database.
     * 
     * <p/>Note that this call returns when the request is <em>sent</em>.  The request
     * may or may not be fulfilled by the remote CouchDB; use the Async api's "complete" listener to verify
     * replication.
     * 
     * @see AsyncPouchDB#replicateTo(remoteDB, options, complete)
     */    
    public void replicateTo(String remoteDB) throws PouchException {
        replicateTo(remoteDB, null);
    }
    
    /**
     * 
     * Replicates to the remote database.
     * 
     * <p/>Note that this call returns when the request is <em>sent</em>.  The request
     * may or may not be fulfilled by the remote CouchDB; use the Async api's "complete" listener to verify
     * replication.
     * 
     * @see AsyncPouchDB#replicateTo(remoteDB, options, complete)
     */    
    public void replicateTo(String remoteDB, boolean continuous) throws PouchException {
        replicateTo(remoteDB, PouchOptions.from(PouchOptions.CONTINUOUS, continuous));
    }

    /**
     * 
     * Replicates from the remote database.
     * 
     * <p/>Note that this call returns when the request is <em>sent</em>.  The request
     * may or may not be fulfilled by the remote CouchDB; use the Async api's "complete" listener to verify
     * replication.
     * 
     * @see AsyncPouchDB#replicateFrom(remoteDB, options, complete)
     */
    
    public void replicateFrom(String remoteDB, Map<String, Object> options) throws PouchException {
        delegate.replicateFrom(remoteDB, options, (ReplicateCallback)null);
        // replication cannot block, because we can't be 100% sure that "complete" will be called back
        // (even if it's not continuous)
        // TODO: this is unexpected
    }
    
    /**
     * 
     * Replicates from the remote database.
     * 
     * <p/>Note that this call returns when the request is <em>sent</em>.  The request
     * may or may not be fulfilled by the remote CouchDB; use the Async api's "complete" listener to verify
     * replication.
     * 
     * @see AsyncPouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB) throws PouchException {
        replicateFrom(remoteDB, null);
    }
    
    /**
     * 
     * Replicates from the remote database.
     * 
     * <p/>Note that this call returns when the request is <em>sent</em>.  The request
     * may or may not be fulfilled by the remote CouchDB; use the Async api's "complete" listener to verify
     * replication.
     * 
     * @see AsyncPouchDB#replicateFrom(remoteDB, options, complete)
     */
    public void replicateFrom(String remoteDB, boolean continuous) throws PouchException {
        replicateFrom(remoteDB, PouchOptions.from(PouchOptions.CONTINUOUS, continuous));
    }
    
    /**
     * 
     * @see AsyncPouchDB#info(DatabaseInfoCallback)
     */
    public DatabaseInfo info() {
        final BlockingQueue<PouchResponse<DatabaseInfo>> lock = createLock();

        delegate.info(new DatabaseInfoCallback() {

            @Override
            public void onCallback(PouchError err, DatabaseInfo info) {

                lock.offer(new PouchResponse<DatabaseInfo>(err, info));
            }
        });
        return waitAndReturn(lock);        
    }
    
    /**
     * @see AsyncPouchDB#query(mapFunction, ReduceFunction, Map, AllDocsCallback)
     */
    public AllDocsInfo<T> query(MapFunction mapFunction, ReduceFunction reduceFunction, 
            Map<String, Object> options) {
        final BlockingQueue<PouchResponse<AllDocsInfo<T>>> lock = createLock();

        delegate.query(mapFunction, reduceFunction, options, new AllDocsCallback<T>() {

            @Override
            public void onCallback(PouchError err, AllDocsInfo<T> info) {

                lock.offer(new PouchResponse<AllDocsInfo<T>>(err, info));
            }
        });
        return waitAndReturn(lock);                
    }
    
    /**
     * @see AsyncPouchDB#query(mapFunction, ReduceFunction, Map, AllDocsCallback)
     */
    public AllDocsInfo<T> query(MapFunction mapFunction, Map<String, Object> options) {
        return query(mapFunction, null, options);
    }
    
    /**
     * @see AsyncPouchDB#query(mapFunction, ReduceFunction, Map, AllDocsCallback)
     */
    public AllDocsInfo<T> query(MapFunction mapFunction, ReduceFunction reduceFunction) {
        return query(mapFunction, reduceFunction, null);
    }
    
    /**
     * @see AsyncPouchDB#query(mapFunction, ReduceFunction, Map, AllDocsCallback)
     */
    public AllDocsInfo<T> query(MapFunction mapFunction) {
        return query(mapFunction, null, null);
    }
    
    /*
     *******************************************
     * Private methods
     *******************************************
     */
    
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