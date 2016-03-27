package com.denis.home.sunnynotes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.denis.home.sunnynotes.MainActivity;
import com.denis.home.sunnynotes.R;
import com.denis.home.sunnynotes.noteDetail.NoteDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class CreateNoteWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //context.startService(new Intent(context, CreateNoteIntentService.class));

        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_create_note);
        //views.setTextViewText(R.id.appwidget_text, widgetText);

/*        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.putExtra(MainActivity.CREATE_NEW_NOTE, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);*/

        boolean useDetailActivity = context.getResources()
                .getBoolean(R.bool.use_detail_activity);
        Intent clickIntentTemplate = useDetailActivity
                ? new Intent(context, NoteDetailActivity.class)
                : new Intent(context, MainActivity.class);

        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget, clickPendingIntentTemplate);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

