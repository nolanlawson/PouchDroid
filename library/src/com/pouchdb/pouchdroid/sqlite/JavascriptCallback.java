package com.pouchdb.pouchdroid.sqlite;

public class JavascriptCallback {
    private int callbackId;
    private Object arg1;
    private CharSequence extraJavascript;
    
    public JavascriptCallback(int callbackId, Object arg1, CharSequence extraJavascript) {
        this.callbackId = callbackId;
        this.arg1 = arg1;
        this.extraJavascript = extraJavascript;
    }
    
    public JavascriptCallback(int callbackId, Object arg1) {
        this(callbackId, arg1, null);
    }
    
    public CharSequence getExtraJavascript() {
        return extraJavascript;
    }

    public int getCallbackId() {
        return callbackId;
    }
    public Object getArg1() {
        return arg1;
    }
    public void setArg1(Object arg1) {
        this.arg1 = arg1;
    }

    @Override
    public String toString() {
        return "JavascriptCallback [callbackId=" + callbackId + ", arg1=" + arg1 + ", extraJavascript="
                + extraJavascript + "]";
    }
}
