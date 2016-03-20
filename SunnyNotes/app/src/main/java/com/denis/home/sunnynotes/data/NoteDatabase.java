package com.denis.home.sunnynotes.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Denis on 20.03.2016.
 */
@Database(version = NoteDatabase.VERSION)
public class NoteDatabase {
    private NoteDatabase(){}

    public static final int VERSION = 1;

    @Table(NoteColumns.class) public static final String Notes = "notes";
}
