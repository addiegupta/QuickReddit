package com.example.android.quickreddit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.android.quickreddit.R;
import com.example.android.quickreddit.model.Thread;
import com.example.android.quickreddit.ui.ThreadDetailActivity;
import com.example.android.quickreddit.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class ThreadsWidget extends AppWidgetProvider {

    private static final String EXTRA_THREADS_ID = "threads";
    private static final String EXTRA_BUNDLE_ID = "bundle";
    private static ArrayList<Thread> mThreads;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = getRemoteView(context,appWidgetId);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews getRemoteView(Context context,int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(context, ListWidgetService.class);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_THREADS_ID, mThreads);
        intent.putExtra(EXTRA_BUNDLE_ID, bundle);
        views.setRemoteAdapter(R.id.widget_list_view, intent);
        views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view_layout);

        Intent appIntent = new Intent(context, ThreadDetailActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_view, appPendingIntent);

        Intent refreshIntent = new Intent(context, ThreadsWidget.class);

        refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
        PendingIntent refreshPendingIntent = PendingIntent
                .getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_empty_view_reload_button, refreshPendingIntent);

        return views;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), ThreadsWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            this.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        DataUtils.processFetchingThreads(context, true, null, new DataUtils.DataUtilsWidgetCallback() {
            @Override
            public void onSuccess(List<List<Thread>> listOfThreadLists) {

                ArrayList<Thread> widgetList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    for (List<Thread> list : listOfThreadLists) {
                        Thread thread = list.get(i);

                        widgetList.add(thread);
                        if (widgetList.size() > 25) {
                            break;
                        }
                    }
                }

                updateAllAppWidgets(context, appWidgetManager, appWidgetIds, widgetList);
            }
        });


        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }


    private static void updateAllAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                            int[] appWidgetIds, ArrayList<Thread> threads) {
        mThreads = threads;
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

}

