package com.nolanlawson.couchdroid;


import android.app.ListActivity;

/**
 * Convenience class for using CouchDroidRuntime.  Extend this if you're using a normal ListActivity,
 * then implement <code>onCouchDroidReady()</code>
 * @author nolan
 *
 */
public abstract class CouchDroidListActivity extends ListActivity {
    
    private CouchDroidRuntime couchDroidRuntime;
    
    /**
     * Called when the CouchDroidRuntime is loaded, after onResume() in the Activity lifecycle.  Runs on the UI thread.
     * 
     * @param runtime
     */
    protected abstract void onCouchDroidReady(CouchDroidRuntime runtime);
    
    @Override
    protected void onResume() {
        super.onResume();
        
        couchDroidRuntime = new CouchDroidRuntime(this, new CouchDroidRuntime.OnReadyListener(){
            @Override
            public void onReady(CouchDroidRuntime runtime) {
                onCouchDroidReady(runtime);
            }
        });
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (couchDroidRuntime != null) {
            couchDroidRuntime.close();
        }
    }
}

