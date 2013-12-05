package com.nolanlawson.couchdroid.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BasicSQLiteOpenHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    
    public BasicSQLiteOpenHelper(Context context, String name) {
        super(context, name, null, 1);
        this.db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // do nothing
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }
    
    /*
     *  Run a task for this database
     */
    public void post(SQLiteTask task) {
        // for now, just synchronous on the whole damn class to avoid "database locked" exceptions
        synchronized (BasicSQLiteOpenHelper.class) {
            task.run(db);
        }
    }
    
    public static interface SQLiteTask {
        public void run(SQLiteDatabase db);
    }

}
