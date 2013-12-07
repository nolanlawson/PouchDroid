package com.nolanlawson.couchdroid;


import android.annotation.SuppressLint;
import android.app.Fragment;

/**
 * Convenience class for using CouchDroidRuntime.  Extend this if you're using a normal Fragment,
 * then implement <code>onCouchDroidReady()</code>
 * @author nolan
 *
 */
@SuppressLint("NewApi")
public abstract class CouchDroidFragment extends Fragment {
    
    private CouchDroidRuntime couchDroidRuntime;
    
    /**
     * Called when the CouchDroidRuntime is loaded, after onResume() in the Activity lifecycle.  Runs on the UI thread.
     * 
     * @param runtime
     */
    protected abstract void onCouchDroidReady(CouchDroidRuntime runtime);
    
    @Override
    public void onResume() {
        super.onResume();
        
        couchDroidRuntime = new CouchDroidRuntime(getActivity(), new CouchDroidRuntime.OnReadyListener(){
            @Override
            public void onReady(CouchDroidRuntime runtime) {
                onCouchDroidReady(runtime);
            }
        });
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (couchDroidRuntime != null) {
            couchDroidRuntime.close();
        }
    }
}
