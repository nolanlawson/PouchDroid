package com.pouchdb.pouchdroid.pouch;

import com.pouchdb.pouchdroid.pouch.model.PouchError;

public class PouchException extends RuntimeException {

    private static final long serialVersionUID = -754998157801386220L;

    private PouchError pouchError;
    
    public PouchException(PouchError pouchError) {
        super(pouchError.toString());
        this.pouchError = pouchError;
    }

    public PouchException(String detailMessage) {
        super(detailMessage);
    }

    public PouchError getPouchError() {
        return pouchError;
    }
}