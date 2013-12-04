package com.nolanlawson.couchdbsync.sqlite;

public class WebSqlQuery {
    
    private String sql;
    private String selectArgsJson;
    private String querySuccessId;
    private String queryErrorId;
    
    public WebSqlQuery(String sql, String selectArgsJson, String querySuccessId, String queryErrorId) {
        this.sql = sql;
        this.selectArgsJson = selectArgsJson;
        this.querySuccessId = querySuccessId;
        this.queryErrorId = queryErrorId;
    }
    public String getSql() {
        return sql;
    }
    public String getSelectArgsJson() {
        return selectArgsJson;
    }
    public String getQuerySuccessId() {
        return querySuccessId;
    }
    public String getQueryErrorId() {
        return queryErrorId;
    }
    @Override
    public String toString() {
        return "WebSqlQuery [sql=" + sql + ", selectArgsJson=" + selectArgsJson + ", querySuccessId=" + querySuccessId
                + ", queryErrorId=" + queryErrorId + "]";
    }
}
