package com.nolanlawson.couchdroid.migration;

import java.util.Map;

import com.nolanlawson.couchdroid.pouch.PouchDocument;

/**
 * Describes a generic document that was migrated from SQLite.  I.e. it started its life as a SQLite row.
 * @author nolan
 *
 */
public class GenericSqliteDocument extends PouchDocument {
    
    private String sqliteDB;
    private String table;
    private String appPackage;
    private String user;
    private Map<String, Object> content;
    public String getSqliteDB() {
        return sqliteDB;
    }
    public void setSqliteDB(String sqliteDB) {
        this.sqliteDB = sqliteDB;
    }
    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public String getAppPackage() {
        return appPackage;
    }
    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public Map<String, Object> getContent() {
        return content;
    }
    public void setContent(Map<String, Object> content) {
        this.content = content;
    }
}
