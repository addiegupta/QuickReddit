package com.example.android.quickreddit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Thread implements Parcelable {
    private String mTitle, mSubredditTitle,mThumbnailUrl,mImageUrl, mExternalUrl,mPermalink,
            mSelftext;
    private long mScore;

    public Thread(String mTitle, String mSubredditTitle, String mThumbnailUrl, String mImageUrl,
                  String mExternalUrl, String mPermalink, String mSelftext, long mScore) {
        this.mTitle = mTitle;
        this.mSubredditTitle = mSubredditTitle;
        this.mThumbnailUrl = mThumbnailUrl;
        this.mImageUrl = mImageUrl;
        this.mExternalUrl = mExternalUrl;
        this.mPermalink = mPermalink;
        this.mSelftext = mSelftext;
        this.mScore = mScore;
    }

    public String getTitle() {
        return mTitle;
    }
    public String getSubredditTitle(){return mSubredditTitle;}
    public String getThumbnailUrl(){return mThumbnailUrl;}
    public String getImageUrl(){return mImageUrl;}
    public long getScore(){return mScore;}
    public String getExternalUrl() {
        return mExternalUrl;
    }
    public String getPermalink() {
        return mPermalink;
    }
    public String getSelftext(){return mSelftext;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mSubredditTitle);
        dest.writeString(this.mThumbnailUrl);
        dest.writeString(this.mImageUrl);
        dest.writeString(this.mExternalUrl);
        dest.writeString(this.mPermalink);
        dest.writeString(this.mSelftext);
        dest.writeLong(this.mScore);
    }

    protected Thread(Parcel in) {
        this.mTitle = in.readString();
        this.mSubredditTitle = in.readString();
        this.mThumbnailUrl = in.readString();
        this.mImageUrl = in.readString();
        this.mExternalUrl = in.readString();
        this.mPermalink = in.readString();
        this.mSelftext = in.readString();
        this.mScore = in.readLong();
    }

    public static final Creator<Thread> CREATOR = new Creator<Thread>() {
        @Override
        public Thread createFromParcel(Parcel source) {
            return new Thread(source);
        }

        @Override
        public Thread[] newArray(int size) {
            return new Thread[size];
        }
    };
}
