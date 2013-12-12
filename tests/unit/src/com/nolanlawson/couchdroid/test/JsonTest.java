package com.nolanlawson.couchdroid.test;

import java.util.Arrays;
import java.util.TimeZone;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;
import android.view.ViewDebug.ExportedProperty;

import com.nolanlawson.couchdroid.appforunittests.MainActivity;
import com.nolanlawson.couchdroid.pouch.PouchDocumentMapper;
import com.nolanlawson.couchdroid.test.data.ClassWithBadConstructor;
import com.nolanlawson.couchdroid.test.data.GameBoy;
import com.nolanlawson.couchdroid.test.data.Person;

/**
 * Test to make sure that the Date Functions work as expected and return the correct times.
 * @author nolan
 *
 */
public class JsonTest extends ActivityInstrumentationTestCase2<MainActivity> {

	@SuppressLint("NewApi")
    public JsonTest() {
		super(MainActivity.class);
	}
	
	@Override
    public void setUp() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }
	
	public void testBasic() {
		
	    Person person = new Person();
	    person.setName("Nolan Lawson");
	    person.setDystopianBarcodeId(4430823408L);
	    person.setNumberOfPetsOwned(3);
	    person.setBelieber(false);
	    
	    // I give names to my GameBoys.  Haters gonna hate.
	    person.setGameBoys(Arrays.asList(new GameBoy("Rudy", "GameBoy Pocket"), new GameBoy("Roger", "GameBoy Color")));
	    
	    String asJson = PouchDocumentMapper.toJson(person);
	    
	    Person fromJson = PouchDocumentMapper.fromJson(asJson, Person.class);
	    
	    System.out.println("asJson: " + asJson);
	    
	    String expectedJson = "{\"name\":\"Nolan Lawson\",\"gameBoys\":[{\"name\":\"Rudy\",\"type\":\"GameBoy Pocket\"}," +
                "{\"name\":\"Roger\",\"type\":\"GameBoy Color\"}],\"dystopianBarcodeId\":4430823408,\"belieber\":false," +
                "\"numberOfPetsOwned\":3,\"_id\":null,\"_rev\":null}";
	    assertEquals(asJson, expectedJson);
	    assertEquals(person, fromJson);
	    
	    assertNull(person.getPouchId());
	    assertNull(person.getPouchRev());
	    assertNull(fromJson.getPouchId());
        assertNull(fromJson.getPouchRev());
        
        // now set the pouch id/rev and make sure everything jives
        
        person.setPouchId("myId");
        person.setPouchRev("myRev");
        expectedJson = "{\"name\":\"Nolan Lawson\",\"gameBoys\":[{\"name\":\"Rudy\",\"type\":\"GameBoy Pocket\"}," +
                "{\"name\":\"Roger\",\"type\":\"GameBoy Color\"}],\"dystopianBarcodeId\":4430823408,\"belieber\":false," +
                "\"numberOfPetsOwned\":3,\"_id\":\"myId\",\"_rev\":\"myRev\"}";
        asJson = PouchDocumentMapper.toJson(person);
        fromJson = PouchDocumentMapper.fromJson(asJson, Person.class);
        
        assertEquals(asJson, expectedJson);
        assertEquals(person, fromJson);
        assertEquals(person.getPouchId(), "myId");
        assertEquals(person.getPouchRev(), "myRev");
        assertEquals(fromJson.getPouchId(), "myId");
        assertEquals(fromJson.getPouchRev(), "myRev");
        
        
	}
	
	@ExportedProperty
	public void testClassWithBadConstructor() {
	    try {
	        ClassWithBadConstructor clazz = new ClassWithBadConstructor("foo", "bar");
	        
	        PouchDocumentMapper.fromJson(PouchDocumentMapper.toJson(clazz), ClassWithBadConstructor.class);
	        Assert.fail("expected an exception");
	    } catch (RuntimeException expected) {
	        // expected this
	    }
	}
}
