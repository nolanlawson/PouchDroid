package com.pouchdb.pouchdroid.pouch.callback;

import com.pouchdb.pouchdroid.pouch.model.PouchError;

/**
 * A generic callback for interacting with PouchDB.
 * 
 * @author nolan
 * 
 */
public interface Callback<E> {

    /**
     * Callback method, which runs on the UI thread.
     * 
     * @param err
     *            if null, there was no error
     * @param info
     *            contains additional information given by PouchDB
     */
    public void onCallback(PouchError err, E info);

    public Object getPrimaryClass();

    public Class<?> getGenericClass();
}