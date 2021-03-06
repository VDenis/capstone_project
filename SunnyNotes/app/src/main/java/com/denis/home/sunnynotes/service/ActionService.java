package com.denis.home.sunnynotes.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.denis.home.sunnynotes.Utility;
import com.denis.home.sunnynotes.data.NoteColumns;
import com.denis.home.sunnynotes.dropbox.DropboxClientFactory;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Created by Denis on 24.03.2016.
 */
public class ActionService extends IntentService {

    public final static String OPERATION = "OPERATION";

    public final static String OPERATION_ADD = "OPERATION_ADD";
    public final static String OPERATION_ADD_LOWER_FILE_PATH = "OPERATION_ADD_LOWER_FILE_PATH";

    public final static String OPERATION_DELETE = "OPERATION_DELETE";
    public final static String OPERATION_UPDATE = "OPERATION_UPDATE";
    public final static String OPERATION_UPDATE_NEW_FILE_NAME = "OPERATION_UPDATE_NEW_FILE_NAME";
    DbxClientV2 client;
    private Context mContext;

    public ActionService() {
        super(ActionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("onHandleIntent");
        if (mContext == null) {
            mContext = this;
        }

        String requestOperation = intent.getStringExtra(OPERATION);
        String lowerFilePath = intent.getStringExtra(OPERATION_ADD_LOWER_FILE_PATH);
        String newFileName = intent.getStringExtra(OPERATION_UPDATE_NEW_FILE_NAME);
        Uri noteUri = intent.getData();


/*        DbxRequestConfig config = new DbxRequestConfig(Utility.getDropboxClientIdentifier(), Utility.getUserLocale());
        client = new DbxClientV2(config, BuildConfig.DEVELOP_ONLY_DROPBOX_ACCESS_TOKEN);*/
        client = DropboxClientFactory.getClient();
        if (client == null)
            return;

        // Get current account info
        FullAccount account = null;
        try {
            account = client.users.getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
        }

        if (requestOperation.equals(OPERATION_ADD)) {
            if (lowerFilePath != null) {
                FileMetadata fileMetadata = uploadFileToDropbox(lowerFilePath);
                Utility.addNoteInDB(mContext, fileMetadata);
            }
        } else {

            if (noteUri == null)
                return;
            if (requestOperation != null) {
                Cursor initQueryCursor;
                initQueryCursor = mContext.getContentResolver().query(
                        noteUri,
                        new String[]{NoteColumns.FILE_ID, NoteColumns.FILE_NAME, NoteColumns.DISPLAY_PATH, NoteColumns.LOWER_PATH},
                        null,
                        null,
                        null
                );
                if (initQueryCursor != null && initQueryCursor.getCount() != 0) {
                    if (initQueryCursor.moveToFirst()) {
                        String serverLowerPath = initQueryCursor.getString(initQueryCursor.getColumnIndex(NoteColumns.LOWER_PATH));
                        switch (requestOperation) {
                            case OPERATION_DELETE:
                                deleteFileOnDropbox(serverLowerPath);
                                Utility.deleteNoteInDB(mContext, noteUri);
                                break;
                            case OPERATION_UPDATE:
                                FileMetadata fileMetadata = uploadFileToDropbox(serverLowerPath, newFileName);
                                Utility.moveTxtFile(mContext, noteUri, fileMetadata.getPathLower());
                                Utility.updateNoteInDB(mContext, noteUri, fileMetadata);
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                    }
                }
            }
        }
        Utility.updateWidgets(mContext);
    }

    private FileMetadata uploadFileToDropbox(String serverLowerPath) {
        return uploadFileToDropbox(serverLowerPath, "");
    }

    private FileMetadata uploadFileToDropbox(String serverLowerPath, String newFileName) {
        FileMetadata fileMetadata = null;
        File appDir = getFilesDir();
        File file = new File(appDir + serverLowerPath);

        if (file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                fileMetadata = client.files.uploadBuilder(serverLowerPath).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
            } catch (DbxException | IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (!newFileName.isEmpty()) {
            if (fileMetadata != null && !newFileName.equals(fileMetadata.getName())) {
                String newFilePath = fileMetadata.getPathLower().replace(fileMetadata.getName(), newFileName);
                try {
                    fileMetadata = (FileMetadata) client.files.move(fileMetadata.getPathLower(), newFilePath);
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileMetadata;
    }

    private boolean deleteFileOnDropbox(String serverLowerPath) {
        boolean isDelete = false;

        try {
            client.files.delete(serverLowerPath);
            isDelete = true;
        } catch (DbxException e) {
            e.printStackTrace();
        }

        return isDelete;
    }
}
