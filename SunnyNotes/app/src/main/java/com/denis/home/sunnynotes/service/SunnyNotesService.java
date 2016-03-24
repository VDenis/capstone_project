package com.denis.home.sunnynotes.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.denis.home.sunnynotes.BuildConfig;
import com.denis.home.sunnynotes.Utility;
import com.denis.home.sunnynotes.data.NoteColumns;
import com.denis.home.sunnynotes.data.NoteProvider;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Denis on 20.03.2016.
 */
public class SunnyNotesService extends IntentService {

    private Context mContext;
    DbxClientV2 client;

    public SunnyNotesService() {
        super(SunnyNotesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("onHandleIntent");
        if (mContext == null) {
            mContext = this;
        }

        Test();
    }

    private void Test() {
        DbxRequestConfig config = new DbxRequestConfig(Utility.getDropboxClientIdentifier(), Utility.getUserLocale());
        client = new DbxClientV2(config, BuildConfig.DEVELOP_ONLY_DROPBOX_ACCESS_TOKEN);

        // Get current account info
        FullAccount account = null;
        try {
            account = client.users.getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        Timber.d(account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory
        List<Metadata> entries = null;
        try {
            //entries = client.files.listFolder("").getEntries();
            // TODO App Folder
            entries = client.files.listFolder("/python-test").getEntries();
        } catch (DbxException e) {
            e.printStackTrace();
        }

        Cursor initQueryCursor;
        initQueryCursor = mContext.getContentResolver().query(NoteProvider.Notes.CONTENT_URI,
                new String[]{"Distinct " + NoteColumns.FILE_ID}, null,
                null, null);
        if (initQueryCursor == null || initQueryCursor.getCount() == 0) {

        } else {
            //DatabaseUtils.dumpCursor(initQueryCursor);
            //initQueryCursor.moveToFirst();

            for (Metadata metadata : entries) {
                if (metadata instanceof FileMetadata) {
                    FileMetadata fileMetadata = (FileMetadata) metadata;
                    if (deleteFile(fileMetadata)) {
                        Timber.d("Delete file: " + metadata.getName());
                    }
                }
            }

            // Delete all rows in database
            int deleteCount = mContext.getContentResolver().delete(NoteProvider.Notes.CONTENT_URI,
                    null, null);
            Timber.d(String.format("Deleted all row in database, count: %d", deleteCount));


            /*
            initQueryCursor = mContext.getContentResolver().query(NoteProvider.Notes.CONTENT_URI,
                    new String[]{NoteColumns.FILE_ID, NoteColumns.FILE_NAME, NoteColumns.DISPLAY_PATH, NoteColumns.LOWER_PATH},
                    null, null, null);
            for (int i = 0; i < initQueryCursor.getCount(); i++) {
                // java.lang.IllegalStateException: Couldn't read row 0, col -1 from CursorWindow.
                // Make sure the Cursor is initialized correctly before accessing data from it.
                initQueryCursor.moveToFirst();
                String fileName = initQueryCursor.getString(initQueryCursor.getColumnIndex(NoteColumns.FILE_NAME));

                initQueryCursor.moveToNext();
            }*/
        }
        if (initQueryCursor != null) {
            initQueryCursor.close();
        }


        try {
            mContext.getContentResolver().applyBatch(NoteProvider.AUTHORITY,
                    fileMetadataToContentVals(entries));
            Timber.d("Insert new data into database");
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public ArrayList fileMetadataToContentVals(List<Metadata> entries) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        for (Metadata metadata : entries) {
            if (metadata instanceof FileMetadata) {
                FileMetadata fileMetadata = (FileMetadata) metadata;
                if (downloadFile(fileMetadata)) {
                    Timber.d("Download file: " + metadata.getName());
                    batchOperations.add(buildBatchOperation(fileMetadata));
                }
            }
            Timber.d(metadata.getPathLower());
        }
        return batchOperations;
    }

    private ContentProviderOperation buildBatchOperation(FileMetadata fileMetadata) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                NoteProvider.Notes.CONTENT_URI);
        builder.withValue(NoteColumns.FILE_ID, fileMetadata.getId());
        builder.withValue(NoteColumns.FILE_NAME, fileMetadata.getName());
        builder.withValue(NoteColumns.SERVER_MOD_TIME, fileMetadata.getServerModified().toString());
        builder.withValue(NoteColumns.CLIENT_MOD_TIME, fileMetadata.getClientModified().toString());
        builder.withValue(NoteColumns.DISPLAY_PATH, fileMetadata.getPathDisplay());
        builder.withValue(NoteColumns.LOWER_PATH, fileMetadata.getPathLower());
        builder.withValue(NoteColumns.FILE_SIZE, fileMetadata.getSize());
        return builder.build();
    }

    private boolean downloadFile(FileMetadata fileMetadata) {
        boolean isDownload = false;

        String server_lower_path = fileMetadata.getPathLower();
        File path = getFilesDir();
        File folder = new File(path + server_lower_path.substring(0, server_lower_path.length() - fileMetadata.getName().length()));

        // Make sure the Downloads directory exists.
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                RuntimeException mException = new RuntimeException("Unable to create directory: " + path);
            }
        } else if (!folder.isDirectory()) {
            RuntimeException mException = new IllegalStateException("Download path is not a directory: " + path);
            return isDownload;
        }

        OutputStream outputStream = null;
        // Download the file.
        try {
            File file = new File(path + server_lower_path);
            outputStream = new FileOutputStream(file);
            FileMetadata result = client.files.download(server_lower_path).download(outputStream);
            isDownload = true;
        } catch (DbxException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isDownload;
    }

    private boolean deleteFile(FileMetadata fileMetadata) {
        boolean isDelete = false;

        String serverLowerPath = fileMetadata.getPathLower();
        File path = getFilesDir();
        File file = new File(path + serverLowerPath);

        // Make sure the Downloads directory exists.
        if (!file.exists()) { // TODO or if (file != null) {
            return isDelete;
        } else {
            if (file.delete()) {
                isDelete = true;
            }
        }

        return isDelete;
    }

}
