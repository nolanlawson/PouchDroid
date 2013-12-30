package com.pouchdb.pouchdroid.test;

import java.util.Random;

import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.pouchdb.pouchdroid.appforunittests.MainActivity;
import com.pouchdb.pouchdroid.pouch.PouchDB;
import com.pouchdb.pouchdroid.pouch.model.DatabaseInfo;
import com.pouchdb.pouchdroid.test.data.Person;

public class MetaOperationsTest extends ActivityInstrumentationTestCase2<MainActivity> {
    
    private String dbName;
    private PouchDB<Person> pouchDB;
    
    @SuppressLint("NewApi")
    public MetaOperationsTest() {
        super(MainActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        while (getActivity() == null || !getActivity().isPouchDroidReady()) {
            Thread.sleep(100);
            Log.i("Tests", "Waiting for pouchDroid to not be null");
        }
        Log.i("Tests", "pouchdroid is not null");
        dbName = "unit-test-" + Integer.toHexString(new Random().nextInt());
        pouchDB = PouchDB.newPouchDB(Person.class, 
                getActivity().getPouchDroid(), dbName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (pouchDB != null) {
            Log.i("Tests", "Destroying pouchDroid");
            pouchDB.destroy();
            Log.i("Tests", "Destroyed pouchDroid");
        }
    }
    public void testDatabaseInfo() {
        
        pouchDB.post(new Person("Mr. Hankey", 0, 0, null, false));
        pouchDB.post(new Person("Butters", 0, 0, null, false));
        
        DatabaseInfo info = pouchDB.info();
        assertEquals("_pouch_" + pouchDB.getName(), info.getDbName());
        assertEquals(2, info.getDocCount());
        assertEquals(2, info.getUpdateSeq());
    }
    
}
