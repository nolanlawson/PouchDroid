package com.nolanlawson.couchdroid;

/**
 * This class is called on progress updates to let you know how far along the syncing is.
 * 
 * @author nolan
 *
 */
public interface CouchDroidProgressListener {

    /**
     * Called when progress is updated.  Data is loaded in batches, tables are loaded sequentially in
     * the order defined.
     * 
     * @param type the type of progress we're reporting
     * @param tableName name of the sqlite table being loaded
     * @param numRowsTotal number of rows in this table 
     * @param numRowsLoaded number of rows we've loaded so far
     */
    public void onProgress(ProgressType type, String tableName, int numRowsTotal, int numRowsLoaded);
    
    public static enum ProgressType {
        
        /**
         * Called when the syncing is just beginning
         */
        Init,
        
        /**
         * Called during the copying process, when one SQLite database is copied to another
         */
        Copy,
        
        /**
         * Called during the syncing progress, when we actually sync to a remote CouchDB
         */
        Sync
    }
    
}
