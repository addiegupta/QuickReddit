package com.example.android.quickreddit.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = SubredditDatabase.VERSION)
public final class SubredditDatabase {


    public static final int VERSION = 1;

    @Table(ListColumns.class) public static final String SUBREDDITS = "subreddits";
}
