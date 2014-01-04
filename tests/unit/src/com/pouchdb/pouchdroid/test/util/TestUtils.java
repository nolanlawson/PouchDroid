package com.pouchdb.pouchdroid.test.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static byte[] read(InputStream input) throws IOException {

        ByteArrayOutputStream output = null;
        try {
            byte[] buffer = new byte[4096];
            output = new ByteArrayOutputStream();
            int read = 0;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            if (output != null)
                output.close();
        }
        return output.toByteArray();
    }
    
    public static List<String> toHexList(byte[] input) {
        List<String> output = new ArrayList<String>(input.length);
        
        for (byte b : input) {
            output.add(Integer.toHexString(b & 0xFF));
        }
        
        return output;
    }
}
