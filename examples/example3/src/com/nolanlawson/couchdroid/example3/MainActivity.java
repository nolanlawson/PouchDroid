package com.nolanlawson.couchdroid.example3;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

import com.nolanlawson.couchdroid.CouchDroidActivity;
import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.pouch.AllDocsInfo;
import com.nolanlawson.couchdroid.pouch.PouchDB;
import com.nolanlawson.couchdroid.pouch.PouchDB.AllDocsCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.BulkCallback;
import com.nolanlawson.couchdroid.pouch.PouchDB.ReplicateCallback;
import com.nolanlawson.couchdroid.pouch.PouchError;
import com.nolanlawson.couchdroid.pouch.PouchInfo;
import com.nolanlawson.couchdroid.pouch.ReplicateInfo;
import com.nolanlawson.couchdroid.util.Maps;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class MainActivity extends CouchDroidActivity {

    private static UtilLogger log = new UtilLogger(MainActivity.class);
    
    private static final String REMOTE_COUCHDB_URL = "http://admin:password@192.168.0.3:5984/robots";
    
    private PouchDB<Robot> pouch1, pouch2;
    
    private Handler handler = new Handler();
    
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
        
        String random = Integer.toHexString(new Random().nextInt());
        
        pouch1 = PouchDB.newPouchDB(Robot.class, runtime, "robots-" + random + "-1.db");
        pouch2 = PouchDB.newPouchDB(Robot.class, runtime, "robots-" + random + "-2.db");
        
        loadIntoPouch1();
    }

    private void loadIntoPouch1() {
        List<Robot> robots = Arrays.asList(
                new Robot("C3P0", "Protocol droid", "George Lucas", 0.4, 200, 
                        Arrays.asList(
                                new RobotFunction("Human-cyborg relations"),
                                new RobotFunction("Losing his limbs")
                                )),
                new Robot("R2-D2", "Astromech droid", "George Lucas", 0.8, 135,
                        Arrays.asList(
                                new RobotFunction("Getting lost"),
                                new RobotFunction("Having a secret jetpack"),
                                new RobotFunction("Showing holographic messages 'n' shit")))        
                );
        
        pouch1.bulkDocs(robots, new BulkCallback() {
            
            @Override
            public void onCallback(PouchError err, List<PouchInfo> info) {
                loadIntoPouch2();
                
            }
        });        
    }

    private void loadIntoPouch2() {
        List<Robot> robots = Arrays.asList(
                new Robot("Mecha Godzilla", "Giant monster", "Toho Co., Ltd.", 0.4, 82, 
                        Arrays.asList(
                                new RobotFunction("Flying through space"),
                                new RobotFunction("Kicking Godzilla's ass"))),
                new Robot("Andy", "Messenger robot", "Stephen King", 0.8, 135,
                        Arrays.asList(
                                new RobotFunction("Relaying messages"),
                                new RobotFunction("Betraying the ka-tet"),
                                new RobotFunction("Many other functions"))),
                new Robot("Bender Bending Rodriguez", "Bending Unit", "Matt Groening", 0.999, 120,
                        Arrays.asList(
                                new RobotFunction("Gettin' drunk"),
                                new RobotFunction("Burping fire"),
                                new RobotFunction("Bending things"),
                                new RobotFunction("Talking about the lustre of his ass")))                                   
                );
        
        pouch2.bulkDocs(robots, new BulkCallback() {
            
            @Override
            public void onCallback(PouchError err, List<PouchInfo> info) {
                replicate();
            }
        }); 
    }

    private void replicate() {
        
        // bi-directional replication on both pouches
        ReplicateCallback onReplicateFrom = new ReplicateCallback() {
            
            @Override
            public void onCallback(PouchError err, ReplicateInfo info) {
                log.i("replicate.from: %s, %s", err, info);
            }
        };
        
        ReplicateCallback onReplicateTo = new ReplicateCallback() {
            
            @Override
            public void onCallback(PouchError err, ReplicateInfo info) {
                log.i("replicate.to  : %s, %s", err, info);
            }
        };
        
        Map<String, Object> options = Maps.quickMap("continuous", true);
        
        pouch1.replicateFrom(REMOTE_COUCHDB_URL, options, onReplicateFrom);
        pouch1.replicateTo(REMOTE_COUCHDB_URL, options, onReplicateTo);
        pouch2.replicateFrom(REMOTE_COUCHDB_URL, options, onReplicateFrom);
        pouch2.replicateTo(REMOTE_COUCHDB_URL, options, onReplicateTo);
        
        final int DELAY = 10000;
        
        handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                checkPouchContents();
                
                handler.postDelayed(this, DELAY); // continuous
            }
        }, DELAY);
        
    }

    private void checkPouchContents() {
        
        pouch1.allDocs(true, new AllDocsCallback<Robot>() {
            
            @Override
            public void onCallback(PouchError err, AllDocsInfo<Robot> info) {
                log.i("pouch1 contains %s", info.getDocuments());
                
            }
        });
        
        
        pouch2.allDocs(true, new AllDocsCallback<Robot>() {
            
            @Override
            public void onCallback(PouchError err, AllDocsInfo<Robot> info) {
                log.i("pouch2 contains %s", info.getDocuments());
                
            }
        });
    }
}