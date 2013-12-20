package com.nolanlawson.couchdroid.example1;

import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nolanlawson.couchdroid.CouchDroidActivity;
import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.example1.pojo.PocketMonster;
import com.nolanlawson.couchdroid.migration.CouchDroidMigrationTask;
import com.nolanlawson.couchdroid.migration.MigrationProgressListener;
import com.nolanlawson.couchdroid.pouch.PouchDB;

public class MainActivity extends CouchDroidActivity {
    
    private static final String COUCHDB_URL = "http://192.168.0.3:5984/pokemon";
    
    private String localPouchName = "pokemon";
    private static final int EXPECTED_COUNT = 743;
    private static final boolean RANDOMIZE_DB = true;
    private static final boolean LOAD_ONLY_ONE_MONSTER = false;
    
    
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
        
        progressIndeterminate.setVisibility(View.VISIBLE);
    }

    
    @Override
    public void onCouchDroidReady(final CouchDroidRuntime runtime) {

        
        progress.setProgress(0);
        text.setText(Html.fromHtml("Loading pok&eacute;mon data into SQLite..."));
        
        // load pokemon data in the background,
        // then launch the migration task in the foreground
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    loadPokemonData();
                } catch (Exception e) {
                    Log.e("MainActivity", "loadPokemonData() threw error; app is probably closing...", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                
                text.setText(Html.fromHtml("Done loading pok&eacute;mon data, beginning Pouch transfer..."));
                startTime = System.currentTimeMillis();
                
                if (RANDOMIZE_DB) {
                    localPouchName = "pokemon_" + Integer.toHexString(Math.abs(new Random().nextInt()));
                }
                
                new CouchDroidMigrationTask.Builder(runtime, sqliteDatabase)
                    .setUserId("fooUser")
                    .setPouchDBName(localPouchName)
                    .addSqliteTable("Monsters", "uniqueId")
                    .setProgressListener(new MyMigrationProgressListener())             
                    .build()
                    .start();
            }
            
        }.execute((Void)null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sqliteDatabase != null) {
            sqliteDatabase.close();
        }
    }

    private void loadPokemonData() {
        
        String dbName = "pokemon.db";
        sqliteDatabase = openOrCreateDatabase(dbName, 0, null);
        
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
    
    public void replicateToRemote() {
        
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                
                PouchDB<PocketMonster> pouch = PouchDB.newPouchDB(PocketMonster.class, getCouchDroidRuntime(), 
                        localPouchName);
                
                pouch.replicateTo(COUCHDB_URL);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                text.setText(text.getText() + "\nDatabase replicated as well!");
            }
        }.execute((Void)null);
    }    

    private class MyMigrationProgressListener extends MigrationProgressListener {

        @Override
        public void onProgress(String tableName, int numRowsTotal, int numRowsLoaded) {
            
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
            text.setText(text.getText() + "\n" + textContent);
        }

        @Override
        public void onStart() {
            text.setText("Migration started!");
        }
        
        @Override
        public void onDocsDeleted(int numDocumentsDeleted) {
            text.setText(text.getText() + "\nDeleted " + numDocumentsDeleted + " docs.");
        }

        @Override
        public void onEnd() {
            text.setText(text.getText() + "\nMigration done!");
            
            replicateToRemote();
        }
    }
}
