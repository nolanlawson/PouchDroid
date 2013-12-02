package com.example.example1;

import android.app.Activity;
import android.os.Bundle;

import com.nolanlawson.couchdbsync.CouchdbSync;

public class MainActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        CouchdbSync.Builder.create(this).build().start();
    }    
    
}
