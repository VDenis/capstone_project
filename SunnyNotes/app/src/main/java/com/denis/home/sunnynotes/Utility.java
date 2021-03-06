package com.denis.home.sunnynotes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.denis.home.sunnynotes.data.NoteColumns;
import com.denis.home.sunnynotes.data.NoteProvider;
import com.denis.home.sunnynotes.service.SunnyNotesServiceHelper;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }

    public static boolean updateTxtFile(Context context, Uri noteUri, String content) {
        boolean result = false;

        String relativePath = getLowerPathFromDB(context, noteUri);

        File path = context.getFilesDir();
        File file = new File(path + relativePath);

        try {
            // TODO check. when update filename and file content
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content);
            bw.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String createTxtFile(Context context, String filename, String content) {
        String result = "";

        String relativePath = new StringBuilder().
                append(getDropboxAppRootFolder()).
                append("/").
                append(filename).
                append(".txt").
                toString();

        File path = context.getFilesDir();
        File file = new File(path + relativePath);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content);
            bw.close();
            result = relativePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean moveTxtFile(Context context, Uri noteUri, String newPath) {
        boolean isRename = false;
        String relativePath = getLowerPathFromDB(context, noteUri);

        File path = context.getFilesDir();
        File fileFrom = new File(path + relativePath);
        File fileTo = new File(path + newPath);
        isRename = fileFrom.renameTo(fileTo);
        return isRename;
    }

    public static boolean deleteTxtFile(Context context, Uri noteUri) {
        boolean isDelete = false;
        String relativePath = getLowerPathFromDB(context, noteUri);

        File path = context.getFilesDir();
        File file = new File(path + relativePath);

        isDelete = file.delete();
        return isDelete;
    }

    public static int deleteNoteInDB(Context context, Uri noteUri) {
        int deleteCount = context.getContentResolver().delete(noteUri,
                null, null);
        return deleteCount;
    }

    public static int deleteAllNoteInDB(Context context) {
        Cursor initQueryCursor;
        int deleteCount = -1;

        initQueryCursor = context.getContentResolver().query(NoteProvider.Notes.CONTENT_URI,
                new String[]{NoteColumns.LOWER_PATH}, null,
                null, null);

        if (initQueryCursor != null && initQueryCursor.getCount() != 0) {
            ArrayList<String> mFilesPath = new ArrayList<String>();
            for (initQueryCursor.moveToFirst(); !initQueryCursor.isAfterLast(); initQueryCursor.moveToNext()) {
                // The Cursor is now set to the right position
                mFilesPath.add(initQueryCursor.getString(initQueryCursor.getColumnIndex(NoteColumns.LOWER_PATH)));
            }

            boolean isDelete = false;
            for (String deletePath : mFilesPath) {
                isDelete = false;

                File path = context.getFilesDir();
                File file = new File(path + deletePath);

                isDelete = file.delete();
            }

            // Delete all rows in database
            deleteCount = context.getContentResolver().delete(NoteProvider.Notes.CONTENT_URI,
                    null, null);
        }
        return deleteCount;
    }

    public static int updateNoteInDB(Context context, Uri noteUri, FileMetadata fileMetadata) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteColumns.FILE_NAME, fileMetadata.getName());
        contentValues.put(NoteColumns.FILE_ID, fileMetadata.getId());
        contentValues.put(NoteColumns.CLIENT_MOD_TIME, fileMetadata.getClientModified().toString());
        contentValues.put(NoteColumns.SERVER_MOD_TIME, fileMetadata.getServerModified().toString());
        contentValues.put(NoteColumns.FILE_SIZE, fileMetadata.getSize());
        contentValues.put(NoteColumns.LOWER_PATH, fileMetadata.getPathLower());
        contentValues.put(NoteColumns.DISPLAY_PATH, fileMetadata.getPathDisplay());

        int updateCount = context.getContentResolver().update(noteUri, contentValues, null, null);
        return updateCount;
    }

    public static Uri addNoteInDB(Context context, FileMetadata fileMetadata) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteColumns.FILE_NAME, fileMetadata.getName());
        contentValues.put(NoteColumns.FILE_ID, fileMetadata.getId());
        contentValues.put(NoteColumns.CLIENT_MOD_TIME, fileMetadata.getClientModified().toString());
        contentValues.put(NoteColumns.SERVER_MOD_TIME, fileMetadata.getServerModified().toString());
        contentValues.put(NoteColumns.FILE_SIZE, fileMetadata.getSize());
        contentValues.put(NoteColumns.LOWER_PATH, fileMetadata.getPathLower());
        contentValues.put(NoteColumns.DISPLAY_PATH, fileMetadata.getPathDisplay());

        Uri result = context.getContentResolver().insert(NoteProvider.Notes.CONTENT_URI, contentValues);
        return result;
    }

    public static String getUserLocale() {
        return Locale.getDefault().toString();
        //return "en_US";
    }

    public static String getDropboxClientIdentifier() {
        // "examples-v2-demo"
        return "dropbox/java-tutorial";
    }

    public static String getSharedPreferencesName() {
        return "sunny-notes";
    }

    public static String getDropboxAppRootFolder() {
        //return "/python-test";
        return "";
    }

    public static String getLowerPathFromDB(Context context, Uri noteUri) {
        if (noteUri == null) {
            return null;
        }

        Cursor initQueryCursor;
        initQueryCursor = context.getContentResolver().query(
                noteUri,
                new String[]{NoteColumns.FILE_ID, NoteColumns.FILE_NAME, NoteColumns.DISPLAY_PATH, NoteColumns.LOWER_PATH},
                null,
                null,
                null
        );
        if (initQueryCursor == null || initQueryCursor.getCount() == 0) {

        } else {
            if (initQueryCursor.moveToFirst()) {
                String serverLowerPath = initQueryCursor.getString(initQueryCursor.getColumnIndex(NoteColumns.LOWER_PATH));
                return serverLowerPath;
            }
        }
        return null;
    }

    public static boolean getNoteIdIfExist(Context context, String noteTitle) {
        return getNoteIdIfExist(context, noteTitle, null);
    }

    public static boolean getNoteIdIfExist(Context context, String noteTitle, Uri noteUri) {
        boolean result = false;
        String filename = noteTitle + ".txt";
        Cursor initQueryCursor;
/*        initQueryCursor = context.getContentResolver().query(NoteProvider.Notes.CONTENT_URI,
                new String[]{"Distinct " + NoteColumns.FILE_NAME},
                NoteColumns.FILE_NAME + "=?", new String[]{filename},
                null);*/

        initQueryCursor = context.getContentResolver().query(NoteProvider.Notes.CONTENT_URI,
                new String[]{"Distinct " + NoteColumns.FILE_NAME, NoteColumns._ID},
                NoteColumns.FILE_NAME + "=?", new String[]{filename},
                null);
        try {
            if (initQueryCursor != null && initQueryCursor.getCount() != 0) {
                initQueryCursor.moveToNext();
                int id = initQueryCursor.getInt(initQueryCursor.getColumnIndex(NoteColumns._ID));
                if (noteUri != null && NoteProvider.Notes.withId(id).equals(noteUri)) {
                    result = false;
                } else {
                    result = true;
                }
            }
        } finally {
            if (initQueryCursor != null)
                initQueryCursor.close();
        }
        return result;
    }

    // validating email id
    public static boolean isValidNoteTitle(String noteTitle) {
        if (noteTitle == null) {
            return false;

        } else if (noteTitle.isEmpty() || noteTitle.length() < 2) {
            return false;
        } else {
            String EMAIL_PATTERN = "[0-9a-zA-Z_]+";

            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            Matcher matcher = pattern.matcher(noteTitle);
            return matcher.matches();
        }
    }


    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    static public void deleteUserAccount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Utility.getSharedPreferencesName(), Context.MODE_PRIVATE);
        //prefs.edit().putString(context.getString(R.string.pref_access_token_key), "").apply();
        prefs.edit().remove(context.getString(R.string.pref_access_token_key)).apply();
    }



    //---------------

    static public void updateWidgets(Context context) {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(SunnyNotesServiceHelper.Constants.ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
