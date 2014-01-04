/**
 * Mostly taken from the Android PhoneGap SQLite plugin.
 * 
 * Acts as an interface to the sqlite_native_interface.js file.  Interacts with SQLite and pretends to
 * be the WebSQL standard.
 * 
 * @author nolan
 */
package com.pouchdb.pouchdroid.sqlite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

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
import android.util.SparseArray;
import android.webkit.JavascriptInterface;

import com.pouchdb.pouchdroid.PouchDroid;
import com.pouchdb.pouchdroid.sqlite.BasicSQLiteOpenHelper.SQLiteTask;
import com.pouchdb.pouchdroid.util.JsonUtil;
import com.pouchdb.pouchdroid.util.UtilLogger;

public class SQLiteJavascriptInterface {

    private static UtilLogger log = new UtilLogger(SQLiteJavascriptInterface.class);

    private PouchDroid pouchDroid;

    // keep static so that we only ever have one access to the dbs
    // see
    // http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
    private static final Map<String, BasicSQLiteOpenHelper> dbs = new HashMap<String, BasicSQLiteOpenHelper>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PriorityQueue<WebSqlTask> queue = new PriorityQueue<WebSqlTask>();
    private final SparseArray<Set<Integer>> transactionIdsToCallbackIds = new SparseArray<Set<Integer>>();
    private final SqliteStatementCache cache = new SqliteStatementCache();

    private Integer currentTransactionId = null;

    public SQLiteJavascriptInterface(PouchDroid pouchDroid) {
        this.pouchDroid = pouchDroid;
    }

