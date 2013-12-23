package com.pouchdb.pouchdroid.test.data;

import com.pouchdb.pouchdroid.pouch.PouchDocument;


public class ClassWithBadConstructor extends PouchDocument {

    private String noGood;
    private String notGonnaWork;

    public ClassWithBadConstructor(String noGood, String notGonnaWork) {
        this.noGood = noGood;
        this.notGonnaWork = notGonnaWork;
    }
    
    public String getNoGood() {
        return noGood;
    }
    public void setNoGood(String noGood) {
        this.noGood = noGood;
    }
    public String getNotGonnaWork() {
        return notGonnaWork;
    }
    public void setNotGonnaWork(String notGonnaWork) {
        this.notGonnaWork = notGonnaWork;
    }
}
