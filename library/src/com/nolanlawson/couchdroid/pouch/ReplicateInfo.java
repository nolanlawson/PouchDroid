package com.nolanlawson.couchdroid.pouch;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents the basic object Pouch gives us for the "complete" callback on replication.  Looks like this:
 * 
 * {
 *   'ok': true,
 *   'docs_read': 2,
 *   'docs_written': 2,
 *   'start_time': "Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)",
 *   'end_time': "Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)"
 * }
 * 
 * @author nolan
 *
 */
public class ReplicateInfo {

    private boolean ok;
    private int docsRead;
    private int docsWritten;
    private Date startTime;
    private Date endTime;
    
    public boolean isOk() {
        return ok;
    }
    public void setOk(boolean ok) {
        this.ok = ok;
    }
    
    @JsonProperty("docs_read")
    public int getDocsRead() {
        return docsRead;
    }
    
    @JsonProperty("docs_read")
    public void setDocsRead(int docsRead) {
        this.docsRead = docsRead;
    }
    
    @JsonProperty("docs_written")
    public int getDocsWritten() {
        return docsWritten;
    }
    
    @JsonProperty("docs_written")
    public void setDocsWritten(int docsWritten) {
        this.docsWritten = docsWritten;
    }
    
    @JsonProperty("start_time")
    public Date getStartTime() {
        return startTime;
    }
    
    @JsonProperty("start_time")
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    @JsonProperty("end_time")
    public Date getEndTime() {
        return endTime;
    }
    
    @JsonProperty("end_time")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    @Override
    public String toString() {
        return "ReplicateInfo [ok=" + ok + ", docsRead=" + docsRead + ", docsWritten=" + docsWritten + ", startTime="
                + startTime + ", endTime=" + endTime + "]";
    }
}
