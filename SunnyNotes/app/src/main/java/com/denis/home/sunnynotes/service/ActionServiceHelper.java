package com.denis.home.sunnynotes.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Denis on 24.03.2016.
 */
public class ActionServiceHelper {
    public static void Add(Context context, Uri noteUri) {
        /*
         * Creates a new Intent to start the ActionService
         * IntentService. Passes a URI in the
         * Intent's "data" field.
         */
        Intent serviceIntent = new Intent(context, ActionService.class);
        serviceIntent.putExtra(ActionService.OPERATION, ActionService.OPERATION_ADD);
        serviceIntent.setData(noteUri);
        // Starts the IntentService
        context.startService(serviceIntent);
    }

    public static void Delete(Context context, Uri noteUri) {
        /*
         * Creates a new Intent to start the ActionService
         * IntentService. Passes a URI in the
         * Intent's "data" field.
         */
        Intent serviceIntent = new Intent(context, ActionService.class);
        serviceIntent.putExtra(ActionService.OPERATION, ActionService.OPERATION_DELETE);
        serviceIntent.setData(noteUri);
        // Starts the IntentService
        context.startService(serviceIntent);
    }


}