    private void sendCallback(JavascriptCallback callback) {
        log.d("sendCallback(%s)", callback);

        try {
            StringBuilder js = new StringBuilder()
                .append("PouchDroid.SQLiteNativeDB.onNativeCallback(")
                .append(objectMapper.writeValueAsString(callback.getCallbackId()))
                .append(",")
                .append(callback.getArg1() != null ? objectMapper.writeValueAsString(callback.getArg1())
                        : "null").append(");");
            
            if (!TextUtils.isEmpty(callback.getExtraJavascript())) {
                js.append(callback.getExtraJavascript());
            }
            pouchDroid.loadJavascript(js);
        } catch (IOException e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }

    /**
     * Get the names of the databases that pouch has created
     * 
     * @return
     */
    public List<String> getDbNames() {
        return new ArrayList<String>(dbs.keySet());
    }

    @JavascriptInterface
    public void open(final String dbName, final int callbackId) {
        log.d("open(%s, %s)", dbName, callbackId);
        
        Activity activity = pouchDroid.getActivity();
        
        if (activity == null) {
            return; // app closed
        }
        
        try {
            BasicSQLiteOpenHelper db = dbs.get(dbName);
            if (db == null) { // doesn't exist yet
                db = new BasicSQLiteOpenHelper(activity.getApplicationContext(), dbName);
                dbs.put(dbName, db);
            }
            sendCallback(new JavascriptCallback(callbackId, null));
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }

    @JavascriptInterface
    public void startTransaction(int transactionId, String dbName, int successId, int errorId) {
        log.d("startTransaction(%s, %s, %s, %s)", transactionId, dbName, successId, errorId);
        try {
    
            queue.add(WebSqlTask.forBeginTransaction(transactionId, dbName, successId, errorId));
            registerCallbackIds(transactionId, successId, errorId);
            processQueue();
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }

    @JavascriptInterface
    public void endTransaction(int transactionId, String dbName, int successId, int errorId,
            boolean markAsSuccessful) {

        log.d("endTransaction(%s, %s, %s, %s, %s)", transactionId, dbName, successId, errorId, markAsSuccessful);
        try {
    
            queue.add(WebSqlTask.forEndTransaction(transactionId, dbName, successId, errorId, markAsSuccessful));
            registerCallbackIds(transactionId, successId, errorId);
            
            processQueue();
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }

    @JavascriptInterface
    public void executeSql(int queryId, int transactionId, final String dbName, final String sql,
            final String selectArgsJson, final int querySuccessId, final int queryErrorId) {
        log.d("executeSql(%s, %s, %s, %s, %s, %s, %s)", queryId, transactionId, dbName, sql, selectArgsJson,
                querySuccessId, queryErrorId);
        try {
    
            queue.add(WebSqlTask.forExecuteSql(queryId, transactionId, dbName, sql, selectArgsJson, querySuccessId,
                    queryErrorId));
            registerCallbackIds(transactionId, querySuccessId, queryErrorId);
            
            processQueue();
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "unexpected");
        }
    }

    @SuppressLint("NewApi")
    private void execute(SQLiteDatabase db, WebSqlTask task) {

        String query = (String) task.getArguments().get(0);
        String selectArgsJson = (String) task.getArguments().get(1);
        int querySuccessId = task.getSuccessId();
        int queryErrorId = task.getErrorId();

        try {
            List<Object> selectArgs = getSelectArgs(selectArgsJson);
            List<Object> batchResults = new ArrayList<Object>();
            ObjectNode queryResult = null;

            String queryLower = query.toLowerCase(Locale.US);

            // /* OPTIONAL changes for new Android SDK from HERE:
            if (android.os.Build.VERSION.SDK_INT >= 11
                    && (queryLower.startsWith("update") || queryLower.startsWith("delete"))) {
                SQLiteStatement myStatement = compileStatementOrGetFromCache(db, query);

                bindSelectArgs(myStatement, selectArgs);

                int rowsAffected = myStatement.executeUpdateDelete();

                queryResult = objectMapper.createObjectNode();
                queryResult.put("rowsAffected", rowsAffected);

                // to HERE. */
            } else if (queryLower.startsWith("insert") && selectArgs != null) {
                SQLiteStatement myStatement = compileStatementOrGetFromCache(db, query);

                bindSelectArgs(myStatement, selectArgs);

                long insertId = myStatement.executeInsert();

                int rowsAffected = (insertId == -1) ? 0 : 1;

                queryResult = objectMapper.createObjectNode();
                queryResult.put("insertId", insertId);
                queryResult.put("rowsAffected", rowsAffected);
            } else {
                // pragma command or something else
                String[] params = convertParamsToStringArray(selectArgs);
                Cursor myCursor = db.rawQuery(query, params);

                queryResult = this.getRowsResultFromQuery(myCursor);

                myCursor.close();
            }

            if (queryResult != null) {
                ObjectNode r = objectMapper.createObjectNode();

                r.put("type", "success");
                r.put("result", queryResult);

                batchResults.add(r);
            }
            log.d("query success");
            sendCallback(new JavascriptCallback(querySuccessId, queryResult));
        } catch (Exception e) {
            log.e(e, "unexpected");
            sendCallback(new JavascriptCallback(queryErrorId, createSqlError(e.getMessage())));
        }
    }

    private SQLiteStatement compileStatementOrGetFromCache(SQLiteDatabase db, String query) {
        
        SQLiteStatement result = cache.get(db.getPath(), query);
        
        if (result == null) {
            result = db.compileStatement(query);
            cache.put(db.getPath(), query, result);
        } else {
            log.d("Able to use cached sqlite statement for \"%s\"", query);
        }
        return result;
    }

    private String[] convertParamsToStringArray(List<Object> selectArgs) {
        String[] params = null;

        if (selectArgs != null) {
            params = new String[selectArgs.size()];

            for (int i = 0, len = selectArgs.size(); i < len; i++) {
                Object val = selectArgs.get(i);
                params[i] = (val == null) ? "" : String.valueOf(val);
            }
        }
        return params;
    }

    private void bindSelectArgs(SQLiteStatement statement, List<Object> selectArgs) {
        if (selectArgs == null) {
            return;
        }

        for (int i = 0, len = selectArgs.size(); i < len; i++) {

            Object val = selectArgs.get(i);

            if (val == null) {
                statement.bindNull(i + 1);
            } else if (val instanceof Float || val instanceof Double) {
                statement.bindDouble(i + 1, (Double) val);
            } else if (val instanceof Integer) {
                statement.bindLong(i + 1, (Integer) val);
            } else if (val instanceof Long) {
                statement.bindLong(i + 1, (Long) val);
            } else {
                statement.bindString(i + 1, (String) val);
            }
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

        if (cur.moveToFirst()) {
            //  query result has rows
            int colCount = cur.getColumnCount();
    
            // Build up JSON result object for each row
            do {
                ObjectNode row = objectMapper.createObjectNode();
                for (int i = 0; i < colCount; ++i) {
                    String key = cur.getColumnName(i);
    
                    // for old Android SDK remove lines from HERE:
                    if (android.os.Build.VERSION.SDK_INT >= 11) {
                        putValueBasedOnType(cur, row, key, i);
                        // to HERE.
                    } else {
                        row.put(key, cur.getString(i));
                    }
                }
    
                rowsArrayResult.add(row);
    
            } while (cur.moveToNext());
        }

        return rowsResult;
    }

    @SuppressLint("NewApi")
    private void putValueBasedOnType(Cursor cur, ObjectNode row, String key, int i) {
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
                row.put(key, JsonUtil.simpleBase64(cur.getBlob(i)));
                break;
        }        
    }

    private synchronized void processQueue() {

        log.d("processQueue");

        WebSqlTask task;
        while ((task = queue.peek()) != null) {

            if (currentTransactionId == null && task.getType() != WebSqlTask.Type.BeginTransaction) {
                log.d("skipping because of canceled transaction: %s", task.getTransactionId());
                queue.poll();
                continue;
            } else if (currentTransactionId != null && task.getTransactionId() != currentTransactionId) {
                log.d("skipping because we're already in another transaction. (#%s tried to line-jump #%s)", 
                        task.getTransactionId(), currentTransactionId);
                break;
            }
            queue.poll();
            currentTransactionId = task.getTransactionId();

            perform(task);
        }
    }

    private void perform(final WebSqlTask task) {
        log.d("perform(%s)", task);

        BasicSQLiteOpenHelper dbHelper = dbs.get(task.getDbName());
        if (dbHelper == null) {
            log.d("couldn't find db for name %s", task.getDbName());
            sendCallback(new JavascriptCallback(task.getErrorId(), "couldn't find db"));
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

        boolean markAsSuccessful = (Boolean) task.getArguments().get(0);

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
                sendCallback(new JavascriptCallback(task.getErrorId(), null, createClearCallbacksJson(task)));
            } else {
                sendCallback(new JavascriptCallback(task.getSuccessId(), null, createClearCallbacksJson(task)));
            }
            currentTransactionId = null;
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
            sendCallback(new JavascriptCallback(task.getSuccessId(), null));
        } catch (Exception e) {
            // couldn't even begin
            sendCallback(new JavascriptCallback(task.getErrorId(), null));
        }
    }
    
    private void registerCallbackIds(int transactionId, int successId, int errorId) {
        Set<Integer> callbackIds = transactionIdsToCallbackIds.get(transactionId);
        if (callbackIds == null) {
            callbackIds = new HashSet<Integer>();
            transactionIdsToCallbackIds.put(transactionId, callbackIds);
        }
        callbackIds.add(successId);
        callbackIds.add(errorId);
    }
    
    private CharSequence createClearCallbacksJson(WebSqlTask task) {
        // when a transaction is ended, we can safely remove all its accumulated callbacks
        Set<Integer> callbackIds = transactionIdsToCallbackIds.get(task.getTransactionId());
        if (callbackIds == null) {
            return "";
        }
        transactionIdsToCallbackIds.remove(task.getTransactionId());
        return new StringBuilder("PouchDroid.SQLiteNativeDB.clearCallbacks([").append(TextUtils.join(",", callbackIds)).append("]);");
    }
}
