package com.nolanlawson.couchdbsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.nolanlawson.couchdbsync.util.ResourceUtil;
import com.nolanlawson.couchdbsync.util.UtilLogger;

@SuppressLint("SetJavaScriptEnabled")
public class CouchdbSync {

    private static UtilLogger log = new UtilLogger(CouchdbSync.class);
    
    private Activity activity;
    private List<SqliteTable> sqliteTables = new ArrayList<SqliteTable>();
    
    private CouchdbSync(Activity activity) {
        this.activity = activity;
    }
    
    public void start() {
        final WebView webView = new WebView(activity);
        webView.setVisibility(View.VISIBLE);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        viewGroup.addView(webView);
        
        webView.getSettings().setJavaScriptEnabled(true);
        
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            @Deprecated
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                log.d(message);
            }
        });
        
        log.d("attempting to load javascript");
        webView.post(new Runnable() {
            
            @Override
            public void run() {
                
                String webPage = new StringBuilder("<html><body><script language='javascript'>")
                        .append(ResourceUtil.loadTextFile(activity, R.raw.pouchdb))
                        .append("</script>")
                        .append("<script language='javascript'>")
                        .append("window.console.log('pouchdb is: ' + PouchDB);")
                        .append("</script>")
                        .append("</body></html>")
                        .toString();
                
                webView.loadData(webPage, "text/html", "UTF-8");
            }
        });
        
        
    }
    
    public void stop() {
        this.activity = null; // release resources
    }
    
    public static class Builder {
        
        private CouchdbSync couchdbSync;
        
        private Builder(Activity activity) {
            this.couchdbSync = new CouchdbSync(activity);
        }
        
        public static Builder create(Activity activity) {
            return new Builder(activity);
        }
        
        public Builder addSqliteTable(String tableName, String... columnsToUseAsId) {
            
            SqliteTable sqliteTable = new SqliteTable();
            sqliteTable.setName(tableName);
            sqliteTable.setIdColumns(Arrays.asList(columnsToUseAsId));
            
            if (columnsToUseAsId.length == 0) {
                throw new IllegalArgumentException("You must supply as least one column to use as a unique ID.");
            }
            
            couchdbSync.sqliteTables.add(sqliteTable);
            return this;
        }
        
        public CouchdbSync build() {
            return couchdbSync;
        }
    }
}
