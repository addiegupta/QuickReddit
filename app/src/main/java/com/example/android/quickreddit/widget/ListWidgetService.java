package com.example.android.quickreddit.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.quickreddit.R;
import com.example.android.quickreddit.model.Thread;

import java.util.ArrayList;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String EXTRA_BUNDLE_ID = "bundle";
    private static final String EXTRA_SELECTED_THREAD_ID = "thread";
    private static final String EXTRA_THREADS_ID = "threads";

    private ArrayList<Thread> mThreads;
    private Context mContext;


    ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE_ID);
        mThreads = bundle.getParcelableArrayList(EXTRA_THREADS_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mThreads == null ? 0 : mThreads.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {


        RemoteViews parentViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        Thread selectedThread = mThreads.get(position);
        String subredditTitle = selectedThread.getSubredditTitle();
        String threadTitle = selectedThread.getTitle();
        long score = selectedThread.getScore();
        parentViews.setTextViewText(R.id.tv_widget_thread_subreddit_title, subredditTitle);
        parentViews.setTextViewText(R.id.tv_widget_thread_title, threadTitle);
        parentViews.setTextViewText(R.id.tv_widget_thread_score, String.valueOf(score));

        Bundle extras = new Bundle();
        extras.putParcelable(EXTRA_SELECTED_THREAD_ID,selectedThread);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        parentViews.setOnClickFillInIntent(R.id.widget_list_item_layout,fillInIntent);

        return parentViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
