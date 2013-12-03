package com.example.example1;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.example.example1.data.PocketMonster;
import com.example.example1.data.PocketMonsterHelper;
import com.nolanlawson.couchdbsync.CouchdbSync;

public class MainActivity extends Activity {
    
    private CouchdbSync couchdbSync;
    SQLiteDatabase sqliteDatabase;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    

    @Override
    protected void onStart() {
        super.onStart();
        
        String dbName = "pokemon_" + Long.toString(Math.abs(new Random().nextLong())) + ".db";
        sqliteDatabase = openOrCreateDatabase(dbName, 0, null);
        
        loadPokemonData(sqliteDatabase);
        couchdbSync = CouchdbSync.Builder.create(this, sqliteDatabase)
                .setDatabaseId(dbName)
                .addSqliteTable("Monsters", "uniqueId")
                .build();
        
        couchdbSync.start();
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
    
}
