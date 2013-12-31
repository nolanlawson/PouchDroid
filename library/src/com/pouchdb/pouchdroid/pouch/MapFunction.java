package com.pouchdb.pouchdroid.pouch;


public abstract class MapFunction<T extends PouchDocumentInterface> {

    private EmitListener listener;
    
    public abstract void map(T doc);
    
    protected void emit(Object key, Object value) {
        if (listener != null) {
            listener.onEmit(key, value);
        }
    }
    
    /* package */ void map(T doc, EmitListener listener) {
        this.listener = listener;
        map(doc);
        this.listener = null;
    }
    
    /* package */ static interface EmitListener {
        public void onEmit(Object key, Object value);
    }
}
