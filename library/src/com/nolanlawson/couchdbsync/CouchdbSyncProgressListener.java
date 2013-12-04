package com.nolanlawson.couchdbsync;

/**
 * This class is called on progress updates to let you know how far along the syncing is.
 * 
 * @author nolan
 *
 */
public interface CouchdbSyncProgressListener {

    /**
     * Called when progress is updated.  Data is loaded in batches, tables are loaded sequentially in
     * the order defined.
     * 
     * @param tableName name of the sqlite table being loaded
     * @param numRowsTotal number of rows in this table 
     * @param numRowsLoaded number of rows we've loaded so far
     */
    public void onProgress(String tableName, int numRowsTotal, int numRowsLoaded);
    
}
