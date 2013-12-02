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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        String dbName = "pokemon-" + Long.toString(new Random().nextLong());
        SQLiteDatabase sqliteDatabase = openOrCreateDatabase(dbName, 0, null);
        
        loadPokemonData(sqliteDatabase);
        
        CouchdbSync couchdbSync = CouchdbSync.Builder.create(this, sqliteDatabase)
                .addSqliteTable("Monsters", "_id")
                .build();
        
        couchdbSync.start();
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
