package com.pouchdb.pouchdroid.pouch.callback;

import com.pouchdb.pouchdroid.pouch.model.DatabaseInfo;

public abstract class DatabaseInfoCallback implements Callback<DatabaseInfo> {
    
    @Override
    public Object getPrimaryClass() {
        return DatabaseInfo.class;
    }

    public Class<?> getGenericClass() {
        return null; // none
    }

}
