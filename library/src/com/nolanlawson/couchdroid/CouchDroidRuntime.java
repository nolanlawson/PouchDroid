package com.nolanlawson.couchdroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.RecoverySystem.ProgressListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.nolanlawson.couchdroid.sqlite.SQLiteJavascriptInterface;
import com.nolanlawson.couchdroid.util.ResourceUtil;
import com.nolanlawson.couchdroid.util.UtilLogger;
import com.nolanlawson.couchdroid.xhr.XhrJavascriptInterface;

@SuppressLint("SetJavaScriptEnabled")
public class CouchDroidRuntime {

    private static UtilLogger log = new UtilLogger(CouchDroidRuntime.class);

    private static final int JSINTERFACE_VERIFIER_CALLER_INTERVAL = 100; // ms
    
    private static final boolean USE_WEINRE = true;
    private static final boolean USE_MINIFIED_POUCH = true;
    private static final String WEINRE_URL = "http://192.168.10.110:8080";
    
    
    private Activity activity;
    private WebView webView;
    private JSInterfaceVerifierCaller jsInterfaceVerifierCaller;
    private CouchDroidProgressListener progressListener;
    private List<CouchDroidProgressListener> clientProgressListeners = new ArrayList<CouchDroidProgressListener>();
    private OnReadyListener onReadyListener;
    
    /**
     * Start a new CouchDroidRuntime in the given activity.  The standard idiom is:
     * 
     * <pre>
     * {@code
     * 
     * private CouchDroidRuntime couchDroidRuntime;
     * 
     * public void onResume() {
     *   super.onResume();
     *   couchDroidRuntime = new CouchDroidRuntime(this, new CouchDroidRuntime.OnReadyListener(){
     *     
     *     public void onReady(CouchDroidRuntime runtime) {
     *       // do stuff
     *     }
     *   });
     * }
     * 
     * public void onPause() {
     *   super.onPause();
     *   couchDroidRuntime.close();
     * }
     * </pre>
     * 
     * @param activity
     * @param onReadyListener
     */
    public CouchDroidRuntime(Activity activity, OnReadyListener onReadyListener) {
        this.activity = activity;
        this.onReadyListener = onReadyListener;
        this.progressListener = createProgressListener();
        
        initWebView();
        
        jsInterfaceVerifierCaller = new JSInterfaceVerifierCaller();
        jsInterfaceVerifierCaller.execute((Void)null);
        
        
    }
    
    /* package */ Activity getActivity() {
        return activity;
    }

    /* package */ WebView getWebView() {
        return webView;
    }

    private CouchDroidProgressListener createProgressListener() {
        // override the client listener to add our own
        
        return new CouchDroidProgressListener(){

            @Override
            public void onProgress(ProgressType type, String tableName, int numRowsTotal, int numRowsLoaded) {
                log.i("progress: (%s, %s, %s, %s)", type, tableName, numRowsTotal, numRowsLoaded);
                for (CouchDroidProgressListener listener : clientProgressListeners) {
                    if (listener != null) {
                        listener.onProgress(type, tableName, numRowsTotal, numRowsLoaded);
                    }
                }
            }
        };
    }
    
    private void loadInitialJavascript() {
        loadJavascript(TextUtils.join("\n", Arrays.asList(
                ResourceUtil.loadTextFile(activity, R.raw.ecmascript_shims),
                ResourceUtil.loadTextFile(activity, R.raw.couchdroid),
                ResourceUtil.loadTextFile(activity, R.raw.couchdroid_util),
                ResourceUtil.loadTextFile(activity, R.raw.couchdroid_xhr_native_interface),
                ResourceUtil.loadTextFile(activity, R.raw.couchdroid_sqlite_native_interface),
                "window.console.log('CouchDroid.NativeXMLHttpRequest is: ' + typeof CouchDroid.NativeXMLHttpRequest);",
                "window.console.log('CouchDroid.fakeLocalStorage is: ' + typeof CouchDroid.fakeLocalStorage);",
                "window.console.log('CouchDroid.SQLiteNativeDB.openNativeDatabase is: ' + typeof CouchDroid.SQLiteNativeDB.openNativeDatabase);",
                (ResourceUtil.loadTextFile(activity, USE_MINIFIED_POUCH ? R.raw.pouchdb_min : R.raw.pouchdb)
                        .replaceAll("\\bXMLHttpRequest\\b", "CouchDroid.NativeXMLHttpRequest")
                        .replaceAll("\\blocalStorage\\b", "CouchDroid.fakeLocalStorage")
                        .replaceAll("\\bopenDatabase\\b", "CouchDroid.SQLiteNativeDB.openNativeDatabase")),
                ResourceUtil.loadTextFile(activity, R.raw.couchdroid_pouchdb_helper),
                "window.console.log('PouchDB is: ' + typeof PouchDB);",
                "window.console.log('CouchDroid is: ' + typeof CouchDroid);")));
    }
    
