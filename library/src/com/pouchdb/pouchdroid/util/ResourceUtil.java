package com.pouchdb.pouchdroid.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class ResourceUtil {

    private static UtilLogger log = new UtilLogger(ResourceUtil.class);
    
    public static String loadTextFile(Context context, int resourceId) {

        InputStream is = context.getResources().openRawResource(resourceId);

        BufferedReader buff = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        try {
            while (buff.ready()) {
                sb.append(buff.readLine()).append("\n");
            }
        } catch (IOException e) {
            log.e("This should not happen", e);
        }

        return sb.toString();

    }
    
}
