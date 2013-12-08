package com.nolanlawson.couchdroid.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class Maps {

    public static Map<String, Object> quickMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(key, value);
        return map;
    }
    
    public static Map<String, Object> quickMap(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
    
    public static Map<String, Object> quickMap(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }
}
