package com.pouchdb.pouchdroid.pouch.model;

/**
 * The standard response that Pouch gives back on success.  Looks like this:
 * {ok=true, id=13B8A234-AA30-4833-821A-78725BEDD546, rev=1-98d278dd695a3d4e47a91e5d58acc441}
 * @author nolan
 *
 */
public final class PouchInfo {

    private boolean ok;
    private String id;
    private String rev;
    
    public boolean isOk() {
        return ok;
    }
    public void setOk(boolean ok) {
        this.ok = ok;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getRev() {
        return rev;
    }
    public void setRev(String rev) {
        this.rev = rev;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (ok ? 1231 : 1237);
        result = prime * result + ((rev == null) ? 0 : rev.hashCode());
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
        PouchInfo other = (PouchInfo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (ok != other.ok)
            return false;
        if (rev == null) {
            if (other.rev != null)
                return false;
        } else if (!rev.equals(other.rev))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "PouchResponse [ok=" + ok + ", id=" + id + ", rev=" + rev + "]";
    }
}
