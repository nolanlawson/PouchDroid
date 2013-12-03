package com.nolanlawson.couchdbsync;

import java.util.List;

public class SqliteTable {

    private String name;
    private List<String> idColumns;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<String> getIdColumns() {
        return idColumns;
    }
    public void setIdColumns(List<String> idColumns) {
        this.idColumns = idColumns;
    }

    @Override
    public String toString() {
        return "SqliteTable [name=" + name + ", idColumns=" + idColumns + "]";
    }
}
