package com.nolanlawson.couchdbsync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.nolanlawson.couchdbsync.sqlite.SQLiteJavascriptInterface;
import com.nolanlawson.couchdbsync.util.ResourceUtil;
import com.nolanlawson.couchdbsync.util.SqliteUtil;
import com.nolanlawson.couchdbsync.util.UtilLogger;

@SuppressLint("SetJavaScriptEnabled")
public class CouchdbSync {

    private static UtilLogger log = new UtilLogger(CouchdbSync.class);

    private static final int BATCH_SIZE = 100;
    
    private Activity activity;
    private List<SqliteTable> sqliteTables = new ArrayList<SqliteTable>();
    private WebView webView;
    private SQLiteDatabase sqliteDatabase;
    private String dbId;
    private String dbName;
    private SQLiteJavascriptInterface sqliteJavascriptInterface;
    
    private CouchdbSync(Activity activity, SQLiteDatabase sqliteDatabase) {
        this.activity = activity;
        this.sqliteDatabase = sqliteDatabase;
        
        // TODO: assuming that the filename is a good enough id is not always ok
        int idx = sqliteDatabase.getPath().lastIndexOf('/');
        
        this.dbName = sqliteDatabase.getPath().substring(idx + 1);
    }
    
    public void start() {
        initWebView();
        
        log.d("attempting to load javascript");
        loadJavascript("DEBUG_MODE = " + UtilLogger.DEBUG_MODE + ";");
        //loadJavascript("DEBUG_MODE = false;");
        loadJavascript(ResourceUtil.loadTextFile(activity, R.raw.ecmascript_shims));
        loadJavascript(ResourceUtil.loadTextFile(activity, R.raw.sqlite_native_interface));
        loadJavascript(ResourceUtil.loadTextFile(activity, R.raw.pouchdb));
        loadJavascript(ResourceUtil.loadTextFile(activity, R.raw.pouchdb_helper));
        loadJavascript("window.console.log('PouchDB is: ' + typeof PouchDB)");
        loadJavascript("window.console.log('PouchDBHelper is: ' + typeof PouchDBHelper)");
        
        migrateSqliteTables();
        
    }
    
    /*
     * migrate sqlite tables from sqlite to PouchDB
     */
    private void migrateSqliteTables() {
        
        new AsyncTask<Void, CouchdbSyncProgress, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                
                for (SqliteTable sqliteTable : sqliteTables) {
                    loadTable(sqliteTable);
                }
                
                return null;
            }
            
            private void loadTable(SqliteTable sqliteTable) {
                log.d("loadTable: %s", sqliteTable);
                List<SqliteColumn> sqliteColumns = getColumnsForTable(sqliteTable.getName());
                
                int offset = 0;
                ObjectMapper objectMapper = new ObjectMapper();
                while (true) {
                    ArrayList<Object> currentBatch = convertBatchToJsonList(
                            offset, sqliteColumns, sqliteTable, objectMapper);
                     
                    if (currentBatch.isEmpty()) {
                        break;
                    }

                    loadBatchIntoPouchdb(currentBatch, objectMapper);
                    if (currentBatch.size() < BATCH_SIZE) {
                        break;
                    }
                    
                    offset += BATCH_SIZE;
                }
            }

            private void loadBatchIntoPouchdb(ArrayList<Object> docsBatch, ObjectMapper objectMapper) {
                
                log.d("loadBatchIntoPouchdb: %s docs", docsBatch.size());
                
                try {
                    StringBuilder js = new StringBuilder()
                            .append("var pouchDBHelper = new PouchDBHelper('")
                            .append(activity.getPackageName())
                            .append(dbId == null ? "" : dbId)
                            .append("',")
                            .append(UtilLogger.DEBUG_MODE)
                            .append(");")
                            .append("pouchDBHelper.putAll(")
                            .append(objectMapper.writeValueAsString(docsBatch))
                            .append(");");
                    
                    log.d("javascript is: %s", js);
                    loadJavascriptWrapped(js);
                    log.d("Loaded %d objects into pouchdb", docsBatch.size());
                } catch (IOException e) {
                    // shouldn't happen
                    log.e(e, "unexpected exception");
                }
                
            }

