package com.pouchdb.pouchdroid.pouch;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class PouchDocumentMapper {
    
    public static <T extends PouchDocumentInterface> T fromJson(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("unexpected json parsing error.  file a bug!", e);
        }
    }
    
    public static String toJson(PouchDocumentInterface pouchDocument) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(pouchDocument);
        } catch (JsonGenerationException e) {
            throw new RuntimeException("Unexpected json generation error.  file a bug!", e);
        } catch (JsonMappingException e) {
            throw new RuntimeException("Your Java classes must be simple POJOs with a no-argument constructor. " +
                    "Please add a public no-arg constructor.", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected json generation error.  file a bug!", e);
        }
    }
    
    public static String toJson(List<? extends PouchDocumentInterface> pouchDocuments) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(pouchDocuments);
        } catch (JsonGenerationException e) {
            throw new RuntimeException("Unexpected json generation error.  file a bug!", e);
        } catch (JsonMappingException e) {
            throw new RuntimeException("Your Java classes must be simple POJOs with a no-argument constructor. " +
                    "Please add a public no-arg constructor.", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected json generation error.  file a bug!", e);
        }
    }
}
