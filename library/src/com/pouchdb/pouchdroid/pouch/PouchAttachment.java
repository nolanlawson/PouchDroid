package com.pouchdb.pouchdroid.pouch;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * POJO of a PouchDB attachment.  Typically it contains the fields contentType and data for PUTs/POSTs,
 * plus digest and revpos for GETs.
 * @author nolan
 *
 */
public class PouchAttachment {
    
    private String contentType;
    private byte[] data;
    private String digest;
    private int revpos;
    private boolean stub;

    /**
     * Create an empty PouchAttachment.  You probably shouldn't use this method; this is just for Jackson.
     */
    public PouchAttachment() {
    }
    
    /**
     * Create a new PouchAttachment with the given data and content type.  This is the usual way
     * to PUT/POST a doc with an attachment; you don't need the digest or revpos.
     * 
     * @param contentType
     * @param data
     */
    public PouchAttachment(String contentType, byte[] data) {
        this.contentType = contentType;
        this.data = data;
    }
    
    @JsonProperty("content_type")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("content_type")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public int getRevpos() {
        return revpos;
    }

    public void setRevpos(int revpos) {
        this.revpos = revpos;
    }

    public boolean isStub() {
        return stub;
    }

    public void setStub(boolean stub) {
        this.stub = stub;
    }
}
