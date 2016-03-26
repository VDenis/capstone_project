package com.denis.home.sunnynotes.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;

import com.denis.home.sunnynotes.R;
import com.denis.home.sunnynotes.Utility;
import com.dropbox.core.android.Auth;

/**
 * Created by Denis on 26.03.2016.
 */
public abstract class DropboxPreferenceFragment extends PreferenceFragment {
    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity();
        SharedPreferences prefs = context.getSharedPreferences(Utility.getSharedPreferencesName(), Context.MODE_PRIVATE);
        String accessToken = prefs.getString(getString(R.string.pref_access_token_key), null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(getString(R.string.pref_access_token_key), accessToken).apply();
                initAndFirstLoadData(accessToken);
            } else {
                registrationInterruption();
            }
        } else {
            initAndFirstLoadData(accessToken);
        }
    }

    private void initAndFirstLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        //PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        loadData();
    }

    protected abstract void loadData();

    protected abstract void registrationInterruption();

    protected boolean hasToken() {
        Context context = getActivity();
        SharedPreferences prefs = context.getSharedPreferences(Utility.getSharedPreferencesName(), Context.MODE_PRIVATE);
        String accessToken = prefs.getString(getString(R.string.pref_access_token_key), null);
        return accessToken != null;
    }
}
