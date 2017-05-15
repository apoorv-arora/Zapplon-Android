package com.application.zapplon.views;

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

/**
 * Created by apoorvarora on 16/03/16.
 */
public class TrackingPage extends ActionBarActivity {

    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_page);
        width = getWindowManager().getDefaultDisplay().getWidth();
        setUpActionBar();
    }

    private void setUpActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString(getResources().getString(R.string.tracking));
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
