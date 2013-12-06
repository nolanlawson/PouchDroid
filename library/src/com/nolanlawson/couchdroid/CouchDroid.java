package com.nolanlawson.couchdroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.nolanlawson.couchdroid.CouchDroidProgressListener.ProgressType;
import com.nolanlawson.couchdroid.sqlite.SQLiteJavascriptInterface;
import com.nolanlawson.couchdroid.util.ResourceUtil;
import com.nolanlawson.couchdroid.util.SqliteUtil;
import com.nolanlawson.couchdroid.util.UtilLogger;
import com.nolanlawson.couchdroid.xhr.XhrJavascriptInterface;

@SuppressLint("SetJavaScriptEnabled")
public class CouchDroid {

    private static UtilLogger log = new UtilLogger(CouchDroid.class);

    private static final String TAG_WEBVIEW = "CouchDroidWebView";
    
    private static final boolean USE_WEINRE = true;
    private static final boolean USE_MINIFIED_POUCH = true;
    private static final String WEINRE_URL = "http://192.168.10.110:8080";
    
    
    private static final int BATCH_SIZE = 100;
    
    private Activity activity;
    private List<SqliteTable> sqliteTables = new ArrayList<SqliteTable>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private WebView webView;
    private SQLiteDatabase sqliteDatabase;
    private String userId;
    private String couchdbUrl;
    private String dbName;
    private SQLiteJavascriptInterface sqliteJavascriptInterface;
    private CouchDroidProgressListener listener;
    private CouchDroidProgressListener clientListener;
    
    private CouchDroid(Activity activity, SQLiteDatabase sqliteDatabase) {
        this.activity = activity;
        this.sqliteDatabase = sqliteDatabase;
        
        // TODO: assuming that the filename is a good enough id is not always ok
        int idx = sqliteDatabase.getPath().lastIndexOf('/');
        
        this.dbName = sqliteDatabase.getPath().substring(idx + 1);
        
        this.listener = wrapClientListener();
    }
    
    private CouchDroidProgressListener wrapClientListener() {
        // override the client listener to add out own
        
        return new CouchDroidProgressListener(){

            @Override
            public void onProgress(ProgressType type, String tableName, int numRowsTotal, int numRowsLoaded) {
                log.i("progress: (%s, %s, %s, %s)", type, tableName, numRowsTotal, numRowsLoaded);
                if (clientListener != null) {
                    clientListener.onProgress(type, tableName, numRowsTotal, numRowsLoaded);
                }
                
                if (type == ProgressType.Copy && numRowsLoaded == numRowsTotal) {
                    log.i("Trying to sync to CouchDB %s... (If this hangs, CouchDB may be unreachable!)", couchdbUrl);
                    replicate();
                }
            }
        };
    }
    
    private void replicate() {
        loadJavascript(new StringBuilder("window.pouchDBHelper.syncAll(function(){});").toString());
    }

    /**
     * Start a new CouchDroid sync process.
     * 
     * You MUST call this on the main application thread.  use runOnUiThread if you're not sure.
     */
    public void start() {
        initWebView();
        
        log.d("attempting to load javascript");
        loadJavascript("var DEBUG_MODE = " + UtilLogger.DEBUG_MODE + ";" 
         + ResourceUtil.loadTextFile(activity, R.raw.ecmascript_shims) 
         + ResourceUtil.loadTextFile(activity, R.raw.sqlite_native_interface) 
         + ResourceUtil.loadTextFile(activity, R.raw.xhr_native_interface) 
         + "var fakeLocalStorage = {};" 
         + (ResourceUtil.loadTextFile(activity, USE_MINIFIED_POUCH ? R.raw.pouchdb_min : R.raw.pouchdb)
                        .replaceAll("\\bXMLHttpRequest\\b", "NativeXMLHttpRequest")
                        .replaceAll("\\blocalStorage\\b", "fakeLocalStorage")
                        .replaceAll("\\bopenDatabase\\b", "openNativeDatabase")) 
         + ResourceUtil.loadTextFile(activity, R.raw.pouchdb_helper) 
         + "window.console.log('PouchDB is: ' + typeof PouchDB);" 
         + "window.console.log('PouchDBHelper is: ' + typeof PouchDBHelper);");
        
        migrateSqliteTables();
        
    }
    
    /**
     * If you're really interested in the names of the databases that pouchdb has created, you're free 
     * to see them.  But you probably shouldn't be messing with this!
     * @return list of the database names, if any
     */
    public List<String> getPouchDatabaseNames() {
        return sqliteJavascriptInterface.getDbNames();
    }
    
    /*
     * migrate sqlite tables from sqlite to PouchDB
     */
    private void migrateSqliteTables() {
        
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                
                for (SqliteTable sqliteTable : sqliteTables) {
                    try {
                        loadTable(sqliteTable);
                    } catch (IOException e) {
                        log.e(e, "unexpected"); // shouldn't happen
                    }
                }
                
                return null;
            }
            
