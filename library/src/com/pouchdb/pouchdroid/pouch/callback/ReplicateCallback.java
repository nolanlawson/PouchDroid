package com.pouchdb.pouchdroid.pouch.callback;

import com.pouchdb.pouchdroid.pouch.model.ReplicateInfo;

public abstract class ReplicateCallback implements Callback<ReplicateInfo> {

    @Override
    public Object getPrimaryClass() {
        return ReplicateInfo.class;
    }

    @Override
    public Class<?> getGenericClass() {
        return null;
    }
}