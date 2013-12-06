package com.nolanlawson.couchdroid.sqlite;

import java.util.Arrays;
import java.util.List;

public class WebSqlTask implements Comparable<WebSqlTask> {

    private Type type;
    
    private int transactionId;
    private int queryId;
    private String dbName;
    private String successId;
    private String errorId;
    private List<Object> arguments;
    
    private WebSqlTask(Type type, int transactionId, int queryId, String dbName, String successId, String errorId,
            List<Object> arguments) {
        this.type = type;
        this.transactionId = transactionId;
        this.queryId = queryId;
        this.dbName = dbName;
        this.successId = successId;
        this.errorId = errorId;
        this.arguments = arguments;
    }
    public Type getType() {
        return type;
    }
    public int getTransactionId() {
        return transactionId;
    }
    public int getQueryId() {
        return queryId;
    }
    public String getDbName() {
        return dbName;
    }
    public String getSuccessId() {
        return successId;
    }
    public String getErrorId() {
        return errorId;
    }
    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public int compareTo(WebSqlTask another) {
        // end transaction first, then exec sql, then begin transactions last
        // all sorted by transaction id itself
        if (transactionId != another.transactionId) {
            return transactionId < another.transactionId ? -1 : 1;
        } else if (type != another.type) {
            return type.ordinal() < another.type.ordinal() ? -1 : 1;
        } else if (type == Type.ExecSql) { // both are exec sql
            return queryId < another.queryId ? -1 : 1;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "WebSqlTask [type=" + type + ", transactionId=" + transactionId + ", queryId=" + queryId + ", dbName="
                + dbName + ", successId=" + successId + ", errorId=" + errorId + ", arguments=" + arguments + "]";
    }
    public static WebSqlTask forBeginTransaction(int transactionId, final String dbName, final String successId, 
            final String errorId) {
        return new WebSqlTask(Type.BeginTransaction, transactionId, 0, dbName, successId, errorId, null);
    }
    
    public static WebSqlTask forEndTransaction(int transactionId, String dbName, String successId, String errorId,
            boolean markAsSuccessful) {
        return new WebSqlTask(Type.EndTransaction, transactionId, 0, dbName, successId, errorId, 
                Arrays.<Object>asList(markAsSuccessful));
    }
    
    public static WebSqlTask forExecuteSql(int queryId, int transactionId, String dbName, String sql, 
            String selectArgsJson, String querySuccessId, String queryErrorId) {
        
        return new WebSqlTask(Type.ExecSql, transactionId, queryId, dbName, querySuccessId, queryErrorId, 
                Arrays.<Object>asList(sql, selectArgsJson));
    }

    public static enum Type {
        EndTransaction,
        ExecSql,
        BeginTransaction,
    }
}
