package com.pouchdb.pouchdroid.example2;

import com.pouchdb.pouchdroid.pouch.PouchDocument;

public class Dinosaur extends PouchDocument {

    private String name;
    private String latinName;
    private String favoriteFood;
    private int iq;
    private double awesomenessFactor;
    
    public Dinosaur() {
    }
    
    public Dinosaur(String name, String latinName, String favoriteFood, int iq, double awesomenessFactor) {
        this.name = name;
        this.latinName = latinName;
        this.favoriteFood = favoriteFood;
        this.iq = iq;
        this.awesomenessFactor = awesomenessFactor;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLatinName() {
        return latinName;
    }
    public void setLatinName(String latinName) {
        this.latinName = latinName;
    }
    public String getFavoriteFood() {
        return favoriteFood;
    }
    public void setFavoriteFood(String favoriteFood) {
        this.favoriteFood = favoriteFood;
    }
    public int getIq() {
        return iq;
    }
    public void setIq(int iq) {
        this.iq = iq;
    }
    public double getAwesomenessFactor() {
        return awesomenessFactor;
    }
    public void setAwesomenessFactor(double awesomenessFactor) {
        this.awesomenessFactor = awesomenessFactor;
    }
    @Override
    public String toString() {
        return "Dinosaur [name=" + name + ", latinName=" + latinName + ", favoriteFood=" + favoriteFood + ", iq=" + iq
                + ", awesomenessFactor=" + awesomenessFactor + "]";
    }
}
