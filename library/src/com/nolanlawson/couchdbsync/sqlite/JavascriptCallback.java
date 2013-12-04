package com.nolanlawson.couchdbsync.sqlite;

public class JavascriptCallback {
    private String callbackId;
    private Object arg1;
    private boolean error;
    
    public JavascriptCallback(String callbackId, Object arg1, boolean error) {
        this.callbackId = callbackId;
        this.arg1 = arg1;
        this.error = error;
    }
    
    public boolean isError() {
        return error;
    }
    public String getCallbackId() {
        return callbackId;
    }
    public Object getArg1() {
        return arg1;
    }
    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }
    public void setArg1(Object arg1) {
        this.arg1 = arg1;
    }
    
    
    @Override
    public String toString() {
        return "JavascriptCallback [callbackId=" + callbackId + ", arg1=" + arg1 + "]";
    }
}
