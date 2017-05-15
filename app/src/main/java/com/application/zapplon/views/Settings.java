package com.application.zapplon.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;

import com.application.zapplon.R;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.TypefaceSpan;
import com.application.zapplon.utils.ZTracker;

public class Settings extends ActionBarActivity {

	private int width;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		width = getWindowManager().getDefaultDisplay().getWidth();
		setUpActionBar();
		setListeners();
	}

	private void setUpActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		SpannableString s = new SpannableString(getResources().getString(R.string.settings));
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
		if (!isAndroidL)
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

		actionBar.setTitle(s);
	}

	public void setListeners() {
		findViewById(R.id.change_number).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ZTracker.logGAEvent(Settings.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_CHANGE_PHONE_NUMBER_PRESSED, "");
				Intent intent = new Intent(Settings.this, CheckPhoneVerificationActivity.class);
				intent.putExtra("finish_on_touch_outside", true);
				startActivity(intent);
			}
		});

		findViewById(R.id.user_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZTracker.logGAEvent(Settings.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_FEEDBACK_PRESSED, "");
                startActivity(new Intent(Settings.this,FeedbackPage.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

		findViewById(R.id.user_password).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ZTracker.logGAEvent(Settings.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_FEEDBACK_PRESSED, "");
				startActivity(new Intent(Settings.this,PasswordResetActivity.class));
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
		});
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
