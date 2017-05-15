package com.application.zapplon.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.application.zapplon.R;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.ZTracker;
import com.application.zapplon.utils.ZWebView;
import com.google.android.gms.plus.PlusOneButton;

public class 
AboutUs extends ActionBarActivity {

	private int width;
	PlusOneButton mPlusOneButton;

	private int TERMS = 100;
	private int PRIVACY = 101;
	View actionBarCustomView;
	LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		width = getWindowManager().getDefaultDisplay().getWidth();
		inflater = LayoutInflater.from(this);
      	fixsizes();
		setListeners();

		mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
		ImageView img = (ImageView) findViewById(R.id.zapplon_logo);
		img.getLayoutParams().width = width / 3;
		img.getLayoutParams().height = width / 3;
		// setting image
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher, options);
			options.inSampleSize = CommonLib.calculateInSampleSize(options, width, width);
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Config.RGB_565;
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher, options);

			img.setImageBitmap(bitmap);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			img.setBackgroundColor(getResources().getColor(R.color.black));
		} catch (Exception e) {
			e.printStackTrace();
			img.setBackgroundColor(getResources().getColor(R.color.black));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			if (mPlusOneButton != null)
				mPlusOneButton.initialize("http://market.android.com/details?id=" + getPackageName(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void fixsizes() {

		width = getWindowManager().getDefaultDisplay().getWidth();
		findViewById(R.id.home_version).setPadding(width / 20, 0, 0, 0);
		findViewById(R.id.home_logo_container).setPadding(width / 20, width / 20, width / 20, width / 20);
		findViewById(R.id.about_us_body).setPadding(width / 20, 0, width / 20, width / 20);
		((LinearLayout.LayoutParams) findViewById(R.id.plus_one_button).getLayoutParams()).setMargins(width / 20,
				width / 20, width / 20, width / 20);

	}

	public void setListeners() {
		LinearLayout btnTermsAndConditons = (LinearLayout) findViewById(R.id.about_us_terms_conditions_container);
		btnTermsAndConditons.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// tracker.trackPageView("/android/About_TC/"+zapp.city_id);
				// trackerAll.trackPageView("/android/About_TC/"+zapp.city_id);

				ZTracker.logGAEvent(AboutUs.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_TERMS_OF_SERVICE_PRESSED, "");
				Intent intent = new Intent(AboutUs.this, ZWebView.class);
				intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
				intent.putExtra("url", "http://www.zapplon.com/terms/");
				intent.putExtra("terms",TERMS);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			}
		});

		LinearLayout btnPrivacyPolicy = (LinearLayout) findViewById(R.id.about_us_privacy_policy_container);
		btnPrivacyPolicy.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// tracker.trackPageView("/android/About_Privacy/"+zapp.city_id);
				// trackerAll.trackPageView("/android/About_Privacy/"+zapp.city_id);
				ZTracker.logGAEvent(AboutUs.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_PRIVACY_POLICY_PRESSED, "");
				Intent intent = new Intent(AboutUs.this, ZWebView.class);
				intent.putExtra("title", getResources().getString(R.string.about_us_privacypolicy));
				intent.putExtra("url", "http://www.zapplon.com/privacy/");
				intent.putExtra("privacy",PRIVACY);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			}
		});
	}

	public void goBack(View view) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

	public void actionBarSelected(View v) {

		switch (v.getId()) {

		case R.id.home_icon_container:
			onBackPressed();

		default:
			break;
		}

	}

}
