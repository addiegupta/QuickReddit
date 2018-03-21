package com.example.android.quickreddit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.quickreddit.R;
import com.example.android.quickreddit.adapter.ThreadAdapter;
import com.example.android.quickreddit.model.Thread;
import com.example.android.quickreddit.utils.DataUtils;
import com.example.android.quickreddit.utils.QueryUtils;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.view.View.GONE;
import static com.example.android.quickreddit.ui.SubredditsActivity.URI_SUBREDDITS;


public class MainActivity extends AppCompatActivity implements ThreadAdapter.ThreadOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener, DataUtils.DataUtilsActivityCallback {

    private static final String THREAD_PARCEL_ID = "thread";
    private static final String EXTRA_LIST_OF_THREADS = "threads";
    private static final String EXTRA_IMAGE_TRANSITION = "image_transition";
    private static final String EXTRA_VOLLEY_LOADING_STATE = "volley_loading";
    private static final String EXTRA_VIEW_STATE = "view_state";
    private boolean mIsPhone;

    @BindView(R.id.rv_main_threads)
    RecyclerView mThreadRecyclerView;
    @BindView(R.id.tv_main_connection_error)
    TextView mConnectionErrorTextView;
    @BindView(R.id.tv_main_error)
    TextView mErrorTextView;
    @BindView(R.id.main_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.tv_main_empty_error)
    TextView mEmptySubredditsTextView;

    private ThreadAdapter mThreadAdapter;
    private ArrayList<Thread> mThreadsList;
    private boolean mVolleyIsLoading;

    private enum viewState {
        NONE,
        ERROR,
        NETWORK_ERROR,
        VIEW
    }

    private viewState mViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binds the Views using ButterKnife
        ButterKnife.bind(this);

        // Timber plant for logging
        Timber.plant(new Timber.DebugTree());
        mIsPhone = getResources().getBoolean(R.bool.isPhone);

