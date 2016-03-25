package com.denis.home.sunnynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.denis.home.sunnynotes.data.NoteColumns;
import com.denis.home.sunnynotes.data.NoteProvider;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

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

    public static boolean updateTxtFile(Context context, Uri noteUri, String content) {
        boolean result = false;

        String relativePath = getLowerPathFromDB(context, noteUri);

        File path = context.getFilesDir();
        File file = new File(path + relativePath);

        try {
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
        // TODO change
        // "examples-v2-demo"
        return "dropbox/java-tutorial";
    }

    public static String getSharedPreferencesName() {
        return "sunny-notes";
    }

    public static String getDropboxAppRootFolder() {
        // TODO change
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

}
