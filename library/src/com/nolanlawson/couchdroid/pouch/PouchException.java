package com.nolanlawson.couchdroid.pouch;

import java.io.IOException;

public class PouchException extends IOException {

    private static final long serialVersionUID = -754998157801386220L;

    public PouchException() {
        super();
    }

    public PouchException(String detailMessage) {
        super(detailMessage);
    }
}