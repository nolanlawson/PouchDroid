package com.nolanlawson.couchdroid.test;

import java.util.Arrays;
import java.util.Random;

import junit.framework.Assert;

import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.nolanlawson.couchdroid.appforunittests.MainActivity;
import com.nolanlawson.couchdroid.pouch.PouchDB;
import com.nolanlawson.couchdroid.pouch.PouchException;
import com.nolanlawson.couchdroid.pouch.PouchInfo;
import com.nolanlawson.couchdroid.pouch.SynchronousPouchDB;
import com.nolanlawson.couchdroid.test.data.GameBoy;
import com.nolanlawson.couchdroid.test.data.Person;

@SuppressLint("NewApi")
public class CrudTest extends ActivityInstrumentationTestCase2<MainActivity>{
    
    private SynchronousPouchDB<Person> pouchDB;
    
    public CrudTest() {
        super(MainActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        while (getActivity() == null || !getActivity().isCouchDroidReady()) {
            Thread.sleep(100);
            Log.i("Tests", "Waiting for couch droid runtime to not be null");
        }
        String dbName = "unit-test-" + Integer.toHexString(new Random().nextInt());
        pouchDB = PouchDB.newSynchronousPouchDB(Person.class, 
                getActivity().getCouchDroidRuntime(), dbName);
    }



    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (pouchDB != null) {
            pouchDB.destroy();
        }
    }
    
    public void testIdempotentPut() throws PouchException {
        
        Person kenny = new Person("Kenny", 243987423, 4, null, false);
        
        try {
            pouchDB.put(kenny);
            Assert.fail();
        } catch (PouchException expected) {
            assertEquals(expected.getPouchError().getStatus(), 412); // missing_id, id required for puts
        }
        kenny.setPouchId("kenny");
        pouchDB.put(kenny);
        
        assertEquals(pouchDB.get("kenny"), kenny);
        
        try {
            pouchDB.put(kenny);
            Assert.fail();
        } catch (PouchException expected) {
            assertEquals(expected.getPouchError().getStatus(), 409); // conflict
        }
    }
    
    public void testPost() throws PouchException {
        
        Person kyle = new Person("Kyle", 24308, 2, null, true);
        
        PouchInfo pouchInfo = pouchDB.post(kyle);
        
        String kyleId = pouchInfo.getId();
        
        assertEquals(kyle, pouchDB.get(kyleId));

        kyle = pouchDB.get(kyleId);
        kyle.setGameBoys(Arrays.asList(new GameBoy("gba", "Game Boy Advance")));
        kyle.setNumberOfPetsOwned(3);
        
        pouchDB.post(kyle);
        
        assertEquals(kyle, pouchDB.get(kyleId));
    }
}
