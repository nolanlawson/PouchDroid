package com.pouchdb.pouchdroid.appforunittests;

import android.os.Bundle;
import android.view.Menu;

import com.pouchdb.pouchdroid.PouchDroidActivity;
import com.pouchdb.pouchdroid.PouchDroid;

public class MainActivity extends PouchDroidActivity {

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
    protected void onPouchDroidReady(PouchDroid pouchDroid) {
    }
}
