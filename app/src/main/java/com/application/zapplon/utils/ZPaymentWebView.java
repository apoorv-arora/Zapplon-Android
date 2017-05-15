package com.application.zapplon.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.TaxiBookings;

/**
 * Created by Saurabh on 2/17/2016.
 */
public class ZPaymentWebView extends AppCompatActivity {
    private int width;

    private String mUrl;
    private String mTitle;

    boolean loadingFinished = true;
    boolean redirect = false;
    ZApplication zapp;
    Bundle bundle;
    int position;
    private TaxiBookings taxiBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        bundle = getIntent().getExtras();

        if (bundle != null) {
            if (bundle.get("title") != null)
                mTitle = bundle.getString("title");
            if (bundle.get("url") != null)
                mUrl = bundle.getString("url");
            if (bundle.get("position") != null && bundle.get("position") instanceof Integer)
                position = bundle.getInt("position");
            if (bundle.get("taxiBookings") != null)
                taxiBookings = (TaxiBookings) bundle.getSerializable("taxiBookings");
        }

        zapp = (ZApplication) getApplication();
        setUpActionBar();
        findViewById(R.id.loader).setVisibility(View.VISIBLE);
        findViewById(R.id.webView).setVisibility(View.GONE);

        final WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch
        webView.getSettings().setBuiltInZoomControls(true);

        CommonLib.ZLog("DBug", "webview url " + mUrl);
        webView.loadUrl(mUrl);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingFinished = false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!loadingFinished) {
                    redirect = true;
                }
                loadingFinished = false;
                if (url.contains("zapplon")) {
                    if (url.contains("confirm_payment")) {
                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("booking", taxiBookings);
                        try {
                            long status = Long.parseLong(url.substring(url.lastIndexOf("status=") + 7));
                            bundle.putLong("intercityBookingStatus", status);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        resultIntent.putExtras(bundle);
                        setResult(CommonLib.PAYMENT_REQUEST_CODE_THIRD_PARTY_COLLECTOR, resultIntent);
                        finish();
                    } else {
                        view.loadUrl(url);
                    }
                } else {
                    view.loadUrl(url);
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!redirect) {
                    loadingFinished = true;
                }

                if (loadingFinished && !redirect) {
                    findViewById(R.id.loader).setVisibility(View.GONE);
                    findViewById(R.id.webView).setVisibility(View.VISIBLE);

                } else {
                    redirect = false;
                }
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                // handler.proceed("staging", "phaughoXii5ayu");
            }
        });
    }

    private void setUpActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString(mTitle);
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
        if (!isAndroidL)
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

        actionBar.setTitle(s);
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        try {
            CommonLib.hideKeyBoard(ZPaymentWebView.this, findViewById(R.id.webView));
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onBackPressed();
    }
}