        if (savedInstanceState == null) {
            launchSubredditsActivityIfDatabaseIsEmpty();
            loadThreads();
        } else {
            mViewState = (viewState) savedInstanceState.getSerializable(EXTRA_VIEW_STATE);
            handleViewState();
            mVolleyIsLoading = savedInstanceState.getBoolean(EXTRA_VOLLEY_LOADING_STATE);
            if (!mVolleyIsLoading) {
                ArrayList<Thread> listOfThreads = savedInstanceState.
                        getParcelableArrayList(EXTRA_LIST_OF_THREADS);
                if (listOfThreads != null && !listOfThreads.isEmpty()) {
                    setupRecyclerView(listOfThreads);
                }
            } else {
                DataUtils.updateCallback(this);
                showRefreshIndicator();
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadThreads();
            }
        });

    }

    private void handleViewState() {
        if (mViewState == viewState.ERROR) {
            showErrorMessage();
        } else if (mViewState == viewState.NETWORK_ERROR) {
            showConnectionErrorMessage();
        } else if (mViewState == viewState.NONE) {
            showEmptySubredditsMessage();
        }
    }

    private void loadThreads() {
        if (mThreadsList != null) {

            mThreadsList.clear();

            mThreadAdapter.setListData(null);
        }
        mEmptySubredditsTextView.setVisibility(GONE);
        Cursor cursor;
        try {
            cursor = getContentResolver().query(URI_SUBREDDITS, null, null, null, null);
            if (cursor != null && cursor.getCount() == 0) {
                showEmptySubredditsMessage();
            } else {

                mSwipeRefreshLayout.setRefreshing(true);
                if (QueryUtils.isConnected(this)) {
                    mVolleyIsLoading = true;
                    DataUtils.processFetchingThreads(this, false, this, null);
                } else {
                    showConnectionErrorMessage();
                }
            }

            if (cursor != null && cursor.isAfterLast()) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.uprootAll();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void launchSubredditsActivityIfDatabaseIsEmpty() {
        Cursor cursor;
        try {
            cursor = getContentResolver().query(URI_SUBREDDITS, null, null, null, null);
            if (cursor != null && cursor.getCount() == 0) {
                startActivity(new Intent(MainActivity.this, SubredditsActivity.class));
            }
            if (cursor != null && cursor.isAfterLast()) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(EXTRA_LIST_OF_THREADS, mThreadsList);
        outState.putBoolean(EXTRA_VOLLEY_LOADING_STATE, mVolleyIsLoading);
        outState.putSerializable(EXTRA_VIEW_STATE, mViewState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuId = item.getItemId();

        switch (menuId) {
            case R.id.menu_subreddits_action:

                //Opens Subreddit chooser activity
                startActivity(new Intent(this, SubredditsActivity.class));
                break;

            case R.id.menu_settings_action:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupRecyclerView(ArrayList<Thread> listOfThreads) {
        mVolleyIsLoading = false;
        mThreadsList = listOfThreads;

        if (!mIsPhone && getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            mThreadRecyclerView.setLayoutManager(new GridLayoutManager(this, 3,
                    GridLayoutManager.VERTICAL, false));
        } else {
            mThreadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }


        mThreadAdapter = new ThreadAdapter(this, this);
        mThreadAdapter.setListData(mThreadsList);

        if (!mThreadsList.isEmpty()) {
            this.mThreadRecyclerView.setAdapter(mThreadAdapter);
        } else {
            this.mThreadRecyclerView.setAdapter(null);
        }
        mViewState = viewState.VIEW;
        mThreadRecyclerView.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(GONE);
        mConnectionErrorTextView.setVisibility(GONE);
        mSwipeRefreshLayout.setRefreshing(false);
        mEmptySubredditsTextView.setVisibility(View.GONE);

    }

    public void showEmptySubredditsMessage() {
        mVolleyIsLoading = false;
        mViewState = viewState.NONE;
        mThreadRecyclerView.setVisibility(GONE);
        mErrorTextView.setVisibility(GONE);
        mConnectionErrorTextView.setVisibility(GONE);
        mSwipeRefreshLayout.setRefreshing(false);
        mEmptySubredditsTextView.setVisibility(View.VISIBLE);
    }

    public void showRefreshIndicator() {
        mVolleyIsLoading = true;
        mViewState = viewState.VIEW;
        mThreadRecyclerView.setVisibility(GONE);
        mErrorTextView.setVisibility(GONE);
        mConnectionErrorTextView.setVisibility(GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mEmptySubredditsTextView.setVisibility(View.GONE);


    }

    public void showErrorMessage() {
        mViewState = viewState.ERROR;
        mVolleyIsLoading = false;
        mThreadRecyclerView.setVisibility(GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mConnectionErrorTextView.setVisibility(GONE);
        mSwipeRefreshLayout.setRefreshing(false);
        mEmptySubredditsTextView.setVisibility(View.GONE);


    }

    public void showConnectionErrorMessage() {
        mViewState = viewState.NETWORK_ERROR;
        mVolleyIsLoading = false;
        mThreadRecyclerView.setVisibility(GONE);
        mErrorTextView.setVisibility(GONE);
        mConnectionErrorTextView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
        mEmptySubredditsTextView.setVisibility(View.GONE);

    }


    @Override
    public void onClick(Thread selectedThread, ImageView imageView) {

        Intent startDetailActivityIntent = new Intent(MainActivity.this, ThreadDetailActivity.class);
        startDetailActivityIntent.putExtra(THREAD_PARCEL_ID, selectedThread);
        String imageTransitionName = ViewCompat.getTransitionName(imageView);
        startDetailActivityIntent.putExtra(EXTRA_IMAGE_TRANSITION, imageTransitionName);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView,
                imageTransitionName);

        startActivity(startDetailActivityIntent, options.toBundle());

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadThreads();
    }

}