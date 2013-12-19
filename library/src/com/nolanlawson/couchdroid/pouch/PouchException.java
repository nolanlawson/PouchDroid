package com.nolanlawson.couchdroid.pouch;

import java.io.IOException;

import com.nolanlawson.couchdroid.pouch.model.PouchError;

public class PouchException extends IOException {

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