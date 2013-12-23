package com.pouchdb.pouchdroid.pouch.callback;

import com.pouchdb.pouchdroid.pouch.PouchDocumentInterface;
import com.pouchdb.pouchdroid.pouch.model.AllDocsInfo;

public abstract class AllDocsCallback<T extends PouchDocumentInterface> implements Callback<AllDocsInfo<T>> {

    @Override
    public Object getPrimaryClass() {
        return AllDocsInfo.class;
    }

    public Class<?> getGenericClass() {
        return null; // overridden
    }

}