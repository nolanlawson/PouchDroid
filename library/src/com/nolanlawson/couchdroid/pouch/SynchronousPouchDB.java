package com.nolanlawson.couchdroid.pouch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.pouch.PouchDB.AllDocsCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.BulkCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.GetCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.ReplicateCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.StandardCallback;

public class SynchronousPouchDB<T extends PouchDocumentInterface> {

    private PouchDB<T> delegate;

    /* package */SynchronousPouchDB(Class<T> documentClass, CouchDroidRuntime runtime, String name,
            boolean autoCompaction) {
        delegate = PouchDB.newPouchDB(documentClass, runtime, name, autoCompaction);
    }

    public PouchInfo destroy(Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<PouchInfo>> lock = new LinkedBlockingQueue<PouchResponse<PouchInfo>>();
        
        delegate.destroy(options, new StandardCallback() {

            @Override
            public void onCallback(PouchError err, PouchInfo info) {
                
                try {
                    lock.put(new PouchResponse<PouchInfo>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<PouchInfo> response;
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

    public PouchInfo destroy() throws PouchException {
        return destroy(null);
    }

    public PouchInfo put(T doc, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<PouchInfo>> lock = new LinkedBlockingQueue<PouchResponse<PouchInfo>>();
        
        delegate.put(doc, options, new StandardCallback() {

            @Override
            public void onCallback(PouchError err, PouchInfo info) {
                
                try {
                    lock.put(new PouchResponse<PouchInfo>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<PouchInfo> response;
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

    public PouchInfo put(T doc) throws PouchException {
        return put(doc, null);
    }

    public PouchInfo post(T doc, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<PouchInfo>> lock = new LinkedBlockingQueue<PouchResponse<PouchInfo>>();
        
        delegate.post(doc, options, new StandardCallback() {

            @Override
            public void onCallback(PouchError err, PouchInfo info) {
                
                try {
                    lock.put(new PouchResponse<PouchInfo>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<PouchInfo> response;
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

    public PouchInfo post(T doc) throws PouchException {
        return post(doc, null);
    }

    public T get(String docid, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<T>> lock = new LinkedBlockingQueue<PouchResponse<T>>();
        
        delegate.get(docid, options, new GetCallback<T>() {

            @Override
            public void onCallback(PouchError err, T info) {
                
                try {
                    lock.put(new PouchResponse<T>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
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

    public T get(String docid) throws PouchException {
        return get(docid, null);
    }

    public PouchInfo remove(T doc, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<PouchInfo>> lock = new LinkedBlockingQueue<PouchResponse<PouchInfo>>();
        
        delegate.remove(doc, options, new StandardCallback() {

            @Override
            public void onCallback(PouchError err, PouchInfo info) {
                
                try {
                    lock.put(new PouchResponse<PouchInfo>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<PouchInfo> response;
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

    public PouchInfo remove(T doc) throws PouchException {
        return remove(doc, null);
    }

    public List<PouchInfo> bulkDocs(List<T> docs, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<List<PouchInfo>>> lock = new LinkedBlockingQueue<PouchResponse<List<PouchInfo>>>();
        
        delegate.bulkDocs(docs, options, new BulkCallback() {

            @Override
            public void onCallback(PouchError err, List<PouchInfo> info) {
                
                try {
                    lock.put(new PouchResponse<List<PouchInfo>>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<List<PouchInfo>> response;
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

    public List<PouchInfo> bulkDocs(List<T> docs) throws PouchException {
        return bulkDocs(docs, null);
    }

    public AllDocsInfo<T> allDocs(Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<AllDocsInfo<T>>> lock = new LinkedBlockingQueue<PouchResponse<AllDocsInfo<T>>>();
        
        delegate.allDocs(options, new AllDocsCallback<T>() {

            @Override
            public void onCallback(PouchError err, AllDocsInfo<T> info) {
                
                try {
                    lock.put(new PouchResponse<AllDocsInfo<T>>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<AllDocsInfo<T>> response;
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

    public AllDocsInfo<T> allDocs() throws PouchException {
        return allDocs(null);
    }

    public ReplicateInfo replicateTo(String remoteDB, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<ReplicateInfo>> lock = new LinkedBlockingQueue<PouchResponse<ReplicateInfo>>();
        
        delegate.replicateTo(remoteDB, options, new ReplicateCallback() {

            @Override
            public void onCallback(PouchError err, ReplicateInfo info) {
                
                try {
                    lock.put(new PouchResponse<ReplicateInfo>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<ReplicateInfo> response;
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

    public ReplicateInfo replicateTo(String remoteDB) throws PouchException {
        return replicateTo(remoteDB, null);
    }

    public ReplicateInfo replicateFrom(String remoteDB, Map<String, Object> options) throws PouchException {
        final BlockingQueue<PouchResponse<ReplicateInfo>> lock = new LinkedBlockingQueue<PouchResponse<ReplicateInfo>>();
        
        delegate.replicateFrom(remoteDB, options, new ReplicateCallback() {

            @Override
            public void onCallback(PouchError err, ReplicateInfo info) {
                
                try {
                    lock.put(new PouchResponse<ReplicateInfo>(err, info));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(new PouchException("interrupted"));
                }
            }
        });
        PouchResponse<ReplicateInfo> response;
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

    public ReplicateInfo replicateFrom(String remoteDB) throws PouchException {
        return replicateFrom(remoteDB, null);
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
