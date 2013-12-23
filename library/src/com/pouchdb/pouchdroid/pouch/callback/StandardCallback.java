package com.pouchdb.pouchdroid.pouch.callback;

import com.pouchdb.pouchdroid.pouch.model.PouchInfo;

public abstract class StandardCallback implements Callback<PouchInfo> {
    public Object getPrimaryClass() {

        return PouchInfo.class;
    }

    public Class<?> getGenericClass() {
        return null;
    }
}