package com.denis.home.sunnynotes.dropbox;

/**
 * Created by Denis on 25.03.2016.
 */
/* From com.dropbox.core.examples.android */

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.denis.home.sunnynotes.Utility;
import com.dropbox.core.android.Auth;

/**
 * Base class for Activities that require auth tokens
 * Will redirect to auth flow if needed
 */
public abstract class DropboxActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(Utility.getSharedPreferencesName(), MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString("access-token", accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        //PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        loadData();
    }

    protected abstract void loadData();

    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences(Utility.getSharedPreferencesName(), MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }
}
