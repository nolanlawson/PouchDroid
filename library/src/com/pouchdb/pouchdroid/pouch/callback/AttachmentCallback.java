package com.pouchdb.pouchdroid.pouch.callback;

import com.pouchdb.pouchdroid.pouch.PouchAttachment;

public abstract class AttachmentCallback implements Callback<PouchAttachment> {
    
    public Object getPrimaryClass() {

        return PouchAttachment.class;
    }

    public Class<?> getGenericClass() {
        return null;
    }
}