package com.nolanlawson.couchdroid.pouch.model;

/**
 * A standard error given by PouchDB.  Looks like this:
 * 
 * {"status":412,"error":"missing_id","reason":"_id is required for puts"}
 * 
 * @author nolan
 *
 */
public class PouchError {

    private int status;
    private String error;
    private String reason;
    
    public PouchError() {
    }
    
    public PouchError(int status, String error, String reason) {
        this.status = status;
        this.error = error;
        this.reason = reason;
    }
    
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((error == null) ? 0 : error.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + status;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PouchError other = (PouchError) obj;
        if (error == null) {
            if (other.error != null)
                return false;
        } else if (!error.equals(other.error))
            return false;
        if (reason == null) {
            if (other.reason != null)
                return false;
        } else if (!reason.equals(other.reason))
            return false;
        if (status != other.status)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "PouchError [status=" + status + ", error=" + error + ", reason=" + reason + "]";
    }
}
