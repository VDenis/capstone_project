package com.denis.home.sunnynotes.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Denis on 25.03.2016.
 */
public class SunnyNotesServiceHelper {

    public static void Sync(Context context) {
        Intent intent = new Intent(context, SunnyNotesService.class);
        context.startService(intent);
    }

    static void sendRefreshStateCallback(Context context, boolean status) {
        Intent localIntent;

        localIntent =
                new Intent(Constants.BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(Constants.EXTENDED_DATA_STATUS, status);

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    public final class Constants {
        // Defines a custom Intent action
        public static final String BROADCAST_ACTION =
                "com.denis.home.sunnynotes.service.sunnynotes.BROADCAST";
        // Defines the key for the status "extra" in an Intent
        public static final String EXTENDED_DATA_STATUS =
                "com.denis.home.sunnynotes.service.sunnynotes.STATUS";
    }
}
