package com.nolanlawson.couchdroid.example2;

import java.util.Map;

import android.os.Bundle;
import android.view.Menu;

import com.nolanlawson.couchdroid.CouchDroidActivity;
import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.PouchDB;
import com.nolanlawson.couchdroid.PouchDB.PutPostCallback;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class MainActivity extends CouchDroidActivity {

    private static UtilLogger log = new UtilLogger(MainActivity.class);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onCouchDroidReady(CouchDroidRuntime runtime) {
        
        log.d("onCouchDroidReady()");
        
        PouchDB<Dinosaur> dinosaurPouch = new PouchDB<Dinosaur>(runtime, "dinosaurs.db");
        
        Dinosaur dinosaur1 = new Dinosaur("T-Rex", "Tyrannosaurus Rex", "Lawyers", 34, 0.89);
        Dinosaur dinosaur2 = new Dinosaur("M'fin Brontosaurus", "Apatosaurus ajax", "Leaves 'n' shit", 12, 0.25);
        Dinosaur dinosaur3 = new Dinosaur("Mega-Raptor", "Deinonychus", "Faces", 81, 0.9999);
        
        dinosaurPouch.put(dinosaur1, new OnPut());
        dinosaurPouch.put(dinosaur2, new OnPut());
        dinosaurPouch.put(dinosaur3, new OnPut());
        
    }
    
    private class OnPut extends PutPostCallback {

        @Override
        public void onCallback(Map<String, Object> err, Map<String, Object> info) {
            log.d("info: %s", info);
        }
    }
}
