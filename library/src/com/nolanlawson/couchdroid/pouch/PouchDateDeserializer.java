package com.nolanlawson.couchdroid.pouch;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * Simple deserializer for deserializing Pouch-supplied JSON dates.
 * @author nolan
 *
 */
public class PouchDateDeserializer extends JsonDeserializer<Date> {
    
    // date formats are not threasafe
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>(){

        @Override
        protected SimpleDateFormat initialValue() {
            // example: Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)
            return new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzzz", Locale.US);
        }
        
    };
    
    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) 
            throws IOException, JsonProcessingException {

        String date = parser.getText();
        try {
            return DATE_FORMAT.get().parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e); // shouldn't happen
        }
    }
}
