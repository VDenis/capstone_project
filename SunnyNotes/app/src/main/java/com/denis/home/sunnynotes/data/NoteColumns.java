package com.denis.home.sunnynotes.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Denis on 20.03.2016.
 */
public class NoteColumns {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String FILE_ID = "file_id";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String FILE_SIZE = "file_size";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String FILE_NAME = "file_name";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String SERVER_MOD_TIME = "server_mod_time";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String CLIENT_MOD_TIME = "client_mod_time";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String DISPLAY_PATH = "display_path";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String LOWER_PATH = "lower_path";

    @DataType(DataType.Type.TEXT)
    public static final String PENDING_ACTION = "pending_action";
}
