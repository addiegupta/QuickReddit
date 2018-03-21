package com.example.android.quickreddit.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {
    private String mText,mAuthor;
    private int mDepth,mScore;

    public Comment(String mText, String mAuthor, int mScore, int mDepth) {
        this.mText = mText;
        this.mAuthor = mAuthor;
        this.mScore = mScore;
        this.mDepth = mDepth;
    }


    public String getText() {
        return mText;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public int getScore() {
        return mScore;
    }

    public int getDepth() {
        return mDepth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mText);
        dest.writeString(this.mAuthor);
        dest.writeInt(this.mDepth);
        dest.writeInt(this.mScore);
    }

    protected Comment(Parcel in) {
        this.mText = in.readString();
        this.mAuthor = in.readString();
        this.mDepth = in.readInt();
        this.mScore = in.readInt();
    }

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
