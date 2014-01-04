package com.pouchdb.pouchdroid.pouch;

import java.util.HashMap;
import java.util.Map;


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
    private Map<String, PouchAttachment> pouchAttachments;
    
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

    @Override
    public Map<String, PouchAttachment> getPouchAttachments() {
        return pouchAttachments;
    }

    @Override
    public void setPouchAttachments(Map<String, PouchAttachment> pouchAttachments) {
        this.pouchAttachments = pouchAttachments;
    }
    
    /**
     * Convenience method for adding pouch attachments to this document.  This simply does a null-check
     * on the pouchAttachments map and calls put() on that map.
     * 
     * @param filename
     * @param pouchAttachment
     */
    public void addPouchAttachment(String filename, PouchAttachment pouchAttachment) {
        pouchAttachments = (pouchAttachments == null) ? new HashMap<String, PouchAttachment>() : pouchAttachments;
        pouchAttachments.put(filename, pouchAttachment);
    }
}
