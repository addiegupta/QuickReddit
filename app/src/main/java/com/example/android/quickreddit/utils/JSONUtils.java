package com.example.android.quickreddit.utils;


import android.os.Build;
import android.text.Html;

import com.example.android.quickreddit.model.Comment;
import com.example.android.quickreddit.model.Subreddit;
import com.example.android.quickreddit.model.Thread;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class JSONUtils {


    private static final String DATA = "data";
    private static final String KIND = "kind";

    // Value for kind attribute for comments
    private static final String KIND_COMMENT_T1 = "t1";

    private static final String BODY = "body";
    private static final String DEPTH = "depth";
    private static final String AUTHOR = "author";
    private static final String REPLIES = "replies";
    private static final String SELFTEXT_HTML = "selftext_html";


    private static final String CHILDREN = "children";
    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String OVER_18_SUBREDDITS = "over18";
    private static final String OVER_18_THREADS = "over_18";

    private static final String THUMBNAIL = "thumbnail";
    private static final String PREVIEW = "preview";
    private static final String IMAGES = "images";
    private static final String SOURCE = "source";
    private static final String SCORE = "score";
    private static final String THREAD_COMMENTS_PERMALINK = "permalink";
    private static final String THREAD_PARENT_SUBREDDIT = "subreddit";

    private static final String REDDIT_PARENT_URL = "https://www.reddit.com";
    private static final String REDDIT_POST_PATH = "/r/";


    public static ArrayList<Subreddit> extractSubredditsJson(String jsonResponse) {

        ArrayList<Subreddit> listOfTitles = new ArrayList<>();

        try {
            JSONObject parentJson = new JSONObject(jsonResponse);
            JSONObject parentData = parentJson.getJSONObject(DATA);
            JSONArray children = parentData.getJSONArray(CHILDREN);
            for (int i = 0; i < children.length(); i++) {

                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject(DATA);
                String title = childData.getString(TITLE);
                String url = childData.getString(URL);
                // Used to filter out NSFW subreddits
                boolean isNSFW = childData.getBoolean(OVER_18_SUBREDDITS);
                if (!isNSFW) {
                    listOfTitles.add(new Subreddit(title, url));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return listOfTitles;

    }

    static ArrayList<Thread> extractThreadsJson(String jsonResponse) {
        ArrayList<Thread> listOfThreads = new ArrayList<>();

        try {
            JSONObject parentJson = new JSONObject(jsonResponse);
            JSONObject parentData = parentJson.getJSONObject(DATA);
            JSONArray children = parentData.getJSONArray(CHILDREN);
            for (int i = 0; i < children.length(); i++) {

                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject(DATA);
                String title = childData.getString(TITLE);

                title = correctHtmlText(title);

                String subredditTitle = childData.getString(THREAD_PARENT_SUBREDDIT);
                long score = childData.getLong(SCORE);
                String thumbnail = childData.getString(THUMBNAIL);
                String imageUrl = getImageUrl(childData);
                String permalink = childData.getString(THREAD_COMMENTS_PERMALINK);
                String threadUrl = childData.getString(URL);

                String selftextHtml = childData.getString(SELFTEXT_HTML);

                selftextHtml = correctHtmlText(selftextHtml);

                // Fixes incomplete reddit links
                selftextHtml = selftextHtml.replaceAll(REDDIT_POST_PATH, REDDIT_PARENT_URL + REDDIT_POST_PATH);

                // Used to filter out NSFW subreddits
                boolean isNSFW = childData.getBoolean(OVER_18_THREADS);
                if (!isNSFW) {
                    listOfThreads.add(new Thread(title, subredditTitle, thumbnail, imageUrl,
                            threadUrl, permalink, selftextHtml, score));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return listOfThreads;

    }

    private static String getImageUrl(JSONObject data) {
        String imageUrl = "";
        try {
            JSONObject preview = data.getJSONObject(PREVIEW);
            JSONArray images = preview.getJSONArray(IMAGES);
            JSONObject image = images.getJSONObject(0);
            JSONObject source = image.getJSONObject(SOURCE);
            imageUrl = source.getString(URL);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return imageUrl;
    }


    public static ArrayList<Comment> extractThreadComments(String jsonResponse) {

        ArrayList<Comment> listOfComments = new ArrayList<>();

        try {
            JSONArray parentJsonArray = new JSONArray(jsonResponse);
            int arrayLength = parentJsonArray.length();
            for (int i = 0; i < arrayLength; i++) {
                JSONObject parentJson = parentJsonArray.getJSONObject(i);
                JSONObject parentData = parentJson.getJSONObject(DATA);
                JSONArray children = parentData.getJSONArray(CHILDREN);
                int childrenLength = children.length();

                for (int j = 0; j < childrenLength; j++) {

                    JSONObject child = children.getJSONObject(j);
                    if (child.getString(KIND).equals(KIND_COMMENT_T1)) {

                        JSONObject childData = child.getJSONObject(DATA);
                        String body = childData.getString(BODY);
                        int depth = childData.getInt(DEPTH);
                        String author = childData.getString(AUTHOR);
                        int score = childData.getInt(SCORE);

                        if (listOfComments.size() < 20) {

                            listOfComments.add(new Comment(body, author, score, depth));
                            listOfComments = getReplies(childData, listOfComments);
                        }

                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return listOfComments;
    }

    private static ArrayList<Comment> getReplies(JSONObject data,
                                                 ArrayList<Comment> tempListOfComments) {
        try {

            JSONObject replies = data.getJSONObject(REPLIES);
            JSONObject replyData = replies.getJSONObject(DATA);
            JSONArray children = replyData.getJSONArray(CHILDREN);
            int childrenLength = children.length();
            for (int i = 0; i < childrenLength; i++) {
                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject(DATA);

                String body = childData.getString(BODY);
                int depth = childData.getInt(DEPTH);
                String author = childData.getString(AUTHOR);
                int score = childData.getInt(SCORE);

                if (tempListOfComments.size() < 20) {

                    tempListOfComments.add(new Comment(body, author, score, depth));

                    if (childData.getJSONObject(REPLIES).length() != 0) {
                        tempListOfComments = getReplies(childData, tempListOfComments);
                    }
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return tempListOfComments;

    }

    private static String correctHtmlText(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            text = Html.fromHtml(text).toString();
        }
        return text;
    }

}
