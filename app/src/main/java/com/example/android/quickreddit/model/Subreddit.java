package com.example.android.quickreddit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Subreddit implements Parcelable {
    private String mTitle, mUrl, mId;

    public Subreddit(String title, String url) {
        mTitle = title;
        mUrl = url;
    }

    public Subreddit(String mTitle, String mUrl, String mId) {
        this.mTitle = mTitle;
        this.mUrl = mUrl;
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mUrl);
        dest.writeString(this.mId);
    }

    protected Subreddit(Parcel in) {
        this.mTitle = in.readString();
        this.mUrl = in.readString();
        this.mId = in.readString();
    }

    public static final Parcelable.Creator<Subreddit> CREATOR = new Parcelable.Creator<Subreddit>() {
        @Override
        public Subreddit createFromParcel(Parcel source) {
            return new Subreddit(source);
        }

        @Override
        public Subreddit[] newArray(int size) {
            return new Subreddit[size];
        }
    };
}
