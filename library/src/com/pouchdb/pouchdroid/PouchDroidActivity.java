package com.pouchdb.pouchdroid;


import android.app.Activity;

/**
 * Convenience class for using PouchDroid.  Extend this if you're using a normal Activity,
 * then implement <code>onPouchDroidReady()</code>
 * @author nolan
 *
 */
public abstract class PouchDroidActivity extends Activity {
    
    private PouchDroid pouchDroid;
    private boolean couchDroidReady;
    
    /**
     * Called when the PouchDroid is loaded, after onResume() in the Activity lifecycle.  Runs on the UI thread.
     * 
     * @param pouchDroid
     */
    protected abstract void onPouchDroidReady(PouchDroid pouchDroid);
    
    @Override
    protected void onResume() {
        super.onResume();
        
        pouchDroid = new PouchDroid(this, new PouchDroid.OnReadyListener(){
            @Override
            public void onReady(PouchDroid pouchDroid) {
                couchDroidReady = true;
                onPouchDroidReady(pouchDroid);
            }
        });
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (pouchDroid != null) {
            pouchDroid.close();
        }
    }
    
    public PouchDroid getPouchDroid() {
        return pouchDroid;
    }
    
    public boolean isPouchDroidReady() {
        return couchDroidReady;
    }
}
