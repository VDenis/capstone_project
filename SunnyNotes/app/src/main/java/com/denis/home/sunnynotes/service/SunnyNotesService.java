package com.denis.home.sunnynotes.service;

import android.app.IntentService;
import android.content.Intent;

import com.denis.home.sunnynotes.BuildConfig;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.util.List;

import timber.log.Timber;

/**
 * Created by Denis on 20.03.2016.
 */
public class SunnyNotesService extends IntentService {

    public SunnyNotesService() {
        super(SunnyNotesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO load notes from Dropbox
        Timber.d("onHandleIntent");

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
            entries = client.files.listFolder("").getEntries();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        for (Metadata metadata : entries) {
            Timber.d(metadata.getPathLower());
        }
        
    }
}
