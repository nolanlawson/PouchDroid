package com.nolanlawson.couchdroid.pouch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.pouch.PouchDB.AllDocsCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.BulkCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.GetCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.ReplicateCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.StandardCallback;
import com.nolanlawson.couchdroid.util.Maps;

public class SynchronousPouchDB<T extends PouchDocumentInterface> {

    private PouchDB<T> delegate;

    /* package */ SynchronousPouchDB(Class<T> documentClass, CouchDroidRuntime runtime, String name,
            boolean autoCompaction) {
        delegate = PouchDB.newPouchDB(documentClass, runtime, name, autoCompaction);
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
        return new ArrayBlockingQueue<PouchResponse<T>>(1);
    }
    
    private static <T> T waitAndReturn(BlockingQueue<PouchResponse<T>> lock) throws PouchException {
        PouchResponse<T> response;
        try {
            response = lock.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new PouchException("interrupted");
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