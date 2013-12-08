package com.nolanlawson.couchdroid.migration;

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
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.migration.CouchDroidProgressListener.ProgressType;
import com.nolanlawson.couchdroid.util.SqliteUtil;
import com.nolanlawson.couchdroid.util.UtilLogger;

@SuppressLint("SetJavaScriptEnabled")
public class CouchDroidMigrationTask {

    private static UtilLogger log = new UtilLogger(CouchDroidMigrationTask.class);

    private static final int BATCH_SIZE_HIMEM = 50;
    private static final int BATCH_SIZE_LOMEM = 5;
    private static final int SDK_INT_CUTOFF = 11; // assume devices < sdk 11 are low-memory (TODO: better method?)
    
    private List<SqliteTableInfo> sqliteTableInfos = new ArrayList<SqliteTableInfo>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private SQLiteDatabase sqliteDatabase;
    private String userId;
    private String couchdbUrl;
    private String dbName;
    private CouchDroidProgressListener clientListener;
    private CouchDroidProgressListener listener;
    private CouchDroidRuntime runtime;
    
    private CouchDroidMigrationTask(CouchDroidRuntime runtime, SQLiteDatabase sqliteDatabase) {
        this.runtime = runtime;
        this.sqliteDatabase = sqliteDatabase;
        
        // TODO: assuming that the filename is a good enough id is not always ok
        int idx = sqliteDatabase.getPath().lastIndexOf('/');
        
        this.dbName = sqliteDatabase.getPath().substring(idx + 1);
        this.listener = wrapClientListener();
        
        runtime.addListener(listener);
    }
    
    private void setSqliteTables(List<SqliteTable> sqliteTables) {
        
        // fetch basic table information
        for (SqliteTable sqliteTable : sqliteTables) {
            sqliteTableInfos.add(new SqliteTableInfo(
                    sqliteTable, 
                    getColumnsForTable(sqliteTable.getName()), 
                    countNumRows(sqliteTable)));
        }
    }
    
    public void start() {
        try {
            initPouchDBHelper();
            migrateSqliteTable(sqliteTableInfos.get(0), 0);
        } catch (Exception e) {
            // shouldn't happen
            log.e(e, "very unexpected exception");
            throw new RuntimeException(e);
        }
    }
    
    private CouchDroidProgressListener wrapClientListener() {
        // override the client listener to add out own
        
        return new CouchDroidProgressListener(){

            @Override
            public void onProgress(ProgressType type, String tableName, int numRowsTotal, int numRowsLoaded) {
                log.d("onProgress(%s,  %s, %s, %s", type, tableName, numRowsTotal, numRowsLoaded);
                try {
                    if (clientListener != null) {
                        clientListener.onProgress(type, tableName, numRowsTotal, numRowsLoaded);
                    }
                    
                    if (type == ProgressType.Copy) {
                        
                        // find sqliteTable by name
                        SqliteTableInfo sqliteTableInfo = null;
                        int idx = -1;
                        for (int i = 0, len = sqliteTableInfos.size(); i < len; i++) {
                            SqliteTableInfo candidate = sqliteTableInfos.get(i);
                            if (candidate.table.getName().equals(tableName)) {
                                sqliteTableInfo = candidate;
                                idx = i;
                                break;
                            }
                        }
                        
                        if (sqliteTableInfo == null) {
                            throw new IllegalStateException("couldn't find sqlite table for name: " + tableName);
                        }
                        
                        if (numRowsLoaded == numRowsTotal) {
                            // copying complete
                            if (idx < sqliteTableInfos.size() - 1) {
                                // copy next table
                                migrateSqliteTable(
                                        sqliteTableInfos.get(idx + 1), 
                                        0);
                            } else {
                                // begin replication of all tables
                                log.i("Trying to sync to CouchDB %s... (If this hangs, CouchDB may be unreachable!)", couchdbUrl);
                                replicate();
                            }
                        } else {
                            // copy next batch
                            migrateSqliteTable(sqliteTableInfo, 
                                    numRowsLoaded);
                        }
                    }
                } catch (Exception e) {
                    // shouldn't happen
                    log.e(e, "very unexpected exception");
                    throw new RuntimeException(e);
                }
            }
        };
    }
    
    private void replicate() {
        try {
            runtime.loadJavascript(new StringBuilder("CouchDroid.pouchDBHelper.syncAll(")
            .append(createFinalReportProgressJs())
            .append(");"));
        } catch (IOException e) {
            // should not happen
            log.e(e, "unexpected");
        }
    }

    /*
     * migrate sqlite tables from sqlite to PouchDB
     */
    private void migrateSqliteTable(final SqliteTableInfo sqliteTableInfo, final int offset) {
        
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    
                    if (offset == 0) {
                        // notify listener at zero
                        runtime.loadJavascript(createInitReportProgressJs(
                                sqliteTableInfo.table, 
                                sqliteTableInfo.totalRowCount));
                    }
                        
                    loadBatchFromTable(sqliteTableInfo.table, sqliteTableInfo.columns, offset, 
                            sqliteTableInfo.totalRowCount);
                } catch (IOException e) {
                    log.e(e, "unexpected"); // shouldn't happen
                }
                
