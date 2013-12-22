package com.nolanlawson.couchdroid.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.pouch.PouchDB;
import com.nolanlawson.couchdroid.pouch.model.AllDocsInfo.Row;
import com.nolanlawson.couchdroid.util.SqliteUtil;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class CouchDroidMigrationTask {

    private static UtilLogger log = new UtilLogger(CouchDroidMigrationTask.class);

    private static final int BATCH_SIZE_HIMEM = 50;
    private static final int BATCH_SIZE_LOMEM = 5;
    private static final int SDK_INT_CUTOFF = 11; // assume devices < sdk 11 are low-memory (TODO: better method?)
    
    private List<SqliteTable> sqliteTables = new ArrayList<SqliteTable>();
    private Set<String> knownDocIds = new HashSet<String>();
    private SQLiteDatabase sqliteDatabase;
    private String userId;
    private String dbName;
    private MigrationProgressListener listener;
    private String pouchDBName;
    private PouchDB<GenericSqliteDocument> pouchDB;
    private String packageName;
    private CouchDroidRuntime runtime;
    
    private CouchDroidMigrationTask(CouchDroidRuntime runtime, SQLiteDatabase sqliteDatabase) {
        this.sqliteDatabase = sqliteDatabase;
        this.runtime = runtime;
    }
    
    private void init() {
        // TODO: assuming that the filename is a good enough id is not always ok
        int idx = sqliteDatabase.getPath().lastIndexOf('/');
        
        this.dbName = sqliteDatabase.getPath().substring(idx + 1);
        this.pouchDB = PouchDB.newPouchDB(GenericSqliteDocument.class, runtime, pouchDBName);
        
        Activity activity = runtime.getActivity();
        if (activity != null) {
            this.packageName = activity.getPackageName();
            this.listener = wrapListener(activity, listener);
        }
    }
    
    private MigrationProgressListener wrapListener(final Activity activity, 
            final MigrationProgressListener clientListener) {
        // wrap the client listener to avoid NPEs and ensure it runs on the UI thread
        return new MigrationProgressListener() {
            
            @Override
            public void onStart() {
                log.i("onStart()");
                activity.runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        clientListener.onStart();
                    }
                });
            }
            
            @Override
            public void onProgress(final String tableName, final int numRowsTotal, final int numRowsLoaded) {
                log.i("onProgress(%s, %s, %s)", tableName, numRowsTotal, numRowsLoaded);
                activity.runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        clientListener.onProgress(tableName, numRowsTotal, numRowsLoaded);
                    }
                });
            }
            
            @Override
            public void onEnd() {
                log.i("onEnd()");
                activity.runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        clientListener.onEnd();
                    }
                });                
            }
            
            @Override
            public void onDocsDeleted(final int numDocumentsDeleted) {
                log.i("onDocsDeleted(%s)", numDocumentsDeleted);
                activity.runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        clientListener.onDocsDeleted(numDocumentsDeleted);
                    }
                });
            }
        };
    }

    /**
     * Migrate all given SQLite tables from SQLite to PouchDB, reporting 
     * to the progress listener along the way.
     * 
     */
    public void start() {
        
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                listener.onStart();
                migrateSqliteTablesInBackground();
                processDeletes();
                listener.onEnd();
                return null;
            }
        }.execute(((Void)null));
    }
    
    private void migrateSqliteTablesInBackground() {
        for (SqliteTable sqliteTable : sqliteTables) {
            int totalNumRows = countNumRows(sqliteTable);
            List<SqliteColumn> columns = getColumnsForTable(sqliteTable);
            
            int offset = 0;
            List<GenericSqliteDocument> documentsBatch;
            for (;;) {
                documentsBatch = convertToDocuments(offset, columns, sqliteTable);
                
                if (documentsBatch.isEmpty()) {
                    break;
                }
                
                loadIntoPouchDB(documentsBatch);
                offset += documentsBatch.size();
                listener.onProgress(sqliteTable.getName(), totalNumRows, offset);
            }
        }        
    }

    private void loadIntoPouchDB(List<GenericSqliteDocument> documentsBatch) {
        // overwrite existing documents
        List<String> keys = new ArrayList<String>();
        for (GenericSqliteDocument document : documentsBatch) {
            keys.add(document.getPouchId());
        }
        knownDocIds.addAll(keys); // save for later
        List<Row<GenericSqliteDocument>> rows = pouchDB.allDocs(false, keys).getRows();
        for (int i = 0; i < rows.size(); i++) {
            Row<GenericSqliteDocument> row = rows.get(i);
            if (row.getValue() != null) { // else error is "not_found"
                documentsBatch.get(i).setPouchRev(row.getValue().getRev());
            } // else doc doesn't exist yet
        }
        pouchDB.bulkDocs(documentsBatch);
    }

    private List<GenericSqliteDocument> convertToDocuments(int offset, 
            List<SqliteColumn> sqliteColumns, SqliteTable sqliteTable) {
        
        List<GenericSqliteDocument> result = new ArrayList<GenericSqliteDocument>();
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
                String uuid = new StringBuilder()
                        .append(userId)
                        .append("~")
                        .append(dbName)
                        .append("~")
                        .append(sqliteTable.getName())
                        .append("~")
                        .append(id).toString();
                
                GenericSqliteDocument document = new GenericSqliteDocument();
                document.setAppPackage(packageName);
                document.setPouchId(uuid);
                document.setSqliteDB(dbName);
                document.setTable(sqliteTable.getName());
                document.setUser(userId);
                document.setContent(new LinkedHashMap<String, Object>());
                
                for (int i = 1; i < cursor.getColumnCount(); i++) {
                    
                    SqliteColumn sqliteColumn = sqliteColumns.get(i - 1);
                    
                    Object value = getValueFromCusor(sqliteColumn, cursor, i);
                    document.getContent().put(sqliteColumn.getName(), value);
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
    
    private void processDeletes() {
        // if any rows were deleted in sqlite, those should be migrated over as well
        List<GenericSqliteDocument> docsToDelete = new ArrayList<GenericSqliteDocument>();
        
        for (Row<GenericSqliteDocument> row : pouchDB.allDocs(false).getRows()) {
            
            if (!knownDocIds.contains(row.getId())) {
                // all we actually need here is the id and rev
                GenericSqliteDocument doc = new GenericSqliteDocument();
                doc.setPouchId(row.getId());
                doc.setPouchRev(row.getValue().getRev());
                docsToDelete.add(doc);
            }
        }
        
        for (GenericSqliteDocument docToDelete : docsToDelete) {
            pouchDB.remove(docToDelete);
        }
        listener.onDocsDeleted(docsToDelete.size());
    }
    
    private Object getValueFromCusor(SqliteColumn sqliteColumn, Cursor cursor, int i) {
        if (cursor.isNull(i)) {
            return null;
        }
        
        switch (sqliteColumn.getType()) {
            case SqliteUtil.FIELD_TYPE_BLOB:
                return cursor.getBlob(i);
            case SqliteUtil.FIELD_TYPE_FLOAT:
                return cursor.getFloat(i);
            case SqliteUtil.FIELD_TYPE_INTEGER:
                return cursor.getInt(i);
            case SqliteUtil.FIELD_TYPE_STRING:
            default:
                return cursor.getString(i);
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
    
    private List<SqliteColumn> getColumnsForTable(SqliteTable table) {
        
        Cursor cursor = null;
        
        List<SqliteColumn> result = new ArrayList<SqliteColumn>();
        
        try {
            cursor = sqliteDatabase.rawQuery(
                    new StringBuilder("pragma table_info(")
                        .append(table.getName())
                        .append(")").toString(), null);
            
            while (cursor.moveToNext()) {
                
                SqliteColumn sqliteColumn = new SqliteColumn();
                sqliteColumn.setName(cursor.getString(1));
                sqliteColumn.setType(SqliteUtil.getTypeForTypeName(cursor.getString(2)));
                
                result.add(sqliteColumn);
            }
            
            if (result.isEmpty()) {
                throw new IllegalArgumentException("No table defined in SQLite with name: " + table.getName());
            }
            
            return result;
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    public static class Builder {
        
        private CouchDroidMigrationTask migrationTask;
        
        public Builder(CouchDroidRuntime runtime, SQLiteDatabase sqliteDatabase) {
            this.migrationTask = new CouchDroidMigrationTask(runtime, sqliteDatabase);
        }
        
        public Builder addSqliteTable(String tableName, String... columnsToUseAsId) {
            
            SqliteTable sqliteTable = new SqliteTable();
            sqliteTable.setName(tableName);
            sqliteTable.setIdColumns(Arrays.asList(columnsToUseAsId));
            
            if (columnsToUseAsId.length == 0) {
                throw new IllegalArgumentException("You must supply as least one column to use as a unique ID.");
            }
            
            migrationTask.sqliteTables.add(sqliteTable);
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
            migrationTask.userId = userId;
            return this;
        }
        
        /**
         * Set the name of the PouchDB you'd like to migrate to.  This could be a local PouchDB if you name 
         * it e.g. "foo", or this could be a remote PouchDb if you set it to e.g. "http://mysite.com:5984".
         * 
         * <p/>In other words, this value will be passed to <code>new PouchDB(pouchDBName)</code>.
         * @param pouchDBName
         * @return
         */
        public Builder setPouchDBName(String pouchDBName) {
            migrationTask.pouchDBName = pouchDBName;
            return this;
        }
        
        /**
         * Listen for progress events, so you can report how far along syncing is to the user.
         * @param listener
         * @return
         */
        public Builder setProgressListener(MigrationProgressListener listener) {
            migrationTask.listener = listener;
            return this;
        }
        
        public CouchDroidMigrationTask build() {
            if (migrationTask.sqliteTables.isEmpty()) {
                throw new IllegalArgumentException(
                        "You must supply at least one sqlite table definition using addSqliteTable()");
            } else if (TextUtils.isEmpty(migrationTask.userId)) {
                throw new IllegalArgumentException("You must supply a userId using setUserId().");
            } else if (TextUtils.isEmpty(migrationTask.pouchDBName)) {
                throw new IllegalArgumentException("You must supply a pouchDBName using setPouchDBName().");
            }
            migrationTask.init();
            return migrationTask;
        }
    }
}
