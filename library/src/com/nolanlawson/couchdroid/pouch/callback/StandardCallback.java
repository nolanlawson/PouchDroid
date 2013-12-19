package com.nolanlawson.couchdroid.pouch.callback;

import com.nolanlawson.couchdroid.pouch.model.PouchInfo;

public abstract class StandardCallback implements Callback<PouchInfo> {
    public Object getPrimaryClass() {

        return PouchInfo.class;
    }

    public Class<?> getGenericClass() {
        return null;
    }
}