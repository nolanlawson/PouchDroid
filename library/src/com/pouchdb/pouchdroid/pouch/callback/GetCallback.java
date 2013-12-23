package com.pouchdb.pouchdroid.pouch.callback;

public abstract class GetCallback<T> implements Callback<T> {

    public GetCallback() {
    }

    public Object getPrimaryClass() {
        return null; // overridden
    }

    public Class<?> getGenericClass() {
        return null;
    }
}