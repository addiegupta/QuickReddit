package com.example.android.quickreddit.data;


import android.net.Uri;

import com.example.android.quickreddit.BuildConfig;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;


@ContentProvider(authority = SubredditProvider.AUTHORITY, database = SubredditDatabase.class)
public class SubredditProvider {


    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;

    @TableEndpoint(table = SubredditDatabase.SUBREDDITS)
    public static class Subreddits {

        @ContentUri(
                path = "subreddits",
                type = "vnd.android.cursor.dir/subreddit",
                defaultSort = ListColumns.TITLE + " ASC")
        public static final Uri SUBREDDITS = Uri.parse("content://" + AUTHORITY + "/subreddits");

    }
}