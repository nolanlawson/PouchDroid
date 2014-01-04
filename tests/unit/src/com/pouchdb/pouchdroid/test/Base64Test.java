package com.pouchdb.pouchdroid.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;

import com.pouchdb.pouchdroid.appforunittests.MainActivity;
import com.pouchdb.pouchdroid.test.util.TestUtils;
import com.pouchdb.pouchdroid.util.Base64Compat;

public class Base64Test extends ActivityInstrumentationTestCase2<MainActivity> {
    
    @SuppressLint("NewApi")
    public Base64Test() {
        super(MainActivity.class);
    }
    
    public void testBase64() throws IOException {
        InputStream inputStream = getActivity().getAssets().open("android.png");
        
        byte[] png = TestUtils.read(inputStream);
        inputStream.close();
        
        String encoded = Base64Compat.encodeToString(png, Base64Compat.DEFAULT);
        byte[] decoded = Base64Compat.decode(encoded, Base64Compat.DEFAULT);
        
        assert(Arrays.equals(png, decoded));
    }
}
