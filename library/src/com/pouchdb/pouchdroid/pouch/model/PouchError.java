package com.pouchdb.pouchdroid.pouch.model;

/**
 * A standard error given by PouchDB.  Looks like this:
 * 
 * {"status":412,"error":"missing_id","message":"_id is required for puts"}
 * 
 * @author nolan
 *
 */
public class PouchError {

    private int status;
    private String name;
    private String message;
    private boolean error = true;
    
    public PouchError() {
    }
    
    public PouchError(int status, String name, String message) {
        this.status = status;
        this.name = name;
        this.message = message;
    }
    
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (error ? 1231 : 1237);
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (error != other.error)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PouchError [status=" + status + ", name=" + name + ", message=" + message + ", error=" + error + "]";
    }
}
