package com.denis.home.sunnynotes.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Denis on 20.03.2016.
 */
@ContentProvider(authority = NoteProvider.AUTHORITY, database = NoteDatabase.class)
public class NoteProvider {
    public static final String AUTHORITY = "com.denis.home.sunnynotes.data.NoteProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        String NOTES = "notes";
    }

    @TableEndpoint(table = NoteDatabase.Notes)
    public static class Notes {
        @ContentUri(
                path = Path.NOTES,
                type = "vnd.android.cursor.dir/note"
        )
        public static final Uri CONTENT_URI = buildUri(Path.NOTES);

        @InexactContentUri(
                name = "NOTE_ID",
                path = Path.NOTES + "/*",
                type = "vnd.android.cursor.item/note",
                whereColumn = NoteColumns._ID,
                pathSegment = 1
        )
        public static Uri withId(int id) {
            return buildUri(Path.NOTES, Integer.toString(id));
        }
    }
}
