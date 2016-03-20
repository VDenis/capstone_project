package com.denis.home.sunnynotes.service;

import android.app.IntentService;
import android.content.Intent;

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
    }
}
