package com.example.example1;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.example1.data.PocketMonster;
import com.example.example1.data.PocketMonsterHelper;
import com.nolanlawson.couchdroid.CouchDroid;
import com.nolanlawson.couchdroid.CouchDroidProgressListener;

public class MainActivity extends Activity implements CouchDroidProgressListener {
    
    private static final String COUCHDB_URL = "http://admin:password@192.168.0.3:5984/pokemon";
    private static final int EXPECTED_COUNT = 743;
    private static final boolean RANDOMIZE_DB = true;
    private static final boolean LOAD_ONLY_ONE_MONSTER = true;
    
    private CouchDroid couchdbSync;
    private SQLiteDatabase sqliteDatabase;
    private long startTime;
    
    private TextView text;
    private ProgressBar progress, progressIndeterminate;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(android.R.id.text1);
        progress = (ProgressBar) findViewById(android.R.id.progress);
        progressIndeterminate = (ProgressBar) findViewById(R.id.progress_indeterminate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        String dbName = "pokemon.db";
        if (RANDOMIZE_DB) {
            dbName = "pokemon_" + Long.toString(Math.abs(new Random().nextLong())) + ".db";
        }
        sqliteDatabase = openOrCreateDatabase(dbName, 0, null);
        
        loadPokemonData(sqliteDatabase);
        couchdbSync = CouchDroid.Builder.create(this, sqliteDatabase)
                .setUserId("fooUser")
                .setCouchdbUrl(COUCHDB_URL)
                .addSqliteTable("Monsters", "uniqueId")
                .setProgressListener(this)
                .build();
        
        couchdbSync.start();
        startTime = System.currentTimeMillis();
        progress.setProgress(0);
        progressIndeterminate.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (couchdbSync != null) {
            couchdbSync.close();
        }
        if (sqliteDatabase != null) {
            sqliteDatabase.close();
        }
    }

    private void loadPokemonData(SQLiteDatabase sqliteDatabase) {
        
        List<PocketMonster> monsters = PocketMonsterHelper.readInPocketMonsters(this);
        
        if (LOAD_ONLY_ONE_MONSTER) {
            monsters = monsters.subList(0, 1);
        }
        
        SQLiteStatement statement = sqliteDatabase.compileStatement(
                "select count(*) from sqlite_master where type='table' and name='Monsters';");
        long tableExists = statement.simpleQueryForLong();
        
        if (tableExists > 0) {
            return; // nothing to do
        }
        
        sqliteDatabase.execSQL("create table Monsters (" +
        		"_id integer primary key autoincrement, " +
        		"uniqueId text not null, " +
        		"nationalDexNumber integer not null, " +
        		"type1 text not null, " +
        		"type2 text, " +
        		"name text not null)");
        
        sqliteDatabase.beginTransaction();
        try {
            for (PocketMonster monster : monsters) {
                ContentValues values = new ContentValues();
                values.put("uniqueId", monster.getUniqueId());
                values.put("nationalDexNumber", monster.getNationalDexNumber());
                values.put("type1", monster.getType1());
                if (monster.getType2() == null) {
                    values.putNull("type2");
                } else {
                    values.put("type2", monster.getType2());
                }
                values.put("name", monster.getName());
                sqliteDatabase.insert("Monsters", null, values);
            }
            sqliteDatabase.setTransactionSuccessful();
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

    @Override
    public void onProgress(ProgressType type, String tableName, int numRowsTotal, int numRowsLoaded) {
        
        progress.setMax(numRowsTotal);
        progress.setProgress(numRowsLoaded);
        StringBuilder textContent = new StringBuilder();
        textContent.append(numRowsLoaded + "/" + numRowsTotal);
        if (numRowsTotal == numRowsLoaded) {
            long totalTimeMs = System.currentTimeMillis() - startTime;
            
            double totalTimeS = totalTimeMs / 1000.0;
            
            textContent.append("\nCompleted in " + totalTimeS + " seconds");
            
            int dbCount = verifyDbCount();
            
            textContent.append("\nFound " + dbCount + " rows, expected " + EXPECTED_COUNT);
            
            getWindow().getDecorView().getRootView().setBackgroundColor(getResources().getColor(
                    dbCount == EXPECTED_COUNT ? R.color.alert_blue : R.color.alert_red));
            progressIndeterminate.setVisibility(View.INVISIBLE);
        }
        text.setText(textContent);
    }

    private int verifyDbCount() {
        
        String dbName = couchdbSync.getPouchDatabaseNames().get(0);
        
        SQLiteDatabase sqliteDatabase = null; 
        try {
            sqliteDatabase = getDatabase(dbName);
            
            Cursor cursor = null;
            try {
                cursor = sqliteDatabase.rawQuery("select count(*) from 'document-store';", null);
                int numRows = cursor.moveToNext() ? cursor.getInt(0) : 0;
                
                return numRows;
                
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } finally {
            if (sqliteDatabase != null) {
                sqliteDatabase.close();
            }
        }
    }
    
    private SQLiteDatabase getDatabase(String dbName) {
        while (true) {
            try{
                return openOrCreateDatabase(dbName, 0, null);
            } catch (SQLiteDatabaseLockedException e) {
                Log.e("MainActivity", "database locked", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                // ignore
            }
        }
    }
}
