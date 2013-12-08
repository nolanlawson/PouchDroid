package com.nolanlawson.couchdroid;

import android.app.Activity;
import android.webkit.JavascriptInterface;

import com.nolanlawson.couchdroid.migration.CouchDroidProgressListener;
import com.nolanlawson.couchdroid.migration.CouchDroidProgressListener.ProgressType;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class ProgressReporter {
    
    private static UtilLogger log = new UtilLogger(ProgressReporter.class);
    
    private Activity activity;
    private CouchDroidProgressListener listener;
    
    public ProgressReporter(Activity activity, CouchDroidProgressListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    @JavascriptInterface
    public void reportProgress(final String type, final String tableName, final int numRowsTotal, 
            final int numRowsLoaded) {
        
        log.d("reportProgress(%s, %s, %s, %s)", type, tableName, numRowsTotal, numRowsLoaded);
        
        if (listener == null) {
            return;
        }
        try {
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            
                            listener.onProgress(ProgressType.valueOf(type), tableName, numRowsTotal, numRowsLoaded);
                        } catch (Exception e) {
                            log.e(e, "progress listener threw an exception!");
                        }                        
                    }
                });
            }
        } catch (Exception e) {
            log.e(e, "unexpected exception in reportProgress()");
        }
    }
}
