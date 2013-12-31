package com.pouchdb.pouchdroid.pouch;

public enum ReduceFunction {
    
    Sum("_sum"), Count("_count"), Stats("_stats");
    
    private String name;
    
    private ReduceFunction(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
