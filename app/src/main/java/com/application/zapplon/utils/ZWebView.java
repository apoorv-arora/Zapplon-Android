package com.application.zapplon.utils;

import org.apache.http.util.EncodingUtils;

import com.application.zapplon.R;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ZWebView extends ActionBarActivity {

	private int width;

	private String mUrl;
	private String mTitle;
	boolean loadingFinished = true;
	boolean redirect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview_layout);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.get("title") != null)
				mTitle = bundle.getString("title");
			if (bundle.get("url") != null)
				mUrl = bundle.getString("url");
		}

		if (mUrl != null && !mUrl.contains("expertise"))
			mUrl = mUrl + "?" + CommonLib.getVersionString(getApplicationContext());
		else if (mUrl != null && mUrl.contains("expertise"))
			mUrl = mUrl + "?src=mob";

		setUpActionBar();
		findViewById(R.id.loader).setVisibility(View.VISIBLE);
		findViewById(R.id.webView).setVisibility(View.GONE);

		WebView webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

		CommonLib.ZLog("DBug", "webview url " + mUrl);
		webView.loadUrl(mUrl);
		String postData = "";
		webView.postUrl(mUrl, EncodingUtils.getBytes(postData, "utf-8"));
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
				return true;
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
}
