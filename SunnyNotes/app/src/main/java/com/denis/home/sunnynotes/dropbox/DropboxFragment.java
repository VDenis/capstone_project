package com.denis.home.sunnynotes.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.denis.home.sunnynotes.Utility;
import com.dropbox.core.android.Auth;

/**
 * Created by Denis on 25.03.2016.
 */
public abstract class DropboxFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity();
        SharedPreferences prefs = context.getSharedPreferences(Utility.getSharedPreferencesName(), Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString("access-token", accessToken).apply();
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
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }

}
