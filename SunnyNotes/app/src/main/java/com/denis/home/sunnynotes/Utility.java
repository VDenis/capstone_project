package com.denis.home.sunnynotes;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Denis on 23.03.2016.
 */
public class Utility {
    public static String stripDotTxtInString(String text) {
        return text.replace(".txt", "");
    }

    public static String readTxtFile(Context context, String relativePath) {
        File path = context.getFilesDir();
        File file = new File(path + relativePath);

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) { e.printStackTrace();}

        return text.toString();
    }

    public static String getUserLocale() {
        return "en_US";
    }

    public static String getDropboxClientIdentifier() {
        return "dropbox/java-tutorial";
    }

    public static String getDropboxAppRootFolder() {
        return "/python-test";
    }

}
