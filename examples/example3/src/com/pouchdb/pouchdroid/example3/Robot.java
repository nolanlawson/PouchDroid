package com.pouchdb.pouchdroid.example3;

import java.util.List;

import com.pouchdb.pouchdroid.pouch.PouchDocument;

public class Robot extends PouchDocument {

    private String name;
    private String type;
    private String creator;
    private double awesomenessFactor;
    private int iq;
    private List<RobotFunction> functions;
    
    public Robot() {
    }
    
    public Robot(String name, String type, String creator, double awesomenessFactor, int iq,
            List<RobotFunction> functions) {
        this.name = name;
        this.type = type;
        this.creator = creator;
        this.awesomenessFactor = awesomenessFactor;
        this.iq = iq;
        this.functions = functions;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    public double getAwesomenessFactor() {
        return awesomenessFactor;
    }
    public void setAwesomenessFactor(double awesomenessFactor) {
        this.awesomenessFactor = awesomenessFactor;
    }
    public int getIq() {
        return iq;
    }
    public void setIq(int iq) {
        this.iq = iq;
    }
    public List<RobotFunction> getFunctions() {
        return functions;
    }
    public void setFunctions(List<RobotFunction> functions) {
        this.functions = functions;
    }

    @Override
    public String toString() {
        return "Robot [name=" + name + ", type=" + type + ", creator=" + creator + ", awesomenessFactor="
                + awesomenessFactor + ", iq=" + iq + ", functions=" + functions + "]";
    }
}
