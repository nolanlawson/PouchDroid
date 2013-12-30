package com.pouchdb.pouchdroid.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.pouchdb.pouchdroid.appforunittests.MainActivity;
import com.pouchdb.pouchdroid.pouch.AsyncPouchDB;
import com.pouchdb.pouchdroid.pouch.PouchDB;
import com.pouchdb.pouchdroid.pouch.PouchException;
import com.pouchdb.pouchdroid.pouch.callback.ReplicateCallback;
import com.pouchdb.pouchdroid.pouch.model.PouchError;
import com.pouchdb.pouchdroid.pouch.model.PouchInfo;
import com.pouchdb.pouchdroid.pouch.model.ReplicateInfo;
import com.pouchdb.pouchdroid.test.data.GameBoy;
import com.pouchdb.pouchdroid.test.data.Person;

@SuppressLint("NewApi")
public class BasicTest extends ActivityInstrumentationTestCase2<MainActivity>{
    
    private String dbName;
    private PouchDB<Person> pouchDB;
    
    public BasicTest() {
        super(MainActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        while (getActivity() == null || !getActivity().isPouchDroidReady()) {
            Thread.sleep(100);
            Log.i("Tests", "Waiting for couch droid pouchDroid to not be null");
        }
        dbName = "unit-test-" + Integer.toHexString(new Random().nextInt());
        pouchDB = PouchDB.newPouchDB(Person.class, 
                getActivity().getPouchDroid(), dbName);
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
        
        Person unsavedPerson = new Person("Tweak", 24423890, 0, null, false);
        unsavedPerson.setPouchId("bogus");
        unsavedPerson.setPouchRev("123-bogus");
        try {
            pouchDB.remove(unsavedPerson);
            Assert.fail();
        } catch (PouchException expected) {
            
        }
    }
    
    public void testReplicate() throws Exception {
        PouchDB<Person> pouch1 = null;
        PouchDB<Person> pouch2 = null;
        
        try {
            String pouch1Name = "pouch1_" + new Random().nextInt();
            String pouch2Name = "pouch2_" + new Random().nextInt();
            
            pouch1 = PouchDB.newPouchDB(Person.class, 
                    getActivity().getPouchDroid(), pouch1Name);
            pouch2 = PouchDB.newPouchDB(Person.class, 
                    getActivity().getPouchDroid(), pouch2Name);
            
            pouch1.post(new Person("Stan", 18324, 1, Arrays.asList(new GameBoy("foo", "Nintendo DS")), false));
            pouch1.post(new Person("Wendy", 2318324, 0, Collections.<GameBoy>emptyList(), true));
            
            pouch2.post(new Person("Butters", 867354, 3, Arrays.asList(new GameBoy("Chaos", "GBA")), true));
            
            // use async so we can tell when it's done
            AsyncPouchDB<Person> asyncPouch1 = PouchDB.newAsyncPouchDB(Person.class, getActivity().getPouchDroid(), 
                    pouch1Name);
            
            final ArrayBlockingQueue<Boolean> lock = new ArrayBlockingQueue<Boolean>(1);
            
            asyncPouch1.replicateTo(pouch2.getName(), false, new ReplicateCallback() {
                
                @Override
                public void onCallback(PouchError err, ReplicateInfo info) {
                    lock.offer(Boolean.TRUE);
                }
            });
            lock.take();
            
            asyncPouch1.replicateFrom(pouch2.getName(), false, new ReplicateCallback() {
                
                @Override
                public void onCallback(PouchError err, ReplicateInfo info) {
                    lock.offer(Boolean.TRUE);
                }
            });
            lock.take();
            
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
