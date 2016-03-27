package com.denis.home.sunnynotes.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Denis on 24.03.2016.
 */
public class ActionServiceHelper {
    public static void Add(Context context, String localPath) {
        /*
         * Creates a new Intent to start the ActionService
         * IntentService. Passes a URI in the
         * Intent's "data" field.
         */
        if (localPath != null && !localPath.isEmpty()) {
            Intent serviceIntent = new Intent(context, ActionService.class);
            serviceIntent.putExtra(ActionService.OPERATION, ActionService.OPERATION_ADD);
            serviceIntent.putExtra(ActionService.OPERATION_ADD_LOWER_FILE_PATH, localPath);
            // Starts the IntentService
            context.startService(serviceIntent);
        }
    }

    public static void Delete(Context context, Uri noteUri) {
        /*
         * Creates a new Intent to start the ActionService
         * IntentService. Passes a URI in the
         * Intent's "data" field.
         */
        if (noteUri != null) {
            Intent serviceIntent = new Intent(context, ActionService.class);
            serviceIntent.putExtra(ActionService.OPERATION, ActionService.OPERATION_DELETE);
            serviceIntent.setData(noteUri);
            // Starts the IntentService
            context.startService(serviceIntent);
        }
    }


    public static void Update(Context context, Uri noteUri, String titleName) {
        if (noteUri != null) {
            Intent serviceIntent = new Intent(context, ActionService.class);
            serviceIntent.putExtra(ActionService.OPERATION, ActionService.OPERATION_UPDATE);
            serviceIntent.putExtra(ActionService.OPERATION_UPDATE_NEW_FILE_NAME, titleName + ".txt");
            serviceIntent.setData(noteUri);
            context.startService(serviceIntent);
        }
    }
}
