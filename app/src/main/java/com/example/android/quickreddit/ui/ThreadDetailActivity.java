package com.example.android.quickreddit.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.android.quickreddit.R;
import com.example.android.quickreddit.adapter.CommentAdapter;
import com.example.android.quickreddit.extra.WebViewLinkHandler;
import com.example.android.quickreddit.model.Comment;
import com.example.android.quickreddit.model.Thread;
import com.example.android.quickreddit.utils.JSONUtils;
import com.example.android.quickreddit.utils.QueryUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class ThreadDetailActivity extends AppCompatActivity {


    private static final String THREAD_PARCEL_ID = "thread";
    private static final String BUNDLE_COMMENTS = "comments";
    private static final String BUNDLE_COMMENTS_STATE = "comments_state";
    private static final String BUNDLE_SCROLL_POSITION = "scroll_position";
    private static final String EXTRA_IMAGE_TRANSITION = "image_transition";


    private ArrayList<Comment> mComments;

    private CommentAdapter mCommentAdapter;

    private static final String REDDIT_PARENT_URL = "https://www.reddit.com";
    private static final String NULL = "null";
    private Thread selectedThread;


    private enum commentsState {
        LOADED,
        LOADING,
        NONE,
        ERROR,
        NETWORK_ERROR
    }

    private commentsState mCommentsState;

    @BindView(R.id.tv_detail_thread_title)
    TextView mThreadTitleTextView;
    @BindView(R.id.tv_thread_selftext)
    WebView mSelftextWebView;
    @BindView(R.id.iv_detail_image_toolbar)
    ImageView mImageView;
    @BindView(R.id.rv_comments)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_thread_rv_placeholder)
    TextView mErrorPlaceholderTextView;
    @BindView(R.id.pb_thread_comments_loading_indicator)
    ProgressBar mCommentsLoadingIndicator;
    @BindView(R.id.thread_nested_scroll_view)
    NestedScrollView mNestedScrollView;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.detail_appbar)
    AppBarLayout mAppBar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.thread_comments_card_view)
    CardView mCommentsCardView;
    @BindView(R.id.thread_selftext_cardview)
    CardView mSelftextCardView;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_detail);
        ButterKnife.bind(this);

        supportPostponeEnterTransition();

        Intent startingIntent = getIntent();
        selectedThread = startingIntent.getParcelableExtra(THREAD_PARCEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            String imageTransitionName = startingIntent.getStringExtra(EXTRA_IMAGE_TRANSITION);
            mImageView.setTransitionName(imageTransitionName);
        }
        loadImageAndTitle();

        manageToolbarAndAppbar();

        if (savedInstanceState == null) {
            fetchComments(selectedThread.getPermalink());
        } else {
            handleCommentsFromSavedState(savedInstanceState);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BUNDLE_COMMENTS, mComments);
        outState.putSerializable(BUNDLE_COMMENTS_STATE, mCommentsState);
        outState.putInt(BUNDLE_SCROLL_POSITION, mNestedScrollView.getScrollY());

    }

    private void manageToolbarAndAppbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbar.setTitle(selectedThread.getSubredditTitle());

    }

    private void loadImageAndTitle() {
        mThreadTitleTextView.setText(selectedThread.getTitle());
        String imageUrl = selectedThread.getImageUrl();
        String thumbnailUrl = selectedThread.getThumbnailUrl();

        if (imageUrl.isEmpty() && !thumbnailUrl.isEmpty()) {
            imageUrl = thumbnailUrl;
        }
        if (!imageUrl.isEmpty()) {

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.reddit_placeholder)
                    .error(R.drawable.reddit_placeholder);

            Glide.with(this)
                    .load(imageUrl)
                    .thumbnail(0.1f)
                    .apply(options)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            mImageView.setImageResource(R.drawable.reddit_placeholder);
                            supportStartPostponedEnterTransition();
                            loadSelftext();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            loadSelftext();
                            return false;
                        }
                    })
                    .into(mImageView);

        } else {
            mImageView.setImageResource(R.drawable.reddit_placeholder);
            supportStartPostponedEnterTransition();
        }
    }

    private void loadSelftext() {


        String selftext = selectedThread.getSelftext();

        if (!selftext.equals(NULL)) {

            mSelftextWebView.loadData(selftext, "text/html; charset=UTF-8", null);

            mSelftextWebView.setWebViewClient(new WebViewLinkHandler());
            mSelftextCardView.setVisibility(View.VISIBLE);
        } else {
            mSelftextCardView.setVisibility(GONE);
        }
    }

    private void handleCommentsFromSavedState(Bundle savedState) {
        mComments = savedState.getParcelableArrayList(BUNDLE_COMMENTS);

        final int scrollPosition = savedState.getInt(BUNDLE_SCROLL_POSITION);
        mAppBar.setExpanded(false);

        mNestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                mNestedScrollView.smoothScrollTo(0, scrollPosition);
            }
        });

        mCommentsState = (commentsState) savedState.getSerializable(BUNDLE_COMMENTS_STATE);
        if (mCommentsState == commentsState.LOADING) {
            showLoadingIndicator();
            fetchComments(selectedThread.getPermalink());
        } else if (mCommentsState == commentsState.LOADED) {
            setupRecyclerView();
        } else if (mCommentsState == commentsState.NONE) {
            showErrorTextView(getString(R.string.no_comments_available));
        } else if (mCommentsState == commentsState.ERROR) {
            showErrorTextView(getString(R.string.error_fetching_data));
        } else if (mCommentsState == commentsState.NETWORK_ERROR) {
            showErrorTextView(getString(R.string.no_internet_connection));

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread_detail, menu);
        if (selectedThread.getExternalUrl().startsWith(REDDIT_PARENT_URL)) {
            menu.findItem(R.id.menu_open_external_link_action).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();

        switch (menuId) {
            case R.id.menu_share_thread_action:

                String shareMessage = getString(R.string.share_message) + selectedThread.getTitle() + "\n " + REDDIT_PARENT_URL + selectedThread.getPermalink();
                Intent shareThreadIntent = new Intent();
                shareThreadIntent.setAction(Intent.ACTION_SEND);
                shareThreadIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                shareThreadIntent.setType("text/plain");
                startActivity(shareThreadIntent);
                break;

            case R.id.menu_open_external_link_action:

                Uri externalUri = Uri.parse(selectedThread.getExternalUrl());
                Intent externalLinkIntent = new Intent(Intent.ACTION_VIEW, externalUri);
                startActivity(externalLinkIntent);
                break;
            case R.id.menu_open_reddit_thread_action:
                Uri threadUri = Uri.parse(REDDIT_PARENT_URL + selectedThread.getPermalink());
                Intent threadIntent = new Intent(Intent.ACTION_VIEW, threadUri);
                startActivity(threadIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchComments(String permalink) {
        if (QueryUtils.isConnected(this)) {
            mCommentsState = commentsState.LOADING;
            showLoadingIndicator();

            QueryUtils.fetchThreadComments(this, new QueryUtils.VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    mComments = JSONUtils.extractThreadComments(result);
                    if (mComments.isEmpty()) {
                        showErrorTextView(getString(R.string.no_comments_available));
                        mCommentsState = commentsState.NONE;
                    } else {
                        mCommentsState = commentsState.LOADED;
                        setupRecyclerView();
                    }
                }

                @Override
                public void onError() {
                    mCommentsState = commentsState.ERROR;
                    showErrorTextView(getString(R.string.error_fetching_data));
                }
            }, permalink);
        } else {
            mCommentsState = commentsState.NETWORK_ERROR;
            showErrorTextView(getString(R.string.no_internet_connection));
        }

    }

    private void setupRecyclerView() {
        mCommentsLoadingIndicator.setVisibility(GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(false);

        mCommentAdapter = new CommentAdapter(this);
        mCommentAdapter.setCommentData(mComments);
        mRecyclerView.setAdapter(mCommentAdapter);
    }

    private void showLoadingIndicator() {
        mCommentsLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(GONE);
        mErrorPlaceholderTextView.setVisibility(GONE);
    }

    private void showErrorTextView(String value) {
        mErrorPlaceholderTextView.setVisibility(View.VISIBLE);
        mErrorPlaceholderTextView.setText(value);
        mRecyclerView.setVisibility(GONE);
        mCommentsLoadingIndicator.setVisibility(GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // To prevent "Unable to create layer for CardView" error
        mCommentsCardView.setVisibility(GONE);
        mSelftextCardView.setVisibility(GONE);
        mThreadTitleTextView.setVisibility(GONE);
    }

}