    /* package */ void loadJavascript(final CharSequence javascript) {
        webView.post(new Runnable() {
            
            @Override
            public void run() {
                webView.loadUrl(new StringBuilder("javascript:")
                        .append("document.addEventListener('DOMContentLoaded', function() {")
                        .append(javascript)
                        .append("});")
                        .append(javascript).toString());
            }
        });
    }
    
    @SuppressLint("NewApi")
    private void initWebView() {
        
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        
        log.d("creating new webivew");
        webView = new WebView(activity);
        webView.setVisibility(View.GONE);
        
        viewGroup.addView(webView);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDatabaseEnabled(false); // we're overriding websql
        webView.getSettings().setDomStorageEnabled(false); // pouch needs to call localStorage.  we fake it.
        webView.getSettings().setAllowContentAccess(false); // don't need it
        
        webView.setWebChromeClient(new MyWebChromeClient());
        
        
        webView.addJavascriptInterface(new SQLiteJavascriptInterface(activity, webView), 
                SQLiteJavascriptInterface.class.getSimpleName());
        webView.addJavascriptInterface(new XhrJavascriptInterface(webView), 
                XhrJavascriptInterface.class.getSimpleName());
        webView.addJavascriptInterface(new ProgressReporter(activity, progressListener), 
                ProgressReporter.class.getSimpleName());
        webView.addJavascriptInterface(new JSInterfaceVerifier(), 
                JSInterfaceVerifier.class.getSimpleName());
        
        final String html = new StringBuilder("<html><head></head><body>")
                .append(USE_WEINRE
                        ? "<script src='" + WEINRE_URL +"/target/target-script-min.js#anonymous'></script>"
                        : "")
                .append("</body></html>").toString();
        
        if (USE_WEINRE) {
            // fake url to contact weinre
            webView.loadDataWithBaseURL("http://localhost:9362", html, "text/html", "UTF-8", null);
        } else {
            webView.loadData(html, "text/html", "UTF-8");
        }
        
        log.d("loaded webview data: %s", html);
    }
    
    public void close() {
        log.i("close()");
        activity = null; // release context resources
        if (webView != null) {
            webView.loadUrl("about:blank");
        }
    }
    
    private class MyWebChromeClient extends WebChromeClient {
        
        @Override
        @Deprecated
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            //log.d(message);
        }
    }
    
    /* 
     * stupid hack because there's a race condition with addJavascriptInterface, so we use
     * this callback to verify that the objects are all loaded in the JS environment
     * 
     */
    private class JSInterfaceVerifier {
        
        @JavascriptInterface
        public void callback() {
            log.d("notify()");

            if (jsInterfaceVerifierCaller.cancelled) {
                return;
            }
            jsInterfaceVerifierCaller.cancelled = true;
            
            loadInitialJavascript();
            
            if (onReadyListener != null) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            onReadyListener.onReady(CouchDroidRuntime.this);
                        }
                    });
                }
            } else {
                log.e("onReadyListener is null!");
            }
        }
    }
    
    private class JSInterfaceVerifierCaller extends AsyncTask<Void, Void, Void> {

        private boolean cancelled;
        
        @Override
        protected Void doInBackground(Void... params) {
            
            while (!cancelled) {
                
                log.d("JSInterfaceVerifierCaller notify");
                loadJavascript("if (!!window.JSInterfaceVerifier){JSInterfaceVerifier.callback();}");
                
                try {
                    Thread.sleep(JSINTERFACE_VERIFIER_CALLER_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            log.d("JSInterfaceVerifierCaller done");
            return null;
        }
    }
    
    public static interface OnReadyListener {
        public void onReady(CouchDroidRuntime runtime);
    }

    /*package*/ void addListener(CouchDroidProgressListener listener) {
        clientProgressListeners.add(listener);
    }
}
