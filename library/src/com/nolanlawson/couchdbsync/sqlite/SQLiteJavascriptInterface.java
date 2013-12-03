/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.nolanlawson.couchdbsync.sqlite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.nolanlawson.couchdbsync.util.UtilLogger;

public class SQLiteJavascriptInterface {

    private static UtilLogger log = new UtilLogger(SQLiteJavascriptInterface.class);
    
    private Activity activity;
    private Map<String, SQLiteDatabase> dbs = new HashMap<String, SQLiteDatabase>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private WebView webView;

    public SQLiteJavascriptInterface(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    private void callback(String callbackId, Object arg1) {
        log.d("callback(%s, %s)", callbackId, arg1);

        try {
            final String url = new StringBuilder().append("javascript:window.nativeDBCallbacks['").append(callbackId)
                    .append("'](").append(arg1 != null ? objectMapper.writeValueAsString(arg1) : "").append(");")
                    .toString();

            log.d("calling javascript: %s", url);

            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(url);
                }
            });
        } catch (IOException e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }

    private void callback(String callbackId) {
        callback(callbackId, null);
    }

    private void executeInBackground(final Runnable runnable) {
        /*new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                runnable.run();
                return null;
            }
            
        }.execute((Void)null);*/
        runnable.run();
    }

    @JavascriptInterface
    public void open(final String name, final String callbackId) {

        executeInBackground(new Runnable() {
            @Override
            public void run() {
                log.d("open(%s, %s)", name, callbackId);
                try {
                    SQLiteDatabase db = activity.openOrCreateDatabase(name, 0, null);
                    dbs.put(name, db);
                    callback(callbackId);
                } catch (Exception e) {
                    // shouldn't happen
                    log.e(e, "unexpected");
                }
            }
        });
    }

    @JavascriptInterface
    public void startTransaction(final String name, final String successId, final String errorId) {
        executeInBackground(new Runnable() {
            @Override
            public void run() {

                log.d("startTransaction(%s, %s)", successId, errorId);
                try {
                    SQLiteDatabase db = dbs.get(name);
                    synchronized (db) {
                        db.beginTransaction();
                    }
                    callback(successId);
                } catch (Exception e) {
                    log.e(e, "error");
                    callback(errorId);
                }
            }
        });
    }

    @JavascriptInterface
    public void endTransaction(final String name, final String endTransactionSuccessId, final String transactionErrorId) {
        executeInBackground(new Runnable() {
            @Override
            public void run() {
                log.d("endTransaction(%s, %s)", endTransactionSuccessId, transactionErrorId);
                SQLiteDatabase db = dbs.get(name);
                try {
                    synchronized (db) {
                        db.setTransactionSuccessful();
                    }
                    callback(endTransactionSuccessId);
                } catch (Exception e) {
                    callback(transactionErrorId);
                } finally {
                    synchronized (db) {
                        db.endTransaction();
                    }
                }
            }
        });
    }

    @JavascriptInterface
    public void executeSql(final String name, final String sql, final String selectArgsJson, final String querySuccessId,
            final String transactionErrorId) {

        executeInBackground(new Runnable() {

            @Override
            public void run() {
                executeSqlInBackground(name, sql, selectArgsJson, querySuccessId, transactionErrorId);
            }
        });
    }

    @SuppressLint("NewApi")
    public void executeSqlInBackground(String name, String sql, String selectArgsJson, String querySuccessId,
            String transactionErrorId) {
        log.d("executeSql(%s, %s, %s, %s)", sql, selectArgsJson, querySuccessId, transactionErrorId);
        
        SQLiteDatabase db = dbs.get(name);
        try {
            List<Object> selectArgs = getSelectArgs(selectArgsJson);

            String query = "";
            String query_id = "";
            String[] queryarr = new String[] { sql };
            String[] queryIDs = new String[] { "foo" };
            int len = queryarr.length;

            List<Object> batchResults = new ArrayList<Object>();

            for (int i = 0; i < len; i++) {
                query_id = queryIDs[i];

                ObjectNode queryResult = null;
                String errorMessage = null;

                try {
                    query = queryarr[i];

                    // /* OPTIONAL changes for new Android SDK from HERE:
                    if (android.os.Build.VERSION.SDK_INT >= 11
                            && (query.toLowerCase().startsWith("update") || query.toLowerCase().startsWith("delete"))) {
                        synchronized (db) {
                            SQLiteStatement myStatement = db.compileStatement(query);
    
                            if (selectArgs != null) {
                                for (int j = 0; j < selectArgs.size(); j++) {
                                    if (selectArgs.get(j) instanceof Float || selectArgs.get(j) instanceof Double) {
                                        myStatement.bindDouble(j + 1, (Double) selectArgs.get(j));
                                    } else if (selectArgs.get(j) instanceof Integer) {
                                        myStatement.bindLong(j + 1, (Integer) selectArgs.get(j));
                                    } else if (selectArgs.get(j) instanceof Long) {
                                        myStatement.bindLong(j + 1, (Long) selectArgs.get(j));
                                    } else if (selectArgs.get(j) == null) {
                                        myStatement.bindNull(j + 1);
                                    } else {
                                        myStatement.bindString(j + 1, (String) selectArgs.get(j));
                                    }
                                }
                            }
    
                            int rowsAffected = myStatement.executeUpdateDelete();
    
                            queryResult = objectMapper.createObjectNode();
                            queryResult.put("rowsAffected", rowsAffected);
                        }
                    } else // to HERE. */
                    if (query.toLowerCase().startsWith("insert") && selectArgs != null) {
                        synchronized (db) {
                            SQLiteStatement myStatement = db.compileStatement(query);
    
                            for (int j = 0; j < selectArgs.size(); j++) {
                                if (selectArgs.get(j) instanceof Float || selectArgs.get(j) instanceof Double) {
                                    myStatement.bindDouble(j + 1, (Double) selectArgs.get(j));
                                } else if (selectArgs.get(j) instanceof Integer) {
                                    myStatement.bindLong(j + 1, (Integer) selectArgs.get(j));
                                } else if (selectArgs.get(j) instanceof Long) {
                                    myStatement.bindLong(j + 1, (Long) selectArgs.get(j));
                                } else if (selectArgs.get(j) == null) {
                                    myStatement.bindNull(j + 1);
                                } else {
                                    myStatement.bindString(j + 1, (String) selectArgs.get(j));
                                }
                            }
    
                            long insertId = myStatement.executeInsert();
    
                            int rowsAffected = (insertId == -1) ? 0 : 1;
    
                            queryResult = objectMapper.createObjectNode();
                            queryResult.put("insertId", insertId);
                            queryResult.put("rowsAffected", rowsAffected);
                        }
                    } else {
                        String[] params = null;

                        if (selectArgs != null) {
                            params = new String[selectArgs.size()];

                            for (int j = 0; j < selectArgs.size(); j++) {
                                if (selectArgs.get(j) == null)
                                    params[j] = "";
                                else
                                    params[j] = String.valueOf(selectArgs.get(j));
                            }
                        }

                        synchronized (db) {
                            Cursor myCursor = db.rawQuery(query, params);
    
                            if (query_id.length() > 0) {
                                queryResult = this.getRowsResultFromQuery(myCursor);
                            }
    
                            myCursor.close();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMessage = ex.getMessage();
                    Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" + errorMessage);
                }

                try {
                    if (queryResult != null) {
                        ObjectNode r = objectMapper.createObjectNode();
                        r.put("qid", query_id);

                        r.put("type", "success");
                        r.put("result", queryResult);

                        batchResults.add(r);
                    } else if (errorMessage != null) {
                        ObjectNode r = objectMapper.createObjectNode();
                        r.put("qid", query_id);

                        r.put("type", "error");
                        r.put("result", errorMessage);

                        batchResults.add(r);
                    }
                } catch (Exception e) {
                    log.e(e, "unexpected");
                    // TODO what to do?
                }

                callback(querySuccessId, queryResult);
            }
        } catch (Exception e) {
            log.e(e, "unexpected");
        }
    }

    private List<Object> getSelectArgs(String selectArgsJson) {

        if (TextUtils.isEmpty(selectArgsJson)) {
            return null;
        }

        log.d("getSelectArgs(%s)", selectArgsJson);
        try {
            return objectMapper.readValue(selectArgsJson, new TypeReference<List<Object>>() {
            });
        } catch (IOException e) {
            // ignore
            log.e(e, "unexpected error");
        }
        return null;
    }

    /**
     * Get rows results from query cursor.
     * 
     * @param cur
     *            Cursor into query results
     * 
     * @return results in string form
     * 
     */
    @SuppressLint("NewApi")
    private ObjectNode getRowsResultFromQuery(Cursor cur) {
        ObjectNode rowsResult = objectMapper.createObjectNode();

        ArrayNode rowsArrayResult = rowsResult.putArray("rows");

        // If query result has rows
        if (cur.moveToFirst()) {
            String key = "";
            int colCount = cur.getColumnCount();

            // Build up JSON result object for each row
            do {
                ObjectNode row = objectMapper.createObjectNode();
                try {
                    for (int i = 0; i < colCount; ++i) {
                        key = cur.getColumnName(i);

                        // for old Android SDK remove lines from HERE:
                        if (android.os.Build.VERSION.SDK_INT >= 11) {
                            switch (cur.getType(i)) {
                                case Cursor.FIELD_TYPE_NULL:
                                    row.putNull(key);
                                    break;
                                case Cursor.FIELD_TYPE_INTEGER:
                                    row.put(key, cur.getInt(i));
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    row.put(key, cur.getFloat(i));
                                    break;
                                case Cursor.FIELD_TYPE_STRING:
                                    row.put(key, cur.getString(i));
                                    break;
                                case Cursor.FIELD_TYPE_BLOB:
                                    row.put(key, new String(Base64.encode(cur.getBlob(i), Base64.DEFAULT)));
                                    break;
                            }
                        } else // to HERE.
                        {
                            row.put(key, cur.getString(i));
                        }
                    }

                    rowsArrayResult.add(row);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (cur.moveToNext());
        }

        return rowsResult;
    }

    public void close() {
        this.activity = null;
        for (SQLiteDatabase db : dbs.values()) {
            if (db != null) {
                synchronized (db) {
                    db.close();
                }
            }
        }
    }
}
