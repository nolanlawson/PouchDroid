package com.pouchdb.pouchdroid.example1;

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
import android.widget.ScrollView;
import android.widget.TextView;

import com.pouchdb.pouchdroid.PouchDroidActivity;
import com.pouchdb.pouchdroid.PouchDroid;
import com.pouchdb.pouchdroid.example1.pojo.PocketMonster;
import com.pouchdb.pouchdroid.migration.PouchDroidMigrationTask;
import com.pouchdb.pouchdroid.migration.GenericSqliteDocument;
import com.pouchdb.pouchdroid.migration.MigrationProgressListener;
import com.pouchdb.pouchdroid.pouch.PouchDB;

public class MainActivity extends PouchDroidActivity {
    
    private static final String COUCHDB_URL = "http://192.168.0.3:5984/pokemon";
    
    private String localPouchName;
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
    }
    
    @Override
    public void onPouchDroidReady(final PouchDroid pouchDroid) {
        progressIndeterminate.setVisibility(View.VISIBLE);
        
        localPouchName = "pokemon";
        
        if (RANDOMIZE_DB) {
            localPouchName += Integer.toHexString(Math.abs(new Random().nextInt()));
        } 
        doInitialMigration();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (sqliteDatabase != null) {
            sqliteDatabase.close();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    private void doInitialMigration() {
        progress.setProgress(0);
        appendText("" + Html.fromHtml("Loading pok&eacute;mon data into SQLite..."));
        
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
                
                runMigration();
            }
            
        }.execute((Void)null);
    }
    
    private void runMigration() {
        new PouchDroidMigrationTask.Builder(getPouchDroid(), sqliteDatabase)
            .setUserId("fooUser")
            .setPouchDBName(localPouchName)
            .addSqliteTable("Monsters", "uniqueId")
            .setProgressListener(new MyMigrationProgressListener())             
            .build()
            .start();
    }

    private void loadPokemonData() {
        
        String dbName = "pokemon.db";
        sqliteDatabase = openOrCreateDatabase(dbName, 0, null);
        
        List<PocketMonster> monsters = PocketMonsterHelper.readInPocketMonsters(this);
        
        if (LOAD_ONLY_ONE_MONSTER) {
            monsters = monsters.subList(0, 1);
        }
        
        SQLiteStatement statement = sqliteDatabase.compileStatement(
                "drop table if exists 'Monsters'");
        statement.execute();
        
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
    
    public void modifyOrVerify() {
        
        String sql = "select count(*) from Monsters where nationalDexNumber > 151";
        long count = sqliteDatabase.compileStatement(sql).simpleQueryForLong();
        
        if (count == 0) { // already modified pokemon db, no need to replicate
            verifyRemotePouchContents();
        } else {
            modifyDbAndContinue();
        }
    }    
    
    private void verifyRemotePouchContents() {
        
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                
                PouchDB<GenericSqliteDocument> pouch = PouchDB.newPouchDB(GenericSqliteDocument.class, getPouchDroid(), 
                        localPouchName);
                pouch.replicateTo(COUCHDB_URL, true);
                
                // verify the remote db contains only 151 pokemon, as God intended
                try {
                    Thread.sleep(90000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                PouchDB<GenericSqliteDocument> remotePouch = PouchDB.newPouchDB(GenericSqliteDocument.class, getPouchDroid(),
                        COUCHDB_URL);
                List<GenericSqliteDocument> localMonsters = pouch.allDocs(true).getDocuments();
                List<GenericSqliteDocument> monsters = remotePouch.allDocs(true).getDocuments();
                if (monsters.size() != 151) {
                    throw new RuntimeException("replication failed!  The remote DB contains " + monsters.size() 
                            + " pokemon instead of 151.");
                } else if (localMonsters.size() != 151) {
                    throw new RuntimeException("replication failed!  The local DB contains " + localMonsters.size() 
                            + " pokemon instead of 151.");
                }
                
                
                return null;
            }
        }.execute((Void)null);
        
    }

    private void modifyDbAndContinue() {
        
        // I hate the later Pokemon
        sqliteDatabase.execSQL("delete from Monsters where nationalDexNumber > 151");
        
        // use the Japanese name for Jigglypuff
        sqliteDatabase.execSQL("update Monsters set name = 'Purin' where nationalDexNumber = 39");
        
        runMigration();
        
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
                
                boolean isExpectedCount = (numRowsLoaded == 743 || numRowsLoaded == 151);
                
                getWindow().getDecorView().getRootView().setBackgroundColor(getResources().getColor(
                        isExpectedCount ? R.color.alert_blue : R.color.alert_red));
                progressIndeterminate.setVisibility(View.INVISIBLE);
            }
            appendText(textContent);
        }

        @Override
        public void onStart() {
            appendText("Migration started!");
        }
        
        @Override
        public void onDocsDeleted(int numDocumentsDeleted) {
            appendText("Deleted " + numDocumentsDeleted + " docs.");
        }

        @Override
        public void onEnd() {
            appendText("Migration done!");
            
            modifyOrVerify();
        }
    }
    
    private void appendText(final CharSequence format, final Object... args) {
        text.post(new Runnable() {
            
            @Override
            public void run() {
                text.setText(new StringBuilder(text.getText()).append("\n").append(String.format(format.toString(), args)));
                ((ScrollView)text.getParent()).fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
