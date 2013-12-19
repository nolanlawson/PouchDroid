package com.nolanlawson.couchdroid.pouch.callback;

import com.nolanlawson.couchdroid.pouch.PouchDocumentInterface;
import com.nolanlawson.couchdroid.pouch.model.AllDocsInfo;

public abstract class AllDocsCallback<T extends PouchDocumentInterface> implements Callback<AllDocsInfo<T>> {

    @Override
    public Object getPrimaryClass() {
        return AllDocsInfo.class;
    }

    public Class<?> getGenericClass() {
        return null; // overridden
    }

}