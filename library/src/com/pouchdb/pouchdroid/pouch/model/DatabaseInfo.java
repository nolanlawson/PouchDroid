package com.pouchdb.pouchdroid.pouch.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * POJO representing the object that's returned from db.info().
 * @author nolan
 *
 */
public class DatabaseInfo {
    
    
    private String dbName;
    private int docCount;
    private int updateSeq;

    @JsonProperty("db_name")
    public String getDbName() {
        return dbName;
    }

    @JsonProperty("db_name")
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @JsonProperty("doc_count")
    public int getDocCount() {
        return docCount;
    }

    @JsonProperty("doc_count")
    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }
    
    @JsonProperty("update_seq")
    public int getUpdateSeq() {
        return updateSeq;
    }
    
    @JsonProperty("update_seq")
    public void setUpdateSeq(int updateSeq) {
        this.updateSeq = updateSeq;
    }
    
    

}
