package com.nolanlawson.couchdroid.migration;

import android.app.Activity;
import android.webkit.JavascriptInterface;

import com.nolanlawson.couchdroid.util.UtilLogger;

public class MigrationProgressReporter {
    
    private static UtilLogger log = new UtilLogger(MigrationProgressReporter.class);
    
    private Activity activity;
    private MigrationProgressListener listener;
    
    public MigrationProgressReporter(Activity activity, MigrationProgressListener listener) {
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
                            callListener(type, tableName, numRowsTotal, numRowsLoaded);
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
    
    private void callListener(String type, String tableName, int numRowsTotal, int numRowsLoaded) {
        switch (ProgressType.valueOf(type)) {
            case Init:
                listener.onMigrationStart();
                break;
            case Copy:
                listener.onMigrationProgress(tableName, numRowsTotal, numRowsLoaded);
                break;
            case End:
            default:
                listener.onMigrationEnd();
                break;
        }
    }

    public static enum ProgressType {
        Init, Copy, End;
    }
}
