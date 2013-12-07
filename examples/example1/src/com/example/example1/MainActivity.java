package com.example.example1;

import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.example1.data.PocketMonster;
import com.example.example1.data.PocketMonsterHelper;
import com.nolanlawson.couchdroid.CouchDroidActivity;
import com.nolanlawson.couchdroid.CouchDroidMigrationTask;
import com.nolanlawson.couchdroid.CouchDroidProgressListener;
import com.nolanlawson.couchdroid.CouchDroidRuntime;

public class MainActivity extends CouchDroidActivity implements CouchDroidProgressListener {
    
    private static final String COUCHDB_URL = "http://admin:password@192.168.10.103:5984/pokemon";
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
        
        progress.setProgress(0);
        progressIndeterminate.setVisibility(View.VISIBLE);
    }

    
    @Override
    public void onCouchDroidReady(final CouchDroidRuntime runtime) {

        
        text.setText(Html.fromHtml("Loading pok&eacute;mon data into SQLite..."));
        
        // load pokemon data in the background,
        // then launch the migration task in the foreground
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                loadPokemonData();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                
                text.setText(Html.fromHtml("Done loading pok&eacute;mon data, beginning Pouch transfer..."));
                startTime = System.currentTimeMillis();
                
                
                new CouchDroidMigrationTask.Builder(runtime, sqliteDatabase)
                    .setUserId("fooUser")
                    .setCouchdbUrl(COUCHDB_URL)
                    .addSqliteTable("Monsters", "uniqueId")
                    .setProgressListener(MainActivity.this)             
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
        if (RANDOMIZE_DB) {
            dbName = "pokemon_" + Long.toString(Math.abs(new Random().nextLong())) + ".db";
        }
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

    @Override
    public void onProgress(ProgressType type, String tableName, int numRowsTotal, int numRowsLoaded) {
        
        if (type == ProgressType.Sync) {
            text.setText(text.getText() + "\nDatabase synced as well!");
            return;
        }
        
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
