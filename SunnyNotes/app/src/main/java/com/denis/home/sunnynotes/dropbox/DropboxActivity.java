package com.denis.home.sunnynotes.dropbox;

/**
 * Created by Denis on 25.03.2016.
 */
/* From com.dropbox.core.examples.android */

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.denis.home.sunnynotes.R;
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
        String accessToken = prefs.getString(getString(R.string.pref_access_token_key), null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(getString(R.string.pref_access_token_key), accessToken).apply();
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
        String accessToken = prefs.getString(getString(R.string.pref_access_token_key), null);
        return accessToken != null;
    }
}