            private void loadTable(SqliteTable sqliteTable) throws IOException {
                log.d("loadTable: %s", sqliteTable);
                List<SqliteColumn> sqliteColumns = getColumnsForTable(sqliteTable.getName());
                
                int offset = 0;
                
                int totalNumRows = countNumRows(sqliteTable);
                
                notifyListenerAtZero(sqliteTable, totalNumRows);
                
                initPouchDBHelper();
                
                while (true) {
                    
                    /**
                     * the resulting, compressed batch object looks like this:
                     * {
                     *  table : "MyTable",
                     *  user : "Bobby B",
                     *  columns : ["id", "name", "date", ...],
                     *  uuids:['foo','bar','baz'...],
                     *  docs : [[..values...], [...values...]...]
                     *  }
                     */
                    
                    ObjectNode batch = objectMapper.createObjectNode();
                    batch.put("table", sqliteTable.getName());
                    batch.put("sqliteDB", dbName);
                    batch.put("appPackage", activity.getPackageName());
                    batch.put("user", userId);
                    ArrayNode columns = batch.putArray("columns");
                    
                    for (SqliteColumn column : sqliteColumns) {
                        columns.add(column.getName());
                    }
                    ArrayNode documents = batch.putArray("docs");
                    ArrayNode uuids = batch.putArray("uuids");
                    
                    convertBatchToJsonList(documents, uuids, offset, sqliteColumns, sqliteTable);
                    
                    if (documents.size() == 0) {
                        break;
                    }

                    CharSequence javascriptCallback = createReportProgressCallback(offset, totalNumRows, sqliteTable);
                    
                    log.d("loadBatchIntoPouchdb: %s docs", documents.size());
                    loadBatchIntoPouchdb(batch, javascriptCallback);
                    log.d("Loaded %d objects into pouchdb", documents.size());
                    
                    if (documents.size() < BATCH_SIZE) {
                        break;
                    }
                    
                    offset += BATCH_SIZE;
                }
            }

            private void initPouchDBHelper() throws IOException {
                
                // ensure that pouchdb talks to the same database every time, i.e. uniquify its name
                StringBuilder internalPouchdbName = new StringBuilder()
                    .append(activity.getPackageName())
                    .append("_")
                    .append(userId)
                    .append("_")
                    .append(dbName);
                
                loadJavascript(new StringBuilder()
                    .append("window.pouchDBHelper = new PouchDBHelper(")
                    .append(objectMapper.writeValueAsString(internalPouchdbName))
                    .append(",")
                    .append(objectMapper.writeValueAsString(couchdbUrl))
                    .append(",")
                    .append(UtilLogger.DEBUG_MODE)
                    .append(");"));
                
            }

