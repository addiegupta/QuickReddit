package com.example.android.quickreddit.extra;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewLinkHandler extends WebViewClient {


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url != null) {

            Context context = view.getContext();
            context.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(24)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if (url != null) {
            Context context = view.getContext();
            context.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } else {
            return false;
        }
    }
}
