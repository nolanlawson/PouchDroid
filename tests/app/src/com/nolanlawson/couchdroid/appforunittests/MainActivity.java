package com.nolanlawson.couchdroid.appforunittests;

import android.os.Bundle;
import android.view.Menu;

import com.nolanlawson.couchdroid.CouchDroidActivity;
import com.nolanlawson.couchdroid.CouchDroidRuntime;

public class MainActivity extends CouchDroidActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCouchDroidReady(CouchDroidRuntime runtime) {
    }
}
