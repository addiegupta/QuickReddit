package com.example.android.quickreddit.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.quickreddit.R;
import com.example.android.quickreddit.model.Subreddit;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SubredditTitleAdapter extends RecyclerView.Adapter<SubredditTitleAdapter.SubredditTitleAdapterViewHolder> {

    private ArrayList<Subreddit> mSubreddits;
    private Context mContext;
    private SubredditOnClickHandler mClickHandler;

    public SubredditTitleAdapter(Context context, SubredditOnClickHandler handler) {
        mContext = context;
        mClickHandler = handler;
    }

    @Override
    public SubredditTitleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View createdView = inflater.inflate(R.layout.subreddit_title_list_item, parent, false);
        return new SubredditTitleAdapterViewHolder(createdView);
    }

    @Override
    public void onBindViewHolder(SubredditTitleAdapterViewHolder holder, int position) {
        Subreddit selectedSubreddit = mSubreddits.get(position);
        String currentTitle = selectedSubreddit.getTitle();
        holder.mTitleTextView.setText(currentTitle);
        if (selectedSubreddit.getId() != null) {
            holder.mSavedIconImageView.setImageResource(R.drawable.ic_star_black_24dp);
        } else holder.mSavedIconImageView.setImageResource(R.drawable.ic_star_border_black_24dp);
    }

    @Override
    public int getItemCount() {
        return mSubreddits == null ? 0 : mSubreddits.size();
    }

    public interface SubredditOnClickHandler {
        void onClick(Subreddit selectedSubreddit);
    }

    class SubredditTitleAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_list_title)
        TextView mTitleTextView;
        @BindView(R.id.iv_subreddit_saved_icon)
        ImageView mSavedIconImageView;

        SubredditTitleAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Subreddit subreddit = mSubreddits.get(adapterPosition);
            invertFavouriteIcon(subreddit);
            mClickHandler.onClick(subreddit);

        }

        void invertFavouriteIcon(Subreddit subreddit) {
            if (subreddit.getId() == null) {
                mSavedIconImageView.setImageResource(R.drawable.ic_star_black_24dp);
            } else mSavedIconImageView.setImageResource(R.drawable.ic_star_border_black_24dp);
        }

    }

    public void setTitleData(ArrayList<Subreddit> subredditData) {
        mSubreddits = subredditData;
        notifyDataSetChanged();
    }

}
