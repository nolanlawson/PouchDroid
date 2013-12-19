package com.nolanlawson.couchdroid.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.nolanlawson.couchdroid.appforunittests.MainActivity;
import com.nolanlawson.couchdroid.pouch.PouchDB;
import com.nolanlawson.couchdroid.pouch.PouchException;
import com.nolanlawson.couchdroid.pouch.model.PouchInfo;
import com.nolanlawson.couchdroid.test.data.GameBoy;
import com.nolanlawson.couchdroid.test.data.Person;

@SuppressLint("NewApi")
public class CrudTest extends ActivityInstrumentationTestCase2<MainActivity>{
    
    private String dbName;
    private PouchDB<Person> pouchDB;
    
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
        dbName = "unit-test-" + Integer.toHexString(new Random().nextInt());
        pouchDB = PouchDB.newPouchDB(Person.class, 
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
    
    public void testRemove() throws PouchException {
        Person cartman = new Person("Cartman", 12324324, 1, null, true);
        cartman.setPouchId("cartman");
        pouchDB.put(cartman);
        try {
            pouchDB.remove(cartman);
            Assert.fail();
        } catch (PouchException expected) {
        }
        
        cartman.setPouchRev(pouchDB.get("cartman").getPouchRev());
        pouchDB.remove(cartman);
        try {
            pouchDB.get("cartman");
            Assert.fail();
        } catch (PouchException expected) {
        }
    }
    
    public void testReplicate() throws Exception {
        PouchDB<Person> pouch1 = null;
        PouchDB<Person> pouch2 = null;
        
        try {
            pouch1 = PouchDB.newPouchDB(Person.class, 
                    getActivity().getCouchDroidRuntime(), "pouch1_" + new Random().nextInt());
            pouch2 = PouchDB.newPouchDB(Person.class, 
                    getActivity().getCouchDroidRuntime(), "pouch2_" + new Random().nextInt());
            
            pouch1.post(new Person("Stan", 18324, 1, Arrays.asList(new GameBoy("foo", "Nintendo DS")), false));
            pouch1.post(new Person("Wendy", 2318324, 0, Collections.<GameBoy>emptyList(), true));
            
            pouch2.post(new Person("Butters", 867354, 3, Arrays.asList(new GameBoy("Chaos", "GBA")), true));
            
            
            pouch1.replicateTo(pouch2.getName(), false);
            pouch1.replicateFrom(pouch2.getName(), false);
            
            assertEquals(3, pouch1.allDocs().getDocuments().size());
            assertEquals(pouch1.allDocs().getDocuments(), pouch2.allDocs().getDocuments());
            
        } finally {
            // cleanup
            if (pouch1 != null) {
                pouch1.destroy();
            }
            if (pouch2 != null) {
                pouch2.destroy();
            }
        }
    }
    
}
