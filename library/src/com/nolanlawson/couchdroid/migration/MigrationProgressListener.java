package com.nolanlawson.couchdroid.migration;

import java.util.List;

/**
 * This class is called on progress updates to let you know how far along the syncing is.
 * 
 * @author nolan
 *
 */
public abstract class MigrationProgressListener {

    /**
     * Called when progress is updated.  Data is loaded in batches, tables are loaded sequentially in
     * the order defined.
     * 
     * @param tableName name of the sqlite table being loaded
     * @param numRowsTotal number of rows in this table 
     * @param numRowsLoaded number of rows we've loaded so far
     */
    public abstract void onMigrationProgress(String tableName, int numRowsTotal, int numRowsLoaded);
    
    /**
     * Called when the migration process begins.
     */
    public abstract void onMigrationStart();
    
    /**
     * Called when the migration process ends
     */
    public abstract void onMigrationEnd();
    
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
            public void onMigrationStart() {
                for (MigrationProgressListener subListener : others) {
                    if (subListener != null) {
                        subListener.onMigrationStart();
                    }
                }
                prototype.onMigrationStart();
            }
            
            @Override
            public void onMigrationProgress(String tableName, int numRowsTotal, int numRowsLoaded) {
                for (MigrationProgressListener subListener : others) {
                    if (subListener != null) {
                        subListener.onMigrationProgress(tableName, numRowsTotal, numRowsLoaded);
                    }
                }
                prototype.onMigrationProgress(tableName, numRowsTotal, numRowsLoaded);
            }
            
            @Override
            public void onMigrationEnd() {
                for (MigrationProgressListener subListener : others) {
                    if (subListener != null) {
                        subListener.onMigrationEnd();
                    }
                }
                prototype.onMigrationEnd();
            }
        };
    }
    
}
