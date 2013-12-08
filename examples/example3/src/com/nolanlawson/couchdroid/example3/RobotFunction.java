package com.nolanlawson.couchdroid.example3;

public class RobotFunction {

    private String name;
    
    public RobotFunction() {
    }
    
    public RobotFunction(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "RobotFunction [name=" + name + "]";
    }
}
