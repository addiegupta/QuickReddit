package com.example.android.quickreddit.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.quickreddit.R;
import com.example.android.quickreddit.model.Thread;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ThreadAdapterViewHolder> {

    private ArrayList<Thread> mThreadsList;
    private Context mContext;
    private ThreadOnClickHandler mClickHandler;

    public ThreadAdapter(Context context, ThreadOnClickHandler handler) {
        mContext = context;
        mClickHandler = handler;
    }

    @Override
    public ThreadAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View createdView = inflater.inflate(R.layout.thread_list_item, parent, false);
        return new ThreadAdapterViewHolder(createdView);
    }

    @Override
    public void onBindViewHolder(ThreadAdapterViewHolder holder, int position) {
        String title, subredditTitle, thumbnailUrl, score, imageUrl;
        try {
            Thread currentThread = mThreadsList.get(position);
            title = currentThread.getTitle();
            subredditTitle = currentThread.getSubredditTitle();
            imageUrl = currentThread.getImageUrl();

            // Using thumbnailUrl only if image is not available
            if (imageUrl.isEmpty()) {
                thumbnailUrl = currentThread.getThumbnailUrl();

            } else thumbnailUrl = imageUrl;

            score = String.valueOf(currentThread.getScore());

            holder.mTitleTextView.setText(title);
            holder.mSubredditTitleTextView.setText(subredditTitle);
            holder.mScoreTextView.setText(score);
            if (!thumbnailUrl.isEmpty()) {

                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.reddit_placeholder)
                        .error(R.drawable.reddit_placeholder);

                Glide.with(mContext)
                        .load(thumbnailUrl)
                        .apply(options)
                        .thumbnail(0.1f)
                        .into(holder.mThumbnailImageView);

            } else holder.mThumbnailImageView.setImageResource(R.drawable.reddit_placeholder);

            ViewCompat.setTransitionName(holder.mThumbnailImageView, title);
        } catch (Exception e) {

            e.printStackTrace();
            FirebaseCrash.report(e);
            holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mThreadsList == null ? 0 : mThreadsList.size();
    }

    public interface ThreadOnClickHandler {
        void onClick(Thread selectedThread, ImageView imageView);
    }

    class ThreadAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        @BindView(R.id.tv_thread_subreddit_title)
        TextView mSubredditTitleTextView;
        @BindView(R.id.tv_thread_title)
        TextView mTitleTextView;
        @BindView(R.id.tv_thread_score)
        TextView mScoreTextView;
        @BindView(R.id.iv_thread_thumbnail)
        ImageView mThumbnailImageView;


        ThreadAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            Thread thread = mThreadsList.get(adapterPosition);
            mClickHandler.onClick(thread, mThumbnailImageView);
        }

    }

    public void setListData(ArrayList<Thread> threadsList) {
        mThreadsList = threadsList;
        notifyDataSetChanged();
    }
}
