package com.example.android.quickreddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;

import com.example.android.quickreddit.R;
import com.example.android.quickreddit.data.ListColumns;
import com.example.android.quickreddit.model.Thread;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.quickreddit.ui.SubredditsActivity.URI_SUBREDDITS;


/**
 * Created to handle a few methods usable by both the MainActivity and the AppWidget
 */
public final class DataUtils {

    private static Cursor mSubredditCursor;
    private static ArrayList<List<Thread>> mListOfThreadLists;
    private static DataUtilsActivityCallback mActivityCallback;
    private static DataUtilsWidgetCallback mWidgetCallback;
    private static boolean mIsWidget;

    public interface DataUtilsActivityCallback {
        void showRefreshIndicator();

        void setupRecyclerView(ArrayList<Thread> listOfThreads);

        void showErrorMessage();
    }

    public interface DataUtilsWidgetCallback {
        void onSuccess(List<List<Thread>> listOfThreadLists);
    }

    public static void processFetchingThreads(Context context, boolean isWidget, DataUtilsActivityCallback activityCallback, DataUtilsWidgetCallback widgetCallback) {
        mListOfThreadLists = new ArrayList<>();
        mActivityCallback = activityCallback;
        mWidgetCallback = widgetCallback;
        mIsWidget = isWidget;
        try {
            mSubredditCursor = context.getContentResolver().query(URI_SUBREDDITS, null, null, null, null);
            if (mSubredditCursor != null && mSubredditCursor.getCount() != 0) {
                mSubredditCursor.moveToFirst();
                if (!mIsWidget) {
                    mActivityCallback.showRefreshIndicator();
                }
                fetchThreadsAndDisplay(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

    private static void fetchThreadsAndDisplay(final Context context) {

        String url = mSubredditCursor.getString(mSubredditCursor.getColumnIndexOrThrow(ListColumns.URL));

        boolean getNewPosts;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = sharedPreferences.getString(context.getString(R.string.pref_sort_order_key), context.getString(R.string.pref_sort_order_new_value));
        getNewPosts = sortOrder.equals(context.getString(R.string.pref_sort_order_new_value));

        QueryUtils.fetchSubredditThreads(context, new QueryUtils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                ArrayList<Thread> threadsFromSubreddit = JSONUtils.extractThreadsJson(result);
                if (!threadsFromSubreddit.isEmpty()) {
                    mListOfThreadLists.add(threadsFromSubreddit);
                }
                if (!mSubredditCursor.isLast()) {
                    mSubredditCursor.moveToNext();
                    fetchThreadsAndDisplay(context);
                } else {
                    mSubredditCursor.close();
                    ArrayList<Thread> mListOfThreads = convertListOfListsIntoList(mListOfThreadLists);
                    if (!mIsWidget) {
                        mActivityCallback.setupRecyclerView(mListOfThreads);
                    } else {
                        mWidgetCallback.onSuccess(mListOfThreadLists);
                    }
                }

            }

            @Override
            public void onError() {
                if (!mIsWidget) {
                    mActivityCallback.showErrorMessage();
                }
            }
        }, url, getNewPosts);
    }

    private static ArrayList<Thread> convertListOfListsIntoList(List<List<Thread>> listOfLists) {
        ArrayList<Thread> threadsList = new ArrayList<>();

        int maxSize = 1;
        for (int i = 0; i < maxSize; i++) {
            for (List<Thread> list : listOfLists) {
                int currentListSize = list.size();
                if (i < currentListSize) {
                    if (currentListSize > maxSize) {
                        maxSize = currentListSize;
                    }
                    Thread thread = list.get(i);
                    threadsList.add(thread);
                }
            }
        }
        return threadsList;
    }

    public static void updateCallback(DataUtilsActivityCallback callback) {
        mActivityCallback = callback;
    }

}
