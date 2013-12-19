package com.nolanlawson.couchdroid.pouch.callback;

import com.nolanlawson.couchdroid.pouch.model.ReplicateInfo;

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