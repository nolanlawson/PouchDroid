package com.pouchdb.pouchdroid.pouch;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Describes a document to be placed in a PouchDB database.  Implement this if, for whatever reason,
 * you can't extend AbsPouchDocument.
 * @author nolan
 *
 */
public interface PouchDocumentInterface {

    /**
     * Returns a string representing the ID of this document in a particular PouchDB database.  This corresponds to the
     * "_id" field of a PouchDB/CouchDB document.
     * 
     * See the PouchDB/CouchDB docs for more details.
     * 
     * @see <a href='https://wiki.apache.org/couchdb/HTTP_Document_API#Naming.2FAddressing'>
     *      https://wiki.apache.org/couchdb/HTTP_Document_API#Naming.2FAddressing
     *      </a> 
     * @return
     */
    @JsonProperty("_id")
    public String getPouchId();
    
    /**
     * Set the document id for this document.  Assumed to be called during JSON parsing.
     * @param pouchRev
     */    
    @JsonProperty("_id")
    public void setPouchId(String pouchId);
    
    /**
     * 
     * Returns a string representing the revision of this document in a particular PouchDB database.  This corresponds
     * to the "_rev" field of a PouchDB/PouchDB document.
     * 
     * See the PouchDB/CouchDB docs for more details.
     * 
     * @see <a href='https://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields'>
     *      https://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields</a>
     */
    @JsonProperty("_rev")
    public String getPouchRev();
    
    /**
     * Set the revision id for this document.  Assumed to be called during JSON parsing.
     * @param pouchRev
     */
    @JsonProperty("_rev")
    public void setPouchRev(String pouchRev);
    
    /**
     * Get a map of filenames to pouch attachments for this document.  This is null unless you set
     * attachments=true and are actually using attachments.
     * @return
     */
    @JsonProperty("_attachments")
    public Map<String, PouchAttachment> getPouchAttachments();
    
    /**
     * Set the map of filenames to pouch attachments for this document.
     * @param pouchAttachments
     */
    @JsonProperty("_attachments")
    public void setPouchAttachments(Map<String, PouchAttachment> pouchAttachments);
}
