package com.pouchdb.pouchdroid;


import android.annotation.SuppressLint;
import android.app.Fragment;

/**
 * Convenience class for using PouchDroid.  Extend this if you're using a normal Fragment,
 * then implement <code>onPouchDroidReady()</code>
 * @author nolan
 *
 */
@SuppressLint("NewApi")
public abstract class PouchDroidFragment extends Fragment {
    
    private PouchDroid pouchDroid;
    
    /**
     * Called when the PouchDroid is loaded, after onResume() in the Activity lifecycle.  Runs on the UI thread.
     * 
     * @param pouchDroid
     */
    protected abstract void onPouchDroidReady(PouchDroid pouchDroid);
    
    @Override
    public void onResume() {
        super.onResume();
        
        pouchDroid = new PouchDroid(getActivity(), new PouchDroid.OnReadyListener(){
            @Override
            public void onReady(PouchDroid pouchDroid) {
                onPouchDroidReady(pouchDroid);
            }
        });
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (pouchDroid != null) {
            pouchDroid.close();
        }
    }
    
    
    public PouchDroid getPouchDroid() {
        return pouchDroid;
    }
}