            @Override
            protected void onProgressUpdate(CouchdbSyncProgress... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
            
        }.execute(((Void)null));
    }
    
    private ArrayList<Object> convertBatchToJsonList(int offset, 
            List<SqliteColumn> sqliteColumns, SqliteTable sqliteTable, ObjectMapper objectMapper) {
        
        ArrayList<Object> result = new ArrayList<Object>();
        Cursor cursor = null;
        try {
            
            StringBuilder sql = new StringBuilder("select ");
            
            // concat ids into single string
            for (int i = 0, len = sqliteTable.getIdColumns().size(); i < len; i++) {
                String idColumn = sqliteTable.getIdColumns().get(i);
                if (i > 0) {
                    sql.append(" || ");
                }
                sql.append(idColumn);
            }
            sql.append(", * from ")
            .append(sqliteTable.getName())
            .append(" limit ").append(BATCH_SIZE)
            .append(" offset ").append(offset)
            .append(";");
            
            cursor = sqliteDatabase.rawQuery(sql.toString(), null);
            
            while (cursor.moveToNext()) {
                
                ObjectNode document = objectMapper.createObjectNode();
                
                String id = cursor.getString(0);
                String uid = new StringBuilder()
                        .append(dbName)
                        .append("~")
                        .append(sqliteTable.getName())
                        .append("~")
                        .append(id).toString();
                
                document.put("_id", uid);
                
                for (int i = 1; i < cursor.getColumnCount(); i++) {
                    
                    SqliteColumn sqliteColumn = sqliteColumns.get(i - 1);
                    String columnName = sqliteColumn.getName();
                    
                    if (columnName.startsWith("_")) {
                        // you're not allowed to use this, since underscore-prefixed fields are reserved in Couchdb
                        columnName = new StringBuilder(columnName).replace(0, 1, "!reserved_android_id!").toString();
                    }
                    
                    if (cursor.isNull(i)) {
                        document.putNull(columnName);
                        continue;
                    }
                    
                    switch (sqliteColumn.getType()) {
                        case SqliteUtil.FIELD_TYPE_BLOB:
                            document.put(columnName, cursor.getBlob(i));
                            break;
                        case SqliteUtil.FIELD_TYPE_FLOAT:
                            document.put(columnName, cursor.getFloat(i));
                            break;
                        case SqliteUtil.FIELD_TYPE_INTEGER:
                            document.put(columnName, cursor.getInt(i));
                            break;
                        case SqliteUtil.FIELD_TYPE_STRING:
                        default:
                            document.put(columnName, cursor.getString(i));
                            break;
                    }
                }
                
                result.add(document);
            }
            
            return result;
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private List<SqliteColumn> getColumnsForTable(String tableName) {
        Cursor cursor = null;
        
        List<SqliteColumn> result = new ArrayList<SqliteColumn>();
        
        try {
            cursor = sqliteDatabase.rawQuery(
                    new StringBuilder("pragma table_info(")
                        .append(tableName)
                        .append(")").toString(), null);
            
            while (cursor.moveToNext()) {
                
                SqliteColumn sqliteColumn = new SqliteColumn();
                sqliteColumn.setName(cursor.getString(1));
                sqliteColumn.setType(SqliteUtil.getTypeForTypeName(cursor.getString(2)));
                
                result.add(sqliteColumn);
            }
            
            if (result.isEmpty()) {
                throw new IllegalArgumentException("No table defined in SQLite with name: " + tableName);
            }
            
            return result;
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    private void loadJavascriptWrapped(CharSequence javascript) {
        
        loadJavascript(new StringBuilder()
                .append("(function(){")
                .append(javascript)
                .append("})();"));
    }

    private void loadJavascript(final CharSequence javascript) {
        webView.post(new Runnable() {
            
            @Override
            public void run() {
                webView.loadUrl(new StringBuilder("javascript:")
                        .append("document.addEventListener('DOMContentLoaded', function() {")
                        .append(javascript)
                        .append("});")
                        .append(javascript).toString());
            }
        });
    }
    
    @SuppressLint("NewApi")
    private void initWebView() {
        webView = new WebView(activity);
        webView.setVisibility(View.GONE);
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        viewGroup.addView(webView);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        if (UtilLogger.DEBUG_MODE) {    
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        
        webView.setWebChromeClient(new MyWebChromeClient());
        
        sqliteJavascriptInterface = new SQLiteJavascriptInterface(activity, webView);
        webView.addJavascriptInterface(sqliteJavascriptInterface, "SQLiteJavascriptInterface");
        
        
        String html = new StringBuilder("<html><body>")
                .append(UtilLogger.DEBUG_MODE 
                        ? "<script src='http://192.168.10.110:8080/target/target-script-min.js#anonymous'></script>"
                        : "")
                .append("</body></html>").toString();
        // fake url to allow loading of weinre
        webView.loadDataWithBaseURL("http://localhost:9340",html, "text/html", "UTF-8", "http://localhost:9340");
        
        if (UtilLogger.DEBUG_MODE) {
            // give me a chance to connect with weinre
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.d("loaded webview data: %s", html);
    }
    
    public void close() {
        if (sqliteJavascriptInterface != null) {
            sqliteJavascriptInterface.close();
        }
        sqliteJavascriptInterface = null;
        activity = null; // release resources
        
    }
    
    private class MyWebChromeClient extends WebChromeClient {
        
        @Override
        @Deprecated
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            //log.d(message);
        }
    }
    
    public static class Builder {
        
        private CouchdbSync couchdbSync;
        
        private Builder(Activity activity, SQLiteDatabase sqliteDatabase) {
            this.couchdbSync = new CouchdbSync(activity, sqliteDatabase);
        }
        
        public static Builder create(Activity activity, SQLiteDatabase sqliteDatabase) {
            return new Builder(activity, sqliteDatabase);
        }
        
        public Builder addSqliteTable(String tableName, String... columnsToUseAsId) {
            
            SqliteTable sqliteTable = new SqliteTable();
            sqliteTable.setName(tableName);
            sqliteTable.setIdColumns(Arrays.asList(columnsToUseAsId));
            
            if (columnsToUseAsId.length == 0) {
                throw new IllegalArgumentException("You must supply as least one column to use as a unique ID.");
            }
            
            couchdbSync.sqliteTables.add(sqliteTable);
            return this;
        }
        
        /**
         * Set a unique id, else we'll just use one package-wide id.
         * @param id
         * @return
         */
        public Builder setDatabaseId(String id) {
            couchdbSync.dbId = id;
            return this;
        }
        
        public CouchdbSync build() {
            if (couchdbSync.sqliteTables.isEmpty()) {
                throw new IllegalStateException(
                        "You must supply at least one sqlite table definition using addSqliteTable()!");
            }
            return couchdbSync;
        }
    }
}
