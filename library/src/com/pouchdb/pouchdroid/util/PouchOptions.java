package com.pouchdb.pouchdroid.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Some convenience utilities for creating PouchDB-style options, which are really just maps of strings to objects.
 * @author nolan
 *
 */
public class PouchOptions {

    public static final String REDUCE = "reduce";
    public static final String INCLUDE_DOCS = "include_docs";
    public static final String CONTINUOUS = "continuous";
    public static final String INTERVAL = "interval";
    public static final String START_KEY = "startkey";
    public static final String END_KEY = "endkey";
    public static final String DESCENDING = "descending";
    public static final String KEY = "key";
    public static final String KEYS = "keys";
    public static final String ATTACHMENTS = "attachments";
    public static final String CONFLICTS = "conflicts";
    
    public static Map<String, Object> from(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(key, value);
        return map;
    }
    
    public static Map<String, Object> from(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
    
    public static Map<String, Object> from(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }
    
    public static Map<String, Object> includeDocs() {
        return from(INCLUDE_DOCS, true);
    }
    
    public static Map<String, Object> continuous() {
        return from(CONTINUOUS, true);
    }
    
    public static Map<String, Object> keys(Object... keys) {
        return keys(Arrays.asList(keys));
    }
    
    public static Map<String, Object> keys(Collection<?> keys) {
        return from(KEYS, keys);
    }
    
    public static Map<String, Object> key(Object key) {
        return from(KEY, key);
    }
}
