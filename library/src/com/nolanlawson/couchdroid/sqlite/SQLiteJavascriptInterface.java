/**
 * Mostly taken from the Android PhoneGap SQLite plugin.
 * 
 * Acts as an interface to the sqlite_native_interface.js file.  Interacts with SQLite and pretends to
 * be the WebSQL standard.
 * 
 * @author nolan
 */
package com.nolanlawson.couchdroid.sqlite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.nolanlawson.couchdroid.sqlite.BasicSQLiteOpenHelper.SQLiteTask;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class SQLiteJavascriptInterface {

    private static UtilLogger log = new UtilLogger(SQLiteJavascriptInterface.class);
    
    private Activity activity;
    private WebView webView;
    
    private final Map<String, BasicSQLiteOpenHelper> dbs = new HashMap<String, BasicSQLiteOpenHelper>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PriorityQueue<WebSqlTask> queue = new PriorityQueue<WebSqlTask>();
    
    private int currentTransactionId = -1;
    
    public SQLiteJavascriptInterface(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }
    
    private void sendCallback(JavascriptCallback callback) {
        sendCallback(Collections.singletonList(callback));
    }
    
    private void sendCallback(List<JavascriptCallback> callbacks) {
        log.d("sendCallback(%s)", callbacks);

        try {
            final StringBuilder url = new StringBuilder().append("javascript:(function(){");
            for (JavascriptCallback callback : callbacks) {
                url.append("SQLiteNativeDB.callbacks['")
                    .append(callback.getCallbackId())
                    .append("'](")
                    .append(callback.getArg1() != null ? objectMapper.writeValueAsString(callback.getArg1()) : "")
                    .append(");");
            }
            url.append("})();");

            log.d("calling javascript: %s", url);

            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(url.toString());
                }
            });
        } catch (IOException e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }
    
    /**
     * Get the names of the databases that pouch has created
     * @return
     */
    public List<String> getDbNames() {
        return new ArrayList<String>(dbs.keySet());
    }
    
    @JavascriptInterface
    public void open(final String dbName, final String callbackId) {
        log.d("open(%s, %s)", dbName, callbackId);
        try {
            BasicSQLiteOpenHelper db = dbs.get(dbName);
            if (db == null) { // doesn't exist yet
                db = new BasicSQLiteOpenHelper(activity, dbName);
                dbs.put(dbName, db);
            }
            sendCallback(new JavascriptCallback(callbackId, null, false));
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "unexpected");
        } 
    }

    @JavascriptInterface
    public void startTransaction(int transactionId, String dbName, String successId, String errorId) {
        log.d("startTransaction(%s, %s, %s, %s)", transactionId, dbName, successId, errorId);
        
        queue.add(WebSqlTask.forBeginTransaction(transactionId, dbName, successId, errorId));
        
        doUnitOfSqliteWork();
    }
    
    @JavascriptInterface
    public void endTransaction(int transactionId, String dbName, String successId, String errorId,
            boolean markAsSuccessful) {
        
        log.d("endTransaction(%s, %s, %s, %s, %s)", transactionId, dbName, successId, errorId, markAsSuccessful);
        
        queue.add(WebSqlTask.forEndTransaction(transactionId, dbName, successId, errorId, markAsSuccessful));
        
        doUnitOfSqliteWork();
    }
    
    @JavascriptInterface
    public void executeSql(int queryId, int transactionId, final String dbName, final String sql, 
            final String selectArgsJson, final String querySuccessId, final String queryErrorId) {
        log.d("executeSql(%s, %s, %s, %s, %s, %s, %s)", queryId, transactionId, dbName, sql, selectArgsJson, 
                querySuccessId, queryErrorId);
        
        queue.add(WebSqlTask.forExecuteSql(queryId, transactionId, dbName, sql, selectArgsJson, querySuccessId, 
                queryErrorId));
        
        doUnitOfSqliteWork();
    }
    
    @SuppressLint("NewApi")
    private void execute(SQLiteDatabase db, WebSqlTask task) {
        
        String sql = (String)task.getArguments().get(0);
        String selectArgsJson = (String)task.getArguments().get(1);
        String querySuccessId = task.getSuccessId(); 
        String queryErrorId = task.getErrorId();
        
        try {
            List<Object> selectArgs = getSelectArgs(selectArgsJson);
            String query = sql;
            List<Object> batchResults = new ArrayList<Object>();
            ObjectNode queryResult = null;

            // /* OPTIONAL changes for new Android SDK from HERE:
            if (android.os.Build.VERSION.SDK_INT >= 11
                    && (query.toLowerCase(Locale.US).startsWith("update") 
                            || query.toLowerCase(Locale.US).startsWith("delete"))) {
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
            if (query.toLowerCase(Locale.US).startsWith("insert") && selectArgs != null) {
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

                    queryResult = this.getRowsResultFromQuery(myCursor);

                    myCursor.close();
                }
            }
            

            if (queryResult != null) {
                ObjectNode r = objectMapper.createObjectNode();

                r.put("type", "success");
                r.put("result", queryResult);

                batchResults.add(r);
            }
            log.d("query success");
            sendCallback(new JavascriptCallback(querySuccessId, queryResult, false));
        } catch (Exception e) {
            log.e(e, "unexpected");
            sendCallback(new JavascriptCallback(queryErrorId, createSqlError(e.getMessage()), true));
        }
    }
    
    private ObjectNode createSqlError(String message) {
        ObjectNode sqlErrorObject = objectMapper.createObjectNode();

        sqlErrorObject.put("type", "error");
        sqlErrorObject.put("result", message);
        
        return sqlErrorObject;
    }

    private List<Object> getSelectArgs(String selectArgsJson) {

        if (TextUtils.isEmpty(selectArgsJson)) {
            return null;
        }

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
        log.i("close()");
        this.activity = null;
        
        for (Entry<String, BasicSQLiteOpenHelper> entry : dbs.entrySet()) {
            String dbName = entry.getKey();
            BasicSQLiteOpenHelper dbHelper = entry.getValue();
            
            if (dbHelper != null) {
                log.i("closing database with name %s", dbName);
                synchronized (BasicSQLiteOpenHelper.class) {
                    dbHelper.close();
                }
                log.i("closed database with name %s", dbName);
            }
        }
    }
    
    private void doUnitOfSqliteWork() {
        
        log.d("doUnitOfSqliteWork");
        
        WebSqlTask task;
        while ((task = queue.poll()) != null) {
            
            if (task.getTransactionId() < currentTransactionId && task.getType() != WebSqlTask.Type.BeginTransaction) {
                log.d("skipping because of canceled transaction: %s");
                continue;
            }
            
            currentTransactionId = task.getTransactionId();
            
            perform(task);
        }
    }

    private void perform(final WebSqlTask task) {
        log.d("perform(%s)", task);
        
        BasicSQLiteOpenHelper dbHelper = dbs.get(task.getDbName());
        if (dbHelper == null) {
            log.d("couldn't find db for name %s", task.getDbName());
            sendCallback(new JavascriptCallback(task.getErrorId(), "couldn't find db", true));
            return;
        }
        
        dbHelper.post(new SQLiteTask() {
            
            @Override
            public void run(SQLiteDatabase db) {
                switch (task.getType()) {
                    case EndTransaction:
                        endTransaction(db, task);
                        break;
                    case BeginTransaction:
                        beginTransaction(db, task);
                        break;
                    case ExecSql:
                    default:
                        executeQuery(db, task);
                        break;
                }
            }
        });          
    }
    
    private void endTransaction(SQLiteDatabase db, final WebSqlTask task) {
        log.d("endTransaction: %s", task);
        boolean error = false;
        
        boolean markAsSuccessful = (Boolean)task.getArguments().get(0);
        
        try {
            if (markAsSuccessful) {
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            log.e(e, "unexpected");
            error = true;
        } finally {
            try {
                db.endTransaction();
            } catch (Exception e) {
                log.e(e, "unexpected");
                error = true;
            }
            
            if (error) {
                sendCallback(new JavascriptCallback(task.getErrorId(), null, true));
            } else {
                sendCallback(new JavascriptCallback(task.getSuccessId(), null, false));
            }
        }                
    }

    private void executeQuery(SQLiteDatabase db, final WebSqlTask task) {
        log.d("executeSql: %s", task);
        
        execute(db, task);
    }

    private void beginTransaction(SQLiteDatabase db, final WebSqlTask task) {
        log.d("beginTransaction: %s", task);
        try {
            db.beginTransaction();
            sendCallback(new JavascriptCallback(task.getSuccessId(), null, false));
        } catch (Exception e) {
            // couldn't even begin
            sendCallback(new JavascriptCallback(task.getErrorId(), null, true));
        }
    }
}
