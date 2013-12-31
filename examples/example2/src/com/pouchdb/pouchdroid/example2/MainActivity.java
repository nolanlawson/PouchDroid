package com.pouchdb.pouchdroid.example2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.pouchdb.pouchdroid.PouchDroid;
import com.pouchdb.pouchdroid.PouchDroidActivity;
import com.pouchdb.pouchdroid.pouch.PouchDB;
import com.pouchdb.pouchdroid.pouch.PouchException;
import com.pouchdb.pouchdroid.pouch.model.AllDocsInfo;
import com.pouchdb.pouchdroid.util.Maps;

public class MainActivity extends PouchDroidActivity {

    private static final String REMOTE_COUCHDB_URL = "http://192.168.0.3:5984/dinosaurs";

    private PouchDB<Dinosaur> dinosaurPouch;
    
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(android.R.id.text1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPouchDroidReady(PouchDroid pouchDroid) {

        appendText("onPouchDroidReady()");

        String dbName = "dinosaurs-" + (Integer.toHexString(new Random().nextInt())) + ".db";
        dinosaurPouch = PouchDB.newPouchDB(Dinosaur.class, pouchDroid, dbName);

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                loadData();
                return null;
            }
        }.execute((Void)null);
        
    }

    private void loadData() {
        
        // run puts
        
        Dinosaur dinosaur1 = new Dinosaur("T-Rex", "Tyrannosaurus Rex", "Lawyers", 34, 0.89);
        Dinosaur dinosaur2 = new Dinosaur("M'fin Brontosaurus", "Apatosaurus ajax", "Leaves 'n' shit", 12, 0.25);
        Dinosaur dinosaur3 = new Dinosaur("Mega-Raptor", "Deinonychus", "Faces", 81, 0.9999);

        dinosaur1.setPouchId("1");
        dinosaur2.setPouchId("2");
        dinosaur3.setPouchId("3");

        dinosaurPouch.put(dinosaur1);
        dinosaurPouch.put(dinosaur2);
        dinosaurPouch.put(dinosaur3);
        
        appendText("ran puts, dinosaurs are now %s", dinosaurPouch.allDocs(true).getDocuments());
     
        // run gets
        
        final List<Dinosaur> dinosaurs = new ArrayList<Dinosaur>();

        dinosaurs.add(dinosaurPouch.get("1"));
        dinosaurs.add(dinosaurPouch.get("2"));
        dinosaurs.add(dinosaurPouch.get("3"));
        
        appendText("ran gets, dinosaurs are now %s", dinosaurPouch.allDocs(true).getDocuments());
        
        // run bulk puts
        
        List<Dinosaur> newDinosaurs = Arrays.asList(new Dinosaur("Steggy", "Stegosaurus armatus", "Dirt", 2, 0.3),
                new Dinosaur("Birdo", "Archaeopteryx lithographica", "Dragonflies prolly", 65, 0.8), new Dinosaur(
                        "Ducky", "Hadrosaurus foulkii", "Seaweed", 52, 0.3));

        dinosaurPouch.bulkDocs(newDinosaurs);
        
        appendText("ran bulk puts, dinosaurs are now %s", dinosaurPouch.allDocs(true).getDocuments());
        
        // get all docs
        
        List<Dinosaur> savedDinosaurs = dinosaurPouch.allDocs(true).getDocuments();
        
        // delete T-Rex
        
        dinosaurPouch.remove(savedDinosaurs.get(0));
        
        appendText("ran delete, dinosaurs are now %s", dinosaurPouch.allDocs(true).getDocuments());

        // try deleting some fake dinosaurs
        Dinosaur fakeDinosaur = new Dinosaur("Terry", "Pterodactylus antiquus", "bugs", 76, 0.5);
        try {
            dinosaurPouch.remove(fakeDinosaur);// fake delete
            throw new RuntimeException("unexpected - no error");
        } catch (PouchException e) {
            appendText("got a pouch exception; expected that");
        }
        try {
            fakeDinosaur.setPouchId("fakeId");
            dinosaurPouch.remove(fakeDinosaur);// fake delete
            throw new RuntimeException("unexpected - no error");
        } catch (PouchException e) {
            appendText("got a pouch exception; expected that");
        }
        try {
            fakeDinosaur.setPouchId("fakeRev");
            dinosaurPouch.remove(fakeDinosaur);// fake delete
            throw new RuntimeException("unexpected - no error");
        } catch (PouchException e) {
            appendText("got a pouch exception; expected that");
        }
        
        appendText("ran fake deletes, dinosaurs are now %s", dinosaurPouch.allDocs(true).getDocuments());
        
        // replicate
        dinosaurPouch.replicateTo(REMOTE_COUCHDB_URL);
        
        appendText("ran replicate, dinosaurs are now %s", dinosaurPouch.allDocs(true).getDocuments());
        
        AllDocsInfo<Dinosaur> queryResponse = dinosaurPouch.query("function(doc){emit(doc.name, null);}", null, 
                Maps.quickMap("include_docs", true));
        appendText("query response is %s", queryResponse);
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // at this point, the dinosaurs should be in the remote CouchDB.  
        
        PouchDB<Dinosaur> remotePouch = PouchDB.newPouchDB(Dinosaur.class, getPouchDroid(), REMOTE_COUCHDB_URL);
        List<Dinosaur> remoteDocs = remotePouch.allDocs(true).getDocuments();
        appendText("remote pouch contents are %s", remoteDocs);
        if (remoteDocs.size() != 5) {
            throw new RuntimeException("expected the remote pouch to have 5 docs, but it had " + remoteDocs.size());
        }
    }
    
    private void appendText(final String format, final Object... args) {
        textView.post(new Runnable() {
            
            @Override
            public void run() {
                textView.setText(new StringBuilder(textView.getText()).append("\n\n").append(String.format(format, args)));
            }
        });
    }
}
