package com.denis.home.sunnynotes.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.denis.home.sunnynotes.R;
import com.denis.home.sunnynotes.Utility;
import com.denis.home.sunnynotes.data.NoteColumns;
import com.denis.home.sunnynotes.data.NoteProvider;

import timber.log.Timber;

/**
 * Created by Denis on 28.03.2016.
 */
public class RecentNotesWidgetRemoteViewsService extends RemoteViewsService
{
    private static final String[] NOTES_COLUMNS = {
            NoteColumns._ID,
            NoteColumns.FILE_NAME,
            NoteColumns.SERVER_MOD_TIME,
    };
    // these indices must match the projection
    static final int INDEX_NOTE_ID = 0;
    static final int INDEX_NOTE_FILE_NAME = 1;
    static final int INDEX_NOTE_SERVER_MOD_TIME = 2;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(NoteProvider.Notes.CONTENT_URI,
                        NOTES_COLUMNS,
                        null,
                        null,
                        //NoteColumns.SERVER_MOD_TIME + " DESC");
                        NoteColumns.SERVER_MOD_TIME + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_recent_notes_list_item);
                String title = Utility.stripDotTxtInString(data.getString(INDEX_NOTE_FILE_NAME));
                int noteId = data.getInt(INDEX_NOTE_ID);

                views.setTextViewText(R.id.widget_title, title);

                final Intent fillInIntent = new Intent();
                Uri noteUri = NoteProvider.Notes.withId(noteId);
                fillInIntent.setData(noteUri);
                Timber.d("Widget Resent notes, Note Uri: " + noteUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_recent_notes_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(data.getColumnIndex(NoteColumns._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
