package com.example.android.quickreddit.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.quickreddit.R;
import com.example.android.quickreddit.adapter.SubredditTitleAdapter;
import com.example.android.quickreddit.data.ListColumns;
import com.example.android.quickreddit.model.Subreddit;
import com.example.android.quickreddit.utils.JSONUtils;
import com.example.android.quickreddit.utils.QueryUtils;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static com.example.android.quickreddit.data.SubredditProvider.AUTHORITY;

public class SubredditsActivity extends AppCompatActivity implements
        SubredditTitleAdapter.SubredditOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {


    private static final String KEY_TITLE = "title";
    private static final String KEY_URL = "url";
    public static final Uri URI_SUBREDDITS = Uri.parse("content://" + AUTHORITY + "/subreddits");
    private static final int DB_LOADER_ID = 542;
    private static final String BUNDLE_DISPLAYING_SAVED = "displaying_saved";
    private static final String BUNDLE_SUBREDDITS = "subreddits";
    private static final String BUNDLE_VIEW_STATE = "error_state";
    private static final String BUNDLE_SEARCH_PARAMETER = "search_parameter";
    private static final String BUNDLE_TITLE = "title";
    private boolean displayingSavedSubreddits = true;
    private ArrayList<Subreddit> mSubreddits;
    private String mSearchParameter;
    private String mTitle;

    private enum viewState {
        NETWORK_ERROR,
        ERROR,
        EMPTY,
        NO_ERROR,
        TRENDING,
        SEARCH
    }

    private viewState mViewState = viewState.NO_ERROR;

    @BindView(R.id.subreddit_recycler_view)
    RecyclerView mSubredditRecyclerView;
    @BindView(R.id.subreddits_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.fab_subreddits_trending)
    FloatingActionButton mTrendingFAB;
    @BindView(R.id.tv_subreddits_error)
    TextView mErrorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits);
        ButterKnife.bind(this);

        mTitle = getString(R.string.saved_subreddits);
        setTitle(mTitle);

        if (savedInstanceState == null) {
            setupRecyclerView(null);

            getSupportLoaderManager().initLoader(DB_LOADER_ID, null, this).startLoading();
        } else {
            handleViewsOnRotation(savedInstanceState);
        }
        setupTrendingFAB();
    }

    private void setupTrendingFAB() {

        mTrendingFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTrendingSubreddits();
            }
        });
        mTrendingFAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getBaseContext(), R.string.display_trending_subreddits, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void handleViewsOnRotation(Bundle savedState) {

        mSubreddits = savedState.getParcelableArrayList(BUNDLE_SUBREDDITS);
        displayingSavedSubreddits = savedState.getBoolean(BUNDLE_DISPLAYING_SAVED);
        mViewState = (viewState) savedState.getSerializable(BUNDLE_VIEW_STATE);
        mTitle = savedState.getString(BUNDLE_TITLE);

        setTitle(mTitle);

        if (mViewState == viewState.SEARCH) {

            mSearchParameter = savedState.getString(BUNDLE_SEARCH_PARAMETER);
            fetchSearchResults(mSearchParameter);
        } else if (mViewState == viewState.TRENDING) {
            fetchTrendingSubreddits();
        } else if (mViewState == viewState.NO_ERROR) {
            setupRecyclerView(mSubreddits);
            showRecyclerView();
        } else {
            int stringId;
            if (mViewState == viewState.NETWORK_ERROR)
                stringId = R.string.no_internet_connection;
            else if (mViewState == viewState.EMPTY) {
                stringId = R.string.no_subreddits_saved;
                setTitle(R.string.add_subreddits);
            } else
                stringId = R.string.error_fetching_data;

            mErrorTextView.setText(getString(stringId));
            mErrorTextView.setVisibility(View.VISIBLE);
        }
    }

    private void showSavedSubreddits() {
        mViewState = viewState.NO_ERROR;
        mTitle = getString(R.string.saved_subreddits);
        setTitle(mTitle);
        showRecyclerView();
        displayingSavedSubreddits = true;
        getSupportLoaderManager().restartLoader(DB_LOADER_ID, null, SubredditsActivity.this).startLoading();

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_DISPLAYING_SAVED, displayingSavedSubreddits);
        outState.putParcelableArrayList(BUNDLE_SUBREDDITS, mSubreddits);
        outState.putSerializable(BUNDLE_VIEW_STATE, mViewState);
        outState.putString(BUNDLE_TITLE, mTitle);
        if (mSearchParameter != null) {
            outState.putString(BUNDLE_SEARCH_PARAMETER, mSearchParameter);
        }
    }

    public void fetchSearchResults(String searchParameter) {
        displayingSavedSubreddits = false;
        mViewState = viewState.SEARCH;
        mTitle = searchParameter;
        setTitle(mTitle);
        showLoadingIndicator();
        if (QueryUtils.isConnected(this)) {

            QueryUtils.fetchSearchResults(SubredditsActivity.this, new QueryUtils.VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    if (!displayingSavedSubreddits) {

                        mSubreddits = JSONUtils.extractSubredditsJson(result);
                        if (mSubreddits.isEmpty()) {
                            showErrorMessage();
                        } else {
                            markAndDisplaySavedSubreddits(mSubreddits);

                        }
                    }
                }

                @Override
                public void onError() {
                    showErrorMessage();
                }
            }, searchParameter);
        } else showNetworkErrorMessage();
    }

    public void fetchTrendingSubreddits() {
        displayingSavedSubreddits = false;
        mViewState = viewState.TRENDING;
        mTitle = getString(R.string.trending_subreddits);
        setTitle(mTitle);
        showLoadingIndicator();

        if (QueryUtils.isConnected(this)) {
            QueryUtils.fetchTrendingSubreddits(SubredditsActivity.this, new QueryUtils.VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    if (!displayingSavedSubreddits) {

                        showRecyclerView();
                        mSubreddits = JSONUtils.extractSubredditsJson(result);
                        markAndDisplaySavedSubreddits(mSubreddits);
                    }
                }

                @Override
                public void onError() {
                    showErrorMessage();
                }
            });
        } else showNetworkErrorMessage();
    }

    private void showNetworkErrorMessage() {
        if (mSubreddits != null) {

            mSubreddits.clear();
        }
        mViewState = viewState.NETWORK_ERROR;
        mErrorTextView.setText(R.string.no_internet_connection);
        mErrorTextView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(GONE);
    }

    private void showErrorMessage() {
        if (mSubreddits != null) {
            mSubreddits.clear();

        }
        mViewState = viewState.ERROR;
        mErrorTextView.setText(R.string.error_fetching_data);
        mErrorTextView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(GONE);
    }

    private void setupRecyclerView(ArrayList<Subreddit> listOfTitles) {

        SubredditTitleAdapter mAdapter = new SubredditTitleAdapter(this, this);
        mSubredditRecyclerView.setAdapter(mAdapter);

        mAdapter.setTitleData(listOfTitles);

        mSubredditRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSubredditRecyclerView.setHasFixedSize(true);

    }

    private void markAndDisplaySavedSubreddits(ArrayList<Subreddit> listOfSubreddits) {

        int size = listOfSubreddits.size();
        Subreddit[] params = new Subreddit[size];
        int i = 0;
        for (Subreddit subreddit : listOfSubreddits) {
            params[i] = subreddit;
            i++;
        }
        new MarkSavedTask().execute(params);
    }

    @Override
    public void onClick(Subreddit selectedSubreddit) {
        try {

            if (selectedSubreddit.getId() == null) {

                ContentValues values = new ContentValues();
                values.put(KEY_TITLE, selectedSubreddit.getTitle());
                values.put(KEY_URL, selectedSubreddit.getUrl());
                Uri insertUri = getContentResolver().insert(URI_SUBREDDITS, values);
                if (insertUri != null) {
                    String id = insertUri.getPath();
                    selectedSubreddit.setId(id);
                }
                Toast.makeText(this, R.string.subreddit_saved, Toast.LENGTH_SHORT).show();
            } else {

                URI_SUBREDDITS.buildUpon().appendPath(selectedSubreddit.getId()).build();
                String selection = ListColumns.URL + "=?";
                String[] selectionArgs = {selectedSubreddit.getUrl()};

                getContentResolver().delete(URI_SUBREDDITS, selection, selectionArgs);
                if (displayingSavedSubreddits) {
                    getSupportLoaderManager().restartLoader(DB_LOADER_ID, null, this).startLoading();
                }
                selectedSubreddit.setId(null);
                Toast.makeText(this, R.string.subreddit_removed, Toast.LENGTH_SHORT).show();
            }

        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

    }


    private void showLoadingIndicator() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mSubredditRecyclerView.setVisibility(GONE);
        mErrorTextView.setVisibility(GONE);
    }


    private void showRecyclerView() {
        mViewState = viewState.NO_ERROR;
        mLoadingIndicator.setVisibility(View.GONE);
        mSubredditRecyclerView.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, URI_SUBREDDITS, null, null, null, null);

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            ArrayList<Subreddit> subreddits = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.TITLE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.URL));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns._ID));
                subreddits.add(new Subreddit(title, url, id));
                cursor.moveToNext();
            }
            mSubreddits = subreddits;
            setupRecyclerView(mSubreddits);
        } else {
            if (mSubreddits != null) {
                mSubreddits.clear();

            }
            mSubredditRecyclerView.setAdapter(null);
            mViewState = viewState.EMPTY;
            mErrorTextView.setText(R.string.no_subreddits_saved);
            mErrorTextView.setVisibility(View.VISIBLE);
            setTitle(R.string.add_subreddits);

        }
        if (cursor != null && cursor.isAfterLast()) {
            cursor.close();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onBackPressed() {
        if (!displayingSavedSubreddits) {
            showSavedSubreddits();
        } else {
            super.onBackPressed();
        }

    }

    private class MarkSavedTask extends AsyncTask<Subreddit, Void, ArrayList<Subreddit>> {

        @Override
        protected ArrayList<Subreddit> doInBackground(Subreddit... params) {

            int size = params.length;
            ArrayList<Subreddit> listOfSubreddits = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                listOfSubreddits.add(params[i]);
            }

            for (Subreddit subreddit : listOfSubreddits) {
                String selection = ListColumns.URL + "=?";
                String[] selectionArgs = {subreddit.getUrl()};
                Cursor cursor = getContentResolver().query(URI_SUBREDDITS,
                        null,
                        selection,
                        selectionArgs, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns._ID));
                    subreddit.setId(id);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
            return listOfSubreddits;
        }

        @Override
        protected void onPostExecute(ArrayList<Subreddit> subreddits) {
            super.onPostExecute(subreddits);
            mSubreddits = subreddits;
            setupRecyclerView(mSubreddits);
            showRecyclerView();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subreddits, menu);

        final MenuItem mSearchMenuItem = menu.findItem(R.id.search_subreddits);

        SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(mSearchMenuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {

                mSearchParameter = query;
                fetchSearchResults(query);
                mSearchMenuItem.collapseActionView();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // do your search on change or save the last string in search
                return false;
            }
        });


        return true;
    }
}
