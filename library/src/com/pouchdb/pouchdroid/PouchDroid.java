package com.pouchdb.pouchdroid;

import java.util.Arrays;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.pouchdb.pouchdroid.pouch.PouchJavascriptInterface;
import com.pouchdb.pouchdroid.sqlite.SQLiteJavascriptInterface;
import com.pouchdb.pouchdroid.util.ResourceUtil;
import com.pouchdb.pouchdroid.util.UtilLogger;
import com.pouchdb.pouchdroid.xhr.XhrJavascriptInterface;

@SuppressLint("SetJavaScriptEnabled")
public class PouchDroid {

    private static UtilLogger log = new UtilLogger(PouchDroid.class);

    private static final int JSINTERFACE_VERIFIER_CALLER_INTERVAL = 1000; // ms
    
    private static final boolean DEBUG_MODE = true;
    private static final boolean USE_WEINRE = DEBUG_MODE;
    private static final boolean USE_MINIFIED_POUCH = !USE_WEINRE;
    private static final boolean USE_MINIFIED_COUCHDROID = !USE_WEINRE;
    private static final String WEINRE_URL = "http://192.168.0.3:8080";
    
    private Activity activity;
    private WebView webView;
    private JSInterfaceVerifierCaller jsInterfaceVerifierCaller;
    private OnReadyListener onReadyListener;

    private PouchJavascriptInterface pouchJavascriptInterface;
    
    /**
     * Start a new PouchDroid in the given activity.  The standard idiom is:
     * 
     * <pre>
     * {@code
     * 
     * private PouchDroid pouchDroid;
     * 
     * public void onResume() {
     *   super.onResume();
     *   pouchDroid = new PouchDroid(this, new PouchDroid.OnReadyListener(){
     *     
     *     public void onReady(PouchDroid pouchDroid) {
     *       // do stuff
     *     }
     *   });
     * }
     * 
     * public void onPause() {
     *   super.onPause();
     *   pouchDroid.close();
     * }
     * </pre>
     * 
     * @param activity
     * @param onReadyListener
     */
    public PouchDroid(Activity activity, OnReadyListener onReadyListener) {
        this.activity = activity;
        this.onReadyListener = onReadyListener;
        this.pouchJavascriptInterface = new PouchJavascriptInterface(this);
        
        initWebView();
        
        jsInterfaceVerifierCaller = new JSInterfaceVerifierCaller();
        jsInterfaceVerifierCaller.execute((Void)null);
        
        
    }
    
    public Activity getActivity() {
        return activity;
    }

    /* package */ WebView getWebView() {
        return webView;
    }

    private void loadInitialJavascript() {
        
        // in Android 4.4+, IndexedDB is now available, so we need to remove it from the Pouch adapter list
        // TODO: compile PouchDB without idb at all
        String removeIdb = "delete PouchDB.adapters.idb;";
        
        loadJavascript(TextUtils.join(";", Arrays.asList(
                ResourceUtil.loadTextFile(activity, USE_MINIFIED_COUCHDROID ? R.raw.pouchdroid_min : R.raw.pouchdroid),
                (ResourceUtil.loadTextFile(activity, USE_MINIFIED_POUCH ? R.raw.pouchdb_min : R.raw.pouchdb)),
                removeIdb
                )));
    }
    
    /**
     * Load the given Javascript in is own function, when the DOM is ready, on the UI thread.
     * @param javascript
     */
    public void loadJavascript(final CharSequence javascript) {
        log.d("loadJavascript(): %s", javascript);
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
        
        log.d("creating new webview");
        webView = new WebView(activity);
        webView.setVisibility(View.GONE);
        
        viewGroup.addView(webView);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDatabaseEnabled(false); // we're overriding websql
        webView.getSettings().setDomStorageEnabled(USE_WEINRE); // pouch needs to call localStorage.  we fake it.
        if (Build.VERSION.SDK_INT >= 11) {
            webView.getSettings().setAllowContentAccess(false); // don't need it
        }
        
        webView.setWebChromeClient(new MyWebChromeClient());
        
        // TODO: combine all these javascript interfaces together, cordova-style
        webView.addJavascriptInterface(new SQLiteJavascriptInterface(this), "SQLiteJavascriptInterface");
        webView.addJavascriptInterface(new XhrJavascriptInterface(this), "XhrJavascriptInterface");
        webView.addJavascriptInterface(pouchJavascriptInterface, "PouchJavascriptInterface");
        webView.addJavascriptInterface(new JSInterfaceVerifier(), "JSInterfaceVerifier");
        
        final String html = new StringBuilder("<html><head></head><body>")
                .append(USE_WEINRE
                        ? "<script src='" + WEINRE_URL +"/target/target-script-min.js#anonymous'></script>"
                        : "")
                .append("</body></html>").toString();
        
        if (USE_WEINRE) {
            // fake url to contact weinre
            int port = new Random().nextInt(2000) + 8000;
            webView.loadDataWithBaseURL("http://localhost:"+port, html, "text/html", "UTF-8", null);
        } else {
            webView.loadData(html, "text/html", "UTF-8");
        }
        
        log.d("loaded webview data: %s", html);
    }
    
    public void close() {
        log.i("close()");
        activity = null; // release context resources (TODO: is this necessary?)
    }
    
    
    private class MyWebChromeClient extends WebChromeClient {
        
        @Override
        @Deprecated
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            if (Build.VERSION.SDK_INT < 11) {
                //  I believe they started logging the "Web Console" in 11, then stopped in kitkat
                Log.i("Web Console", message);
            }
        }

        @Override
        @SuppressLint("NewApi")
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (Build.VERSION.SDK_INT >= 19) {
                Log.i("Web Console", consoleMessage.message());
                return true;
            }
            return false;
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
                            onReadyListener.onReady(PouchDroid.this);
                        }
                    });
                }
            } else {
                log.e("onReadyListener is null!");
            }
        }
    }
    
    /**
     * Confirms that our JavaScript bridge is actually operational
     * @author nolan
     *
     */
    private class JSInterfaceVerifierCaller extends AsyncTask<Void, Void, Void> {

        private boolean cancelled;
        
        @Override
        protected Void doInBackground(Void... params) {
            
            while (!cancelled) {
                
                log.d("JSInterfaceVerifierCaller notify");
                
                loadJavascript("if (!!window.SQLiteJavascriptInterface " +
                		"&& !!window.XhrJavascriptInterface " +
                		"&& !!window.PouchJavascriptInterface " +
                		"&& !!window.JSInterfaceVerifier){JSInterfaceVerifier.callback();}");
                
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
        public void onReady(PouchDroid pouchDroid);
    }

    public PouchJavascriptInterface getPouchJavascriptInterface() {
        return pouchJavascriptInterface;
    }
}
