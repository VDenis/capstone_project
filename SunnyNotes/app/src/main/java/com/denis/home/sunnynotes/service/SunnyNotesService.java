package com.denis.home.sunnynotes.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;

import com.denis.home.sunnynotes.BuildConfig;
import com.denis.home.sunnynotes.data.NoteColumns;
import com.denis.home.sunnynotes.data.NoteProvider;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Denis on 20.03.2016.
 */
public class SunnyNotesService extends IntentService {

    private Context mContext;

    public SunnyNotesService() {
        super(SunnyNotesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO load notes from Dropbox
        Timber.d("onHandleIntent");
        if (mContext == null) {
            mContext = this;
        }

        Test();
    }

    private void Test() {
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        DbxClientV2 client = new DbxClientV2(config, BuildConfig.DEVELOP_ONLY_DROPBOX_ACCESS_TOKEN);

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
            entries = client.files.listFolder("/python-test").getEntries();
        } catch (DbxException e) {
            e.printStackTrace();
        }

        Cursor initQueryCursor;
        initQueryCursor = mContext.getContentResolver().query(NoteProvider.Notes.CONTENT_URI,
                new String[]{"Distinct " + NoteColumns.FILE_ID}, null,
                null, null);
        if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
            try {
                mContext.getContentResolver().applyBatch(NoteProvider.AUTHORITY,
                        fileMetadataToContentVals(entries));
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        } else {
            DatabaseUtils.dumpCursor(initQueryCursor);
            initQueryCursor.moveToFirst();
            for (int i = 0; i < initQueryCursor.getCount(); i++) {
                String fileName = initQueryCursor.getString(initQueryCursor.getColumnIndex("file_name"));

                initQueryCursor.moveToNext();
            }
        }
    }

    public static ArrayList fileMetadataToContentVals(List<Metadata> entries) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        for (Metadata metadata : entries) {
            if (metadata instanceof FileMetadata) {
                batchOperations.add(buildBatchOperation((FileMetadata) metadata));
            }
            Timber.d(metadata.getPathLower());
        }
        return batchOperations;
    }

    private static ContentProviderOperation buildBatchOperation(FileMetadata fileMetadata) {
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
}
