package com.pouchdb.pouchdroid.pouch.model;

/**
 * Representation of a standard CouchDB/PouchDB map function.
 * 
 * @see <a href=
 *      'https://wiki.apache.org/couchdb/Introduction_to_CouchDB_views#Map_Functions'>https://wiki.apache.org/couchdb/Introduction_to_CouchDB_views#Map_Functions</
 *      a >
 * @author nolan
 * 
 */
public class MapFunction {

    private CharSequence javascript;

    private MapFunction(CharSequence javascript) {
        this.javascript = javascript;
    }

    public CharSequence toJavascript() {
        return javascript;
    }

    /**
     * Create a new map function using pure Javascript.
     * 
     * @see <a href=
     *      'https://wiki.apache.org/couchdb/Introduction_to_CouchDB_views#Map_Functions'>https://wiki.apache.org/couchdb/Introduction_to_CouchDB_views#Map_Functions</
     *      a>
     * @param javascript
     * @return
     */
    public static MapFunction fromJavascript(CharSequence javascript) {
        return new MapFunction(javascript);
    }
    
    /**
     * Create a new map function that simply indexes documents based on their fields, using null as the value, 
     * per the standard CouchDB best practices.  This means that you'll need to use include_docs=true in order
     * to get the documents back from this function, but the view will take up less space on disk.
     * 
     * I.e. it generates javascript like this:
     * 
     * <code>
     * function(doc) {
     *   emit(doc.myField, null);
     * }
     * </code>
     * @return
     */
    public static MapFunction simpleFieldLookup(CharSequence fieldName) {
        return new MapFunction(
                new StringBuilder("function(doc){emit(doc.")
                .append(fieldName)
                .append(", null);}"));
    }
    
    /**
     * Create a new map function that simply indexes documents based on their fields, using null as the value, 
     * per the standard CouchDB best practices.  This means that you'll need to use include_docs=true in order
     * to get the documents back from this function, but the view will take up less space on disk.
     * 
     * I.e. it generates javascript like this:
     * 
     * <code>
     * function(doc) {
     *   emit([doc.myField1, doc.myField2], null);
     * }
     * </code>
     * @return
     */
    public static MapFunction simpleFieldLookup(CharSequence fieldName, CharSequence... additionalFieldNames) {
        StringBuilder js = new StringBuilder("function(doc){emit([doc.").append(fieldName);
        
        for (CharSequence additionalFieldName : additionalFieldNames) {
            js.append(",doc.").append(additionalFieldName);
        }
        
        js.append("], null);}");
        return new MapFunction(js);
    }    
}
