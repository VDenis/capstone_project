package com.denis.home.sunnynotes.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.denis.home.sunnynotes.BuildConfig;
import com.denis.home.sunnynotes.Utility;
import com.denis.home.sunnynotes.data.NoteColumns;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
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
    public final static String OPERATION_DELETE = "OPERATION_DELETE";

    private Context mContext;
    DbxClientV2 client;

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
        Uri noteUri = intent.getData();

        if (noteUri == null)
            return;
        if (requestOperation != null) {

            DbxRequestConfig config = new DbxRequestConfig(Utility.getDropboxClientIdentifier(), Utility.getUserLocale());
            client = new DbxClientV2(config, BuildConfig.DEVELOP_ONLY_DROPBOX_ACCESS_TOKEN);

            // Get current account info
            FullAccount account = null;
            try {
                account = client.users.getCurrentAccount();
            } catch (DbxException e) {
                e.printStackTrace();
            }

            Cursor initQueryCursor;
            initQueryCursor = mContext.getContentResolver().query(
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
                    switch (requestOperation) {
                        case OPERATION_ADD:
                            uploadFileToDropbox(serverLowerPath);
                            break;
                        case OPERATION_DELETE:
                            deleteFileOnDropbox(serverLowerPath);
                            break;
                        default:
                            // TODO: trow exeption
                            throw new IllegalArgumentException();
                    }
                }
            }
        }
    }

    private boolean uploadFileToDropbox(String serverLowerPath) {
        boolean isAdd = false;
        File appDir = getFilesDir();
        File file = new File(appDir + serverLowerPath);

        if (!file.exists()) { // TODO or if (file != null) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                client.files.upload(serverLowerPath).uploadAndFinish(in);
                isAdd = true;
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
        return isAdd;
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
