package com.nolanlawson.couchdroid.util;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();
    
    public static String simpleMap(String key1, Object value1) {
        return simpleMap(key1, value1, null, null, null, null);
    }
    
    public static String simpleMap(String key1, Object value1, String key2, Object value2) {
        return simpleMap(key1, value1, key2, value2, null, null);
    }
    
    public static String simpleMap(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (key1 != null) {
            map.put(key1, value1);
        }
        if (key2 != null) {
            map.put(key2, value2);
        }
        if (key3 != null) {
            map.put(key3, value3);
        }
        
        return simpleMap(map);
    }
    
    public static String simpleMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (IOException e) {
            // shouldn't happen
            e.printStackTrace();
            throw new RuntimeException("unexpected", e);
        }
    }
    
    public static String simpleString(String input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (IOException e) {
            // shouldn't happen
            e.printStackTrace();
            throw new RuntimeException("unexpected", e);
        }
    }
    
    public static String simplePojo(Object pojo) {
        if (pojo == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(pojo);
        } catch (IOException e) {
            // shouldn't happen
            e.printStackTrace();
            throw new RuntimeException("unexpected", e);
        }        
    }
}
