package com.nolanlawson.couchdbsync.sqlite;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Abstraction of a web sqlite transaction, so that we can batch queries and do other performance improvements
 * 
 * @author nolan
 *
 */
public class WebSqlTransaction {

    private String dbName;
    private int transactionId;
    private String successId;
    private String errorId;
    private LinkedBlockingQueue<WebSqlQuery> queries = new LinkedBlockingQueue<WebSqlQuery>();
    private boolean invalid = false;
    
    public WebSqlTransaction(String dbName, int transactionId, String successId, String errorId) {
        this.dbName = dbName;
        this.transactionId = transactionId;
        this.successId = successId;
        this.errorId = errorId;
    }

    public String getDbName() {
        return dbName;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public LinkedBlockingQueue<WebSqlQuery> getQueries() {
        return queries;
    }

    public String getSuccessId() {
        return successId;
    }

    public String getErrorId() {
        return errorId;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    @Override
    public String toString() {
        return "WebSqlTransaction [dbName=" + dbName + ", transactionId=" + transactionId + ", successId=" + successId
                + ", errorId=" + errorId + ", queries=" + queries + ", invalid=" + invalid + "]";
    }
}
