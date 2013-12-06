package com.nolanlawson.couchdroid.migration;

public class SqliteColumn {
    
    private String name;
    private int type;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
}