            private void notifyListenerAtZero(final SqliteTable sqliteTable, final int totalNumRows) {
                try {
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                try {
                                    listener.onProgress(ProgressType.Init, sqliteTable.getName(), totalNumRows, 0);
                                } catch (Exception e) {
                                    log.e(e, "progress listener threw exception");
                                }
                                
                            }
                        });
                        
                    }
                } catch (Exception e) {
                    log.e(e, "progress listener threw exception");
                }                
            }

            private CharSequence createReportProgressCallback(int offset, int totalNumRows,
                    SqliteTable sqliteTable) throws IOException {
                return new StringBuilder()
                        .append(",function(numLoaded){ProgressReporter.reportProgress(")
                        .append(objectMapper.writeValueAsString(ProgressType.Copy.name()))
                        .append(",")
                        .append(objectMapper.writeValueAsString(sqliteTable.getName()))
                        .append(",")
                        .append(totalNumRows)
                        .append(",numLoaded + ")
                        .append(offset)
                        .append(");}");
            }

            private void loadBatchIntoPouchdb(ObjectNode docsBatch,
                    CharSequence javascriptCallback) throws IOException {

                // call the PouchDBHelper, set the db id, load the documents
                StringBuilder js = new StringBuilder()
                        .append("window.pouchDBHelper.putAll(")
                        .append(objectMapper.writeValueAsString(docsBatch))
                        .append(javascriptCallback)
                        .append(");");
                
                log.d("javascript is: %s", js);
                loadJavascript(js);
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
            
        }.execute(((Void)null));
    }


    private void convertBatchToJsonList(ArrayNode documents, ArrayNode uuids, int offset, 
            List<SqliteColumn> sqliteColumns, SqliteTable sqliteTable) {
        
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
                
                // generate uuid (i.e. the _id for couch)
                String id = cursor.getString(0);
                String uid = new StringBuilder()
                        .append(userId)
                        .append("~")
                        .append(dbName)
                        .append("~")
                        .append(sqliteTable.getName())
                        .append("~")
                        .append(id).toString();
                
                uuids.add(uid);
                
                ArrayNode document = objectMapper.createArrayNode();
                
                for (int i = 1; i < cursor.getColumnCount(); i++) {
                    
                    SqliteColumn sqliteColumn = sqliteColumns.get(i - 1);
                    
                    if (cursor.isNull(i)) {
                        document.addNull();
                        continue;
                    }
                    
                    switch (sqliteColumn.getType()) {
                        case SqliteUtil.FIELD_TYPE_BLOB:
                            document.add(cursor.getBlob(i));
                            break;
                        case SqliteUtil.FIELD_TYPE_FLOAT:
                            document.add(cursor.getFloat(i));
                            break;
                        case SqliteUtil.FIELD_TYPE_INTEGER:
                            document.add(cursor.getInt(i));
                            break;
                        case SqliteUtil.FIELD_TYPE_STRING:
                        default:
                            document.add(cursor.getString(i));
                            break;
                    }
                }
                
                documents.add(document);
            }
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    private int countNumRows(SqliteTable sqliteTable) {
        Cursor cursor = null;
        
        try {
            cursor = sqliteDatabase.rawQuery(
                    new StringBuilder("select count(*) from ")
                        .append(sqliteTable.getName())
                        .append(";").toString(), null);
            
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
            
            return 0;
            
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
        
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        
        webView = (WebView) viewGroup.findViewWithTag(TAG_WEBVIEW);
        
        log.d("creating new webivew");
        webView = new WebView(activity);
        webView.setTag(TAG_WEBVIEW);
        webView.setVisibility(View.GONE);
        
        viewGroup.addView(webView);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDatabaseEnabled(true); // we're overriding websql, but we still need to set this
        webView.getSettings().setDomStorageEnabled(true); // pouch needs to call localStorage for some reason
        webView.getSettings().setAllowContentAccess(true);
        
        webView.setWebChromeClient(new MyWebChromeClient());
        
        sqliteJavascriptInterface = new SQLiteJavascriptInterface(activity, webView);
        
        webView.addJavascriptInterface(sqliteJavascriptInterface, "SQLiteJavascriptInterface");
        webView.addJavascriptInterface(new XhrJavascriptInterface(webView), "XhrJavascriptInterface");
        webView.addJavascriptInterface(new ProgressReporter(activity, listener), "ProgressReporter");
        
        final String html = new StringBuilder("<html><body>")
                .append(USE_WEINRE
                        ? "<script src='" + WEINRE_URL +"/target/target-script-min.js#anonymous'></script>"
                        : "")
                .append("</body></html>").toString();
        
        // fake url to allow loading of weinre
        webView.loadDataWithBaseURL("http://localhost:9362", html, "text/html", "UTF-8", null);
        
        log.d("loaded webview data: %s", html);
    }
    
    public void close() {
        log.d("close()");
        if (sqliteJavascriptInterface != null) {
            sqliteJavascriptInterface.close();
        }
        sqliteJavascriptInterface = null;
        activity = null; // release context resources
        webView.loadUrl("about:blank");
    }
    
    private class MyWebChromeClient extends WebChromeClient {
        
        @Override
        @Deprecated
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            //log.d(message);
        }
    }
    
    public static class Builder {
        
        private CouchDroid couchdbSync;
        
        private Builder(Activity activity, SQLiteDatabase sqliteDatabase) {
            this.couchdbSync = new CouchDroid(activity, sqliteDatabase);
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
         * Set a unique userId.  It's required, so that you can properly set up CouchDB to enforce read-only/write-only
         * access for documents (see the README.md for details).
         * 
         * @param userId
         * @return
         */
        public Builder setUserId(String userId) {
            couchdbSync.userId = userId;
            return this;
        }
        /**
         * Set a CouchDB database url to sync to.  User credentials should be set here as well.
         * (Hopefully you're using SSH!)
         * 
         * Here's an example URL:
         * 
         * "https://myusername:mypassword@localhost:5984/mydb"
         * 
         * @param userId
         * @return
         */
        public Builder setCouchdbUrl(String couchdbUrl) {
            couchdbSync.couchdbUrl = couchdbUrl;
            return this;
        }
        
        /**
         * Listen for progress events, so you can report how far along syncing is to the user.
         * @param listener
         * @return
         */
        public Builder setProgressListener(CouchDroidProgressListener listener) {
            couchdbSync.clientListener = listener;
            return this;
        }
        
        public CouchDroid build() {
            if (couchdbSync.sqliteTables.isEmpty()) {
                throw new IllegalArgumentException(
                        "You must supply at least one sqlite table definition using addSqliteTable()");
            } else if (TextUtils.isEmpty(couchdbSync.userId)) {
                throw new IllegalArgumentException("You must supply a userId using setUserId().");
            } else if (TextUtils.isEmpty(couchdbSync.couchdbUrl)) {
                throw new IllegalArgumentException("You must supply a valid couchDB url using setCouchdbUrl()");
            }
            return couchdbSync;
        }
    }
}
