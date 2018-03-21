package com.example.android.quickreddit.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.quickreddit.R;
import com.example.android.quickreddit.model.Comment;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentAdapterViewHolder> {

    private ArrayList<Comment> mComments;
    private Context mContext;

    public CommentAdapter(Context context) {
        mContext = context;
    }

    @Override
    public CommentAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View createdView = inflater.inflate(R.layout.comment_list_item, parent, false);
        return new CommentAdapterViewHolder(createdView);
    }

    @Override
    public void onBindViewHolder(CommentAdapterViewHolder holder, int position) {
        String body, author, score;
        int depth;
        try {
            Comment comment = mComments.get(position);
            body = comment.getText();
            author = comment.getAuthor();
            depth = comment.getDepth();
            score = String.valueOf(comment.getScore());

            // Padding for replies to comments
            holder.mCommentBodyTextView.setPaddingRelative(depth * 32, 0, 0, 0);
            holder.mAuthorTextView.setPaddingRelative(depth * 32, 0, 0, 0);

            // Corrects the html characters in the text
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                body = Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                body = Html.fromHtml(body).toString();
            }

            holder.mCommentBodyTextView.setText(body);
            holder.mAuthorTextView.setText(author);
            holder.mScoreTextView.setText(score);
        } catch (Exception e) {

            e.printStackTrace();
            FirebaseCrash.report(e);
            holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mComments == null ? 0 : mComments.size();
    }

    class CommentAdapterViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.tv_comment_body)
        TextView mCommentBodyTextView;
        @BindView(R.id.tv_comment_author)
        TextView mAuthorTextView;
        @BindView(R.id.tv_comment_score)
        TextView mScoreTextView;


        CommentAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public void setCommentData(ArrayList<Comment> comments) {
        mComments = comments;
        notifyDataSetChanged();
    }
}
