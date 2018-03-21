package com.example.android.quickreddit.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public final class QueryUtils {

    private static String JSON_SOURCE_URL = "https://www.reddit.com/subreddits/popular.json";
    private static String SEARCH_QUERY_URL = "https://www.reddit.com/subreddits/search.json?q=";
    private static String REDDIT_PARENT_URL = "https://www.reddit.com";
    private static String SUBREDDIT_SORT_NEW = "new.json";
    private static String SUBREDDIT_SORT_HOT = "hot.json";
    private static String JSON = ".json";


    public static void fetchTrendingSubreddits(Context context, final VolleyCallback callback) {
        getVolleyResults(context, callback, JSON_SOURCE_URL);
    }

    public static void fetchSearchResults(Context context, final VolleyCallback callback, String queryParameter) {
        getVolleyResults(context, callback, SEARCH_QUERY_URL + queryParameter);
    }

    private static void getVolleyResults(Context context, final VolleyCallback callback, String volleyUrl) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

// Request string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, volleyUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    static void fetchSubredditThreads(Context context, final VolleyCallback callback, String subredditUrl, boolean getNewPosts) {

        String url;
        if (getNewPosts) {
            url = REDDIT_PARENT_URL + subredditUrl + SUBREDDIT_SORT_NEW;
        } else {
            url = REDDIT_PARENT_URL + subredditUrl + SUBREDDIT_SORT_HOT;

        }
        getVolleyResults(context, callback, url);
    }

    public static void fetchThreadComments(Context context, final VolleyCallback callback, String commentsPermalink) {

        String url = REDDIT_PARENT_URL + commentsPermalink + JSON;
        getVolleyResults(context, callback, url);
    }

    public interface VolleyCallback {
        void onSuccess(String result);

        void onError();
    }

    public static boolean isConnected(Context context) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

    }

}
