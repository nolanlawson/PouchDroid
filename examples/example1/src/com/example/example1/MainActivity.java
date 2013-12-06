package com.example.example1;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.example1.data.PocketMonster;
import com.example.example1.data.PocketMonsterHelper;
import com.nolanlawson.couchdroid.CouchDroidMigrationTask;
import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.CouchDroidRuntime.OnReadyListener;
import com.nolanlawson.couchdroid.CouchDroidProgressListener;

public class MainActivity extends Activity implements CouchDroidProgressListener, OnReadyListener {
    
    private static final String COUCHDB_URL = "http://admin:password@192.168.10.110:5984/pokemon";
    private static final int EXPECTED_COUNT = 743;
    private static final boolean RANDOMIZE_DB = true;
    private static final boolean LOAD_ONLY_ONE_MONSTER = false;
    
    private CouchDroidRuntime couchDroidRuntime;
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
    protected void onResume() {
        super.onResume();
        
        String dbName = "pokemon.db";
        if (RANDOMIZE_DB) {
            dbName = "pokemon_" + Long.toString(Math.abs(new Random().nextLong())) + ".db";
        }
        sqliteDatabase = openOrCreateDatabase(dbName, 0, null);
        
        loadPokemonData(sqliteDatabase);
        
        
        couchDroidRuntime = new CouchDroidRuntime(this, this);
        
        startTime = System.currentTimeMillis();
        progress.setProgress(0);
        progressIndeterminate.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onReady(CouchDroidRuntime runtime) {
        
        new CouchDroidMigrationTask.Builder(runtime, sqliteDatabase)
            .setUserId("fooUser")
            .setCouchdbUrl(COUCHDB_URL)
            .addSqliteTable("Monsters", "uniqueId")
            .setProgressListener(MainActivity.this)             
            .build()
            .start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (couchDroidRuntime != null) {
            couchDroidRuntime.close();
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
            
            getWindow().getDecorView().getRootView().setBackgroundColor(getResources().getColor(
                    numRowsLoaded == EXPECTED_COUNT ? R.color.alert_blue : R.color.alert_red));
            progressIndeterminate.setVisibility(View.INVISIBLE);
        }
        text.setText(textContent);
    }
}
