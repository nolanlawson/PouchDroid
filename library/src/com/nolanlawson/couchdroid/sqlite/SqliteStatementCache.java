package com.nolanlawson.couchdroid.sqlite;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.LruCache;

/**
 * Abstraction around a weak reference cache of compiled sql statements.  Since LruCache is only available
 * in API 12 and later, this doesn't do anything for older devices.  (TODO: use the Android support library?)
 * 
 * @author nolan
 *
 */
public class SqliteStatementCache {

    
    /* This doesn't have to be a large number, cuz there are actually only a few statements 
     * that get re-used by pouch, i.e. :
     * INSERT INTO 'by-sequence' (doc_id_rev, json) VALUES (?, ?);
     * INSERT INTO 'document-store' (id, seq, winningseq, json) VALUES (?, ?, ?, ?);
     * UPDATE 'metadata-store' SET update_seq=?
     */
    private static final int MAX_SIZE = 10;
    private static final int MIN_SDK_INT = 12; // required for lru cache, honeycomb mr1
    
    private Object cache;
    
    public SqliteStatementCache() {
        if (Build.VERSION.SDK_INT >= MIN_SDK_INT) {
            
            // sqlite statements are not thread safe
            cache = new ThreadLocal<LruCache<CacheKey, SQLiteStatement>>(){

                @SuppressLint("NewApi")
                @Override
                protected LruCache<CacheKey, SQLiteStatement> initialValue() {
                    return new LruCache<CacheKey, SQLiteStatement>(MAX_SIZE);
                }
            };
        }
    }
    
    @SuppressLint("NewApi")
    @SuppressWarnings("unchecked")
    public SQLiteStatement get(String dbName, String sql) {
        if (cache == null) {
            return null;
        }
        
        ThreadLocal<LruCache<CacheKey, SQLiteStatement>> localCache = 
                (ThreadLocal<LruCache<CacheKey, SQLiteStatement>>) cache;
        
        return localCache.get().get(new CacheKey(dbName, sql));
    }
    
    @SuppressLint("NewApi")
    @SuppressWarnings("unchecked")
    public void put(String dbName, String sql, SQLiteStatement compiledStatement) {
        if (cache == null) {
            return;
        }
        
        ThreadLocal<LruCache<CacheKey, SQLiteStatement>> localCache = 
                (ThreadLocal<LruCache<CacheKey, SQLiteStatement>>) cache;
        
        localCache.get().put(new CacheKey(dbName, sql), compiledStatement);
    }
    
    private static class CacheKey {
        
        private String dbName;
        private String sql;
        
        public CacheKey(String dbName, String sql) {
            this.dbName = dbName;
            this.sql = sql;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dbName == null) ? 0 : dbName.hashCode());
            result = prime * result + ((sql == null) ? 0 : sql.hashCode());
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
            CacheKey other = (CacheKey) obj;
            if (dbName == null) {
                if (other.dbName != null)
                    return false;
            } else if (!dbName.equals(other.dbName))
                return false;
            if (sql == null) {
                if (other.sql != null)
                    return false;
            } else if (!sql.equals(other.sql))
                return false;
            return true;
        }
    }
    
}
