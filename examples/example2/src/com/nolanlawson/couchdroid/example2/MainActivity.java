package com.nolanlawson.couchdroid.example2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Bundle;
import android.view.Menu;

import com.nolanlawson.couchdroid.CouchDroidActivity;
import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.pouch.AsyncPouchDB;
import com.nolanlawson.couchdroid.pouch.PouchDB;
import com.nolanlawson.couchdroid.pouch.callback.AllDocsCallback;
import com.nolanlawson.couchdroid.pouch.callback.BulkCallback;
import com.nolanlawson.couchdroid.pouch.callback.GetCallback;
import com.nolanlawson.couchdroid.pouch.callback.ReplicateCallback;
import com.nolanlawson.couchdroid.pouch.callback.StandardCallback;
import com.nolanlawson.couchdroid.pouch.model.AllDocsInfo;
import com.nolanlawson.couchdroid.pouch.model.PouchError;
import com.nolanlawson.couchdroid.pouch.model.PouchInfo;
import com.nolanlawson.couchdroid.pouch.model.ReplicateInfo;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class MainActivity extends CouchDroidActivity {

    private static UtilLogger log = new UtilLogger(MainActivity.class);
    
    private static final String REMOTE_COUCHDB_URL = "http://admin:password@192.168.0.3:5984/dinosaurs";
    
    private AsyncPouchDB<Dinosaur> dinosaurPouch;
    
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
        
        log.i("onCouchDroidReady()");
        
        String dbName = "dinosaurs-" + (Integer.toHexString(new Random().nextInt())) + ".db";
        dinosaurPouch = PouchDB.newAsyncPouchDB(Dinosaur.class, runtime, dbName);
        
        runPuts();
    }
    
    private void runPuts() {
        Dinosaur dinosaur1 = new Dinosaur("T-Rex", "Tyrannosaurus Rex", "Lawyers", 34, 0.89);
        Dinosaur dinosaur2 = new Dinosaur("M'fin Brontosaurus", "Apatosaurus ajax", "Leaves 'n' shit", 12, 0.25);
        Dinosaur dinosaur3 = new Dinosaur("Mega-Raptor", "Deinonychus", "Faces", 81, 0.9999);
        
        dinosaur1.setPouchId("1");
        dinosaur2.setPouchId("2");
        dinosaur3.setPouchId("3");
        
        final AtomicInteger numRun = new AtomicInteger(0);
        
        StandardCallback onPut  = new StandardCallback() {

            @Override
            public void onCallback(PouchError err, PouchInfo info) {
                log.i("put: got response: err: %s, info: %s", err, info);
                if (numRun.incrementAndGet() == 3) {
                    runGets();
                }
            }
        };
        
        dinosaurPouch.put(dinosaur1, onPut);
        dinosaurPouch.put(dinosaur2, onPut);
        dinosaurPouch.put(dinosaur3, onPut);
    }
    
    private void runGets() {
        
        final List<Dinosaur> dinosaurs = new ArrayList<Dinosaur>();
        
        GetCallback<Dinosaur> onGet = new GetCallback<Dinosaur>() {
            
            @Override
            public void onCallback(PouchError err, Dinosaur dinosaur) {
                log.i("get: got response: err: %s, doc: %s", err, dinosaur);
                dinosaurs.add(dinosaur);
                if (dinosaurs.size() == 3) {
                    runBulkPuts();
                }
            }
        };
        
        dinosaurPouch.get("1", onGet);
        dinosaurPouch.get("2", onGet);
        dinosaurPouch.get("3", onGet);
    }
    
    private void runBulkPuts() {
        
        List<Dinosaur> newDinosaurs = Arrays.asList(
                new Dinosaur("Steggy", "Stegosaurus armatus", "Dirt", 2, 0.3),
                new Dinosaur("Birdo", "Archaeopteryx lithographica", "Dragonflies prolly", 65, 0.8),
                new Dinosaur("Ducky", "Hadrosaurus foulkii", "Seaweed", 52, 0.3)
                );
        
        dinosaurPouch.bulkDocs(newDinosaurs, new BulkCallback() {
            
            @Override
            public void onCallback(PouchError err, List<PouchInfo> info) {
                log.i("bulkDocs: got response: err: %s, info: %s", err, info);
                runBulkGetsWithoutIncludeDocs();
            }
        });
    }
    
    private void runBulkGetsWithoutIncludeDocs() {
        dinosaurPouch.allDocs(new AllDocsCallback<Dinosaur>() {
            
            @Override
            public void onCallback(PouchError err, AllDocsInfo<Dinosaur> info) {
                log.i("allDocs(without include_docs): got response: err: %s, info: %s", err, info);
                runBulkGets();
            }
        });
    }
    
    private void runBulkGets() {
        dinosaurPouch.allDocs(true, new AllDocsCallback<Dinosaur>() {
            
            @Override
            public void onCallback(PouchError err, AllDocsInfo<Dinosaur> info) {
                log.i("allDocs: got response: err: %s, info: %s", err, info);
                List<Dinosaur> dinosaurs = info.getDocuments();
                log.i(" -> dinosaurs are: %s", dinosaurs);
                runDeletes(dinosaurs);
            }
        });
    }

    private void runDeletes(List<Dinosaur> dinosaurs) {

        final AtomicInteger numRun = new AtomicInteger(0);
        
        StandardCallback onDelete  = new StandardCallback() {

            @Override
            public void onCallback(PouchError err, PouchInfo info) {
                log.i("delete: got response: err: %s, info: %s", err, info);
                if (numRun.incrementAndGet() == 4) {
                    runReplicate();
                }
            }
        };
        
        dinosaurPouch.remove(dinosaurs.get(0), onDelete); // delete T-Rex
        
        Dinosaur fakeDinosaur = new Dinosaur("Terry", "Pterodactylus antiquus", "bugs", 76, 0.5);
        dinosaurPouch.remove(fakeDinosaur, onDelete);//fake delete
        fakeDinosaur.setPouchId("fakeId");
        dinosaurPouch.remove(fakeDinosaur, onDelete);//fake delete
        fakeDinosaur.setPouchId("fakeRev");
        dinosaurPouch.remove(fakeDinosaur, onDelete);//fake delete
    }

    private void runReplicate() {
        
        dinosaurPouch.replicateTo(REMOTE_COUCHDB_URL, new ReplicateCallback() {
            
            @Override
            public void onCallback(PouchError err, ReplicateInfo info) {
                log.i("replicateTo: got response: err: %s, info: %s", err, info);
            }
        });
        
    }
}
