package com.pouchdb.pouchdroid.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqliteUtil {

    // copied from Cursor, api level >=11
    public static final int FIELD_TYPE_NULL = 0;
    public static final int FIELD_TYPE_INTEGER = 1;
    public static final int FIELD_TYPE_FLOAT = 2;
    public static final int FIELD_TYPE_STRING = 3;
    public static final int FIELD_TYPE_BLOB = 4;
    
    private static final Pattern SHORT_NAME_PATTERN = Pattern.compile("^[^\\s\\d(]+", Pattern.DOTALL);
    private static final Map<String, Integer> LOOKUP = new HashMap<String, Integer>();
    
    // see https://www.sqlite.org/datatype3.html
    static {
        for (String name : new String[]{
                "INT", "INTEGER", "TINYINT", "SMALLINT", "MEDIUMINT", 
                "BIGINT", "UNSIGNED BIG INT"}) {
            LOOKUP.put(name, FIELD_TYPE_INTEGER);
        }
        for (String name : new String[]{
                "CHARACTER", "VARCHAR", "VARYING CHARACTER", "NCHAR", 
                "NATIVE CHARACTER", "NVARCHAR", "TEXT", "CLOB"}) {
            LOOKUP.put(name, FIELD_TYPE_STRING);
        }
        LOOKUP.put("BLOB", FIELD_TYPE_BLOB);
        for (String name : new String[]{"REAL", "DOUBLE", "DOUBLE PRECISION", "FLOAT"}) {
            LOOKUP.put(name, FIELD_TYPE_FLOAT);
        }
        for (String name : new String[]{"NUMERIC", "DECIMAL", "BOOLEAN", "DATE", "DATETIME"}) {
            LOOKUP.put(name, FIELD_TYPE_INTEGER);
        }
    }
    
    public static int getTypeForTypeName(String typeName) {
        
        // get the column name without any parentheses, numbers, etc.
        Matcher matcher = SHORT_NAME_PATTERN.matcher(typeName);
        matcher.find();
        String shortName = matcher.group(0).toUpperCase(Locale.US);
        
        Integer result = LOOKUP.get(shortName);
        return result == null ? FIELD_TYPE_BLOB : result;
    }
    
}
