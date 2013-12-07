package com.nolanlawson.couchdroid.pouch;


/**
 * Represents a document to be stored in PouchDB.
 * 
 * Extend this for any class you'd like to place in a PouchDB database.
 * 
 * @author nolan
 *
 */
public abstract class PouchDocument implements PouchDocumentInterface {

    private String pouchId;
    private String pouchRev;
    
    @Override
    public String getPouchId() {
        return pouchId;
    }
    
    @Override
    public void setPouchId(String pouchId) {
        this.pouchId = pouchId;
    }
    
    @Override
    public String getPouchRev() {
        return pouchRev;
    }
    
    @Override
    public void setPouchRev(String pouchRev) {
        this.pouchRev = pouchRev;
    }
}
