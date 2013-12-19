package com.nolanlawson.couchdroid.test;

import java.util.Random;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.nolanlawson.couchdroid.appforunittests.MainActivity;
import com.nolanlawson.couchdroid.pouch.PouchDB;
import com.nolanlawson.couchdroid.pouch.PouchException;
import com.nolanlawson.couchdroid.test.data.Person;

@SuppressLint("NewApi")
public class CreateDestroyTest extends ActivityInstrumentationTestCase2<MainActivity>{
    
    public CreateDestroyTest() {
        super(MainActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        while (getActivity() == null || !getActivity().isCouchDroidReady()) {
            Thread.sleep(100);
            Log.i("Tests", "Waiting for couch droid runtime to not be null");
        }
    }



    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCreateDestroy() throws PouchException {
        String dbName = "unit-test-" + Integer.toHexString(new Random().nextInt());
        
        PouchDB<Person> pouchDB = PouchDB.newPouchDB(Person.class, 
                getActivity().getCouchDroidRuntime(), dbName);
        
        Person person = new Person("Mr. Mackey", 0, 0, null, false);
        person.setPouchId("fooId");
        pouchDB.put(person);
        Person gotPerson = pouchDB.get("fooId");
        assertEquals(person, gotPerson);
        pouchDB.destroy();
        try {
            pouchDB.get("fooId");
            Assert.fail();
        } catch (Exception expected) {
            
        }
        
        // ok, re-create it and try to get the person
        pouchDB = PouchDB.newPouchDB(Person.class, 
                getActivity().getCouchDroidRuntime(), dbName);
        
        try {
            pouchDB.get("fooId");
            Assert.fail();
        } catch (PouchException expected) {
            
        }
        
        pouchDB.destroy();
    }
}
