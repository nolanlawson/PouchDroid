package com.nolanlawson.couchdroid.pouch.callback;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.nolanlawson.couchdroid.pouch.model.PouchInfo;

public abstract class BulkCallback implements Callback<List<PouchInfo>> {
    public Object getPrimaryClass() {

        return new TypeReference<List<PouchInfo>>() {
        };
    }

    public Class<?> getGenericClass() {
        return null;
    }
}