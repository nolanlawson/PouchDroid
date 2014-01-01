package com.pouchdb.pouchdroid.pouch.model;

/**
 * Representation of a standard PouchDB/CouchDB reduce function.
 * 
 * <p/> A word to the wise: if you're not using the built-in reduce functions, you're probably doing something wrong.
 * 
 * @see <a href='https://wiki.apache.org/couchdb/Introduction_to_CouchDB_views#Reduce_Functions'>https://wiki.apache.org/couchdb/Introduction_to_CouchDB_views#Reduce_Functions</a>
 * @see <a href='https://wiki.apache.org/couchdb/Built-In_Reduce_Functions'>https://wiki.apache.org/couchdb/Built-In_Reduce_Functions</a>
 * @see <a href='http://youtu.be/BKQ9kXKoHS8'>http://youtu.be/BKQ9kXKoHS8</a>
 * @author nolan
 *
 */
public class ReduceFunction {
    
    private String name;
    private CharSequence javascript;
    
    private ReduceFunction(String name) {
        this.name = name;
    }
    
    public CharSequence toJavascript() {
        if (name != null) {
            // built-in
            return new StringBuilder("\"").append(name).append("\"");
        }
        return javascript;
    }
    
    /**
     * Built-in "_sum" function.
     * @see <a href='https://wiki.apache.org/couchdb/Built-In_Reduce_Functions'>https://wiki.apache.org/couchdb/Built-In_Reduce_Functions</a>
     * 
     * @return
     */
    public static ReduceFunction sum() {
        return new ReduceFunction("_sum");
    }
    
    /**
     * Built-in "_count" function.
     * @see <a href='https://wiki.apache.org/couchdb/Built-In_Reduce_Functions'>https://wiki.apache.org/couchdb/Built-In_Reduce_Functions</a>
     * 
     * @return
     */
    public static ReduceFunction count() {
        return new ReduceFunction("_count");
    }
    
    /**
     * Built-in "_stats" function.
     * @see <a href='https://wiki.apache.org/couchdb/Built-In_Reduce_Functions'>https://wiki.apache.org/couchdb/Built-In_Reduce_Functions</a>
     * 
     * @return
     */
    public static ReduceFunction stats() {
        return new ReduceFunction("_stats");
    }    
    
    /**
     * Create a new reduce function using pure Javascript.
     * 
     * <p/> A word to the wise: if you're not using the built-in reduce functions, you're probably doing something wrong.
     * 
     * @see <a href='http://youtu.be/BKQ9kXKoHS8'>http://youtu.be/BKQ9kXKoHS8</a>
     * @param javascript
     * @return
     */
    public static ReduceFunction fromJavascript(CharSequence javascript) {
        ReduceFunction result = new ReduceFunction(null);
        result.javascript = javascript;
        return result;
    }
}
