package com.pouchdb.pouchdroid;


import android.app.ListActivity;

/**
 * Convenience class for using PouchDroid.  Extend this if you're using a normal ListActivity,
 * then implement <code>onPouchDroidReady()</code>
 * @author nolan
 *
 */
public abstract class PouchDroidListActivity extends ListActivity {
    
    private PouchDroid pouchDroid;
    
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
}