                return null;
            }
        }.execute(((Void)null));
    }
    
    private void loadBatchFromTable(SqliteTable sqliteTable, 
            List<SqliteColumn> sqliteColumns, int offset, int totalNumRows) throws IOException {
        log.d("load batch for table: %s, offset %s", sqliteTable, offset);
        
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
        
        Activity activity = runtime.getActivity();
        if (activity == null) {
            return; // app closed
        }
        
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
            return; // hit exactly BATCH_SIZE * n documents
        }

        CharSequence reportProgress = createReportProgressJs(sqliteTable, totalNumRows, offset);
        
        log.d("report progress js is %s", reportProgress);
        log.d("loadBatchIntoPouchdb: %s docs", documents.size());
        loadBatchIntoPouchdb(batch, reportProgress);
        log.d("Loaded %d objects into pouchdb", documents.size());
    }

    private void initPouchDBHelper() throws IOException {
        
        Activity activity = runtime.getActivity();
        if (activity == null) {
            return; // app closed
        }
        
        // ensure that pouchdb talks to the same database every time, i.e. uniquify its name
        StringBuilder internalPouchdbName = new StringBuilder()
            .append(activity.getPackageName())
            .append("_")
            .append(userId)
            .append("_")
            .append(dbName);
        
        runtime.loadJavascript(new StringBuilder()
            .append("CouchDroid.pouchDBHelper = new CouchDroid.PouchDBHelper(")
            .append(objectMapper.writeValueAsString(internalPouchdbName))
            .append(",")
            .append(objectMapper.writeValueAsString(couchdbUrl))
            .append(",")
            .append(UtilLogger.DEBUG_MODE)
            .append(");"));
        
    }
    
    private CharSequence createFinalReportProgressJs() throws IOException {
        return new StringBuilder()
            .append("function(){ProgressReporter.reportProgress(")
            .append(objectMapper.writeValueAsString(ProgressType.Sync.name()))
            .append(",null, 0, 0);}");
    }
    
    private CharSequence createInitReportProgressJs(SqliteTable sqliteTable, int totalNumRows) throws IOException {
        
        return new StringBuilder()
            .append("ProgressReporter.reportProgress(")
            .append(objectMapper.writeValueAsString(ProgressType.Init.name()))
            .append(",")
            .append(objectMapper.writeValueAsString(sqliteTable.getName()))
            .append(",")
            .append(totalNumRows)
            .append(", 0);");
        
    }

    private CharSequence createReportProgressJs(SqliteTable sqliteTable, int totalNumRows, int numRowsLoaded) 
            throws IOException {
        return new StringBuilder()
                .append("function(numLoaded){ProgressReporter.reportProgress(")
                .append(objectMapper.writeValueAsString(ProgressType.Copy.name()))
                .append(",")
                .append(objectMapper.writeValueAsString(sqliteTable.getName()))
                .append(",")
                .append(totalNumRows)
                .append(",numLoaded + ")
                .append(numRowsLoaded)
                .append(");}");
    }

    private void loadBatchIntoPouchdb(ObjectNode docsBatch,
            CharSequence reportProgress) throws IOException {

        // call the PouchDBHelper, set the db id, load the documents
        StringBuilder js = new StringBuilder()
                .append("CouchDroid.pouchDBHelper.putAll(")
                .append(objectMapper.writeValueAsString(docsBatch))
                .append(",")
                .append(reportProgress)
                .append(");");
        
        log.d("javascript is: %s", js);
        runtime.loadJavascript(js);
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
                .append(" limit ").append(getBatchSize())
                .append(" offset ").append(offset);
            
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
    
    private int getBatchSize() {
        return Build.VERSION.SDK_INT < SDK_INT_CUTOFF ? BATCH_SIZE_LOMEM : BATCH_SIZE_HIMEM;
    }

    private int countNumRows(SqliteTable sqliteTable) {
        Cursor cursor = null;
        
        try {
            cursor = sqliteDatabase.rawQuery(
                    new StringBuilder("select count(*) from ")
                        .append(sqliteTable.getName())
                        .toString(), null);
            
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
    
    public static class Builder {
        
        private CouchDroidMigrationTask couchdbSync;
        private List<SqliteTable> sqliteTables = new ArrayList<SqliteTable>();
        
        public Builder(CouchDroidRuntime runtime, SQLiteDatabase sqliteDatabase) {
            this.couchdbSync = new CouchDroidMigrationTask(runtime, sqliteDatabase);
        }
        
        public Builder addSqliteTable(String tableName, String... columnsToUseAsId) {
            
            SqliteTable sqliteTable = new SqliteTable();
            sqliteTable.setName(tableName);
            sqliteTable.setIdColumns(Arrays.asList(columnsToUseAsId));
            
            if (columnsToUseAsId.length == 0) {
                throw new IllegalArgumentException("You must supply as least one column to use as a unique ID.");
            }
            
            sqliteTables.add(sqliteTable);
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
        
        public CouchDroidMigrationTask build() {
            if (sqliteTables.isEmpty()) {
                throw new IllegalArgumentException(
                        "You must supply at least one sqlite table definition using addSqliteTable()");
            } else if (TextUtils.isEmpty(couchdbSync.userId)) {
                throw new IllegalArgumentException("You must supply a userId using setUserId().");
            } else if (TextUtils.isEmpty(couchdbSync.couchdbUrl)) {
                throw new IllegalArgumentException("You must supply a valid couchDB url using setCouchdbUrl()");
            }
            couchdbSync.setSqliteTables(sqliteTables);
            return couchdbSync;
        }
    }
    
    private static class SqliteTableInfo {
        
        private SqliteTableInfo(SqliteTable table, List<SqliteColumn> columns, int totalRowCount) {
            this.table = table;
            this.columns = columns;
            this.totalRowCount = totalRowCount;
        }

        SqliteTable table;
        List<SqliteColumn> columns;
        int totalRowCount;
    }
}
