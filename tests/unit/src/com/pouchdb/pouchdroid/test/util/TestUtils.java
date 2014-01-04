package com.pouchdb.pouchdroid.test.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
