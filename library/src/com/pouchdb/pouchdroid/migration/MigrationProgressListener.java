package com.pouchdb.pouchdroid.migration;

import java.util.List;

/**
 * This class is called on progress updates to let you know how far along the syncing is.
 * 
 * @author nolan
 *
 */
public abstract class MigrationProgressListener {

    /**
     * Called when a number of rows have been loaded into PouchDB.
     * Data is loaded in batches, tables are loaded sequentially in
     * the order defined.
     * 
     * @param tableName name of the sqlite table being loaded
     * @param numRowsTotal number of rows in this table 
     * @param numRowsLoaded number of rows we've loaded so far
     */
    public abstract void onProgress(String tableName, int numRowsTotal, int numRowsLoaded);
    
    /**
     * Called when the migration process begins.
     */
    public abstract void onStart();
    
    /**
     * Called when the migration process fully ends.
     */
    public abstract void onEnd();
    

    /**
     * Called when the migration task has detected some number of documents for all tables that need
     * to be deleted, and has deleted them from PouchDB as appropriate.  Occurs after
     * all <code>onProgress()</code> calls have finished.  Called exactly once.
     * 
     * @param numDocumentsDeleted number of documents deleted in PouchDB to reflect the state of the SQLite tables
     */
    public abstract void onDocsDeleted(int numDocumentsDeleted);
    
    /**
     * Create a MigrationProgressListener that extends 0 or more other listeners
     * @param others
     * @return
     */
    public static MigrationProgressListener extend(
            final List<MigrationProgressListener> others,
            final MigrationProgressListener prototype) {
        
        return new MigrationProgressListener() {
            
            @Override
            public void onStart() {
                for (MigrationProgressListener subListener : others) {
                    if (subListener != null) {
                        subListener.onStart();
                    }
                }
                prototype.onStart();
            }
            
            @Override
            public void onProgress(String tableName, int numRowsTotal, int numRowsLoaded) {
                for (MigrationProgressListener subListener : others) {
                    if (subListener != null) {
                        subListener.onProgress(tableName, numRowsTotal, numRowsLoaded);
                    }
                }
                prototype.onProgress(tableName, numRowsTotal, numRowsLoaded);
            }
            
            @Override
            public void onEnd() {
                for (MigrationProgressListener subListener : others) {
                    if (subListener != null) {
                        subListener.onEnd();
                    }
                }
                prototype.onEnd();
            }

            @Override
            public void onDocsDeleted(int numDocumentsDeleted) {
                for (MigrationProgressListener subListener : others) {
                    if (subListener != null) {
                        subListener.onDocsDeleted(numDocumentsDeleted);
                    }
                }
                prototype.onDocsDeleted(numDocumentsDeleted);
            }
        };
    }
}
