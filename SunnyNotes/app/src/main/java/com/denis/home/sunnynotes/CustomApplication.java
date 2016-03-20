package com.denis.home.sunnynotes;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Denis on 20.03.2016.
 */
public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
