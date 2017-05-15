package com.application.zapplon.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.CabDetails;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.TypefaceSpan;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.location.ZLocationCallback;

import java.util.ArrayList;

/**
 * Created by apoorvarora on 28/01/16.
 */
public class CabBookingsPage extends ActionBarActivity implements ZLocationCallback {

    private ZApplication zapp;
    private SharedPreferences prefs;
    private int width, height;

    private AsyncTask mAsyncRunning;
    private Activity mContext;
//    private BookingAdapter mAdapter;
    private LayoutInflater inflater;
    private boolean destroyed = false;

    // Load more part
    private ListView mListView;
    private ArrayList<CabDetails> wishes;

    private ProgressDialog zProgressDialog;

    private SwipeRefreshLayout swipeRefreshLayout;
    private String filterString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cab_bookings_page);
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        zapp = (ZApplication) getApplication();
        inflater = LayoutInflater.from(this);
        prefs = getSharedPreferences("application_settings", 0);

        mContext = this;
        mListView = (ListView) findViewById(R.id.cabs_list);
        mListView.setDivider(null);
        mListView.setDividerHeight(width / 20);
        mListView.setClipToPadding(false);

        setUpActionBar();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CommonLib.ZLog("onRefresh", "called");
                refreshView();
            }

        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        setListeners();

        // start location check
        if(zapp.lat == 0 || zapp.lon == 0) {
            zapp.zll.forced = true;
            zapp.zll.addCallback(this);
            zapp.startLocationCheck();
        } else
            refreshView();
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        zapp.zll.removeCallback(this);
        super.onDestroy();
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetUberCabs().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void setUpActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString(getResources().getString(R.string.cabs_booking));
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
        findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView();
            }
        });
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

    public void actionBarSelected(View v) {

        switch (v.getId()) {

            case R.id.home_icon_container:
                onBackPressed();

            default:
                break;
        }

    }

    @Override
    public void onCoordinatesIdentified(Location loc) {
        if(loc != null && prefs != null) {
            refreshView();
            float lat = (float) loc.getLatitude();
            float lon = (float)loc.getLongitude();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("lat1", lat+"");
            editor.putString("lon1", lon+"");
            editor.commit();

            UploadManager.updateLocation(lat, lon);

        } else {
            findViewById(R.id.content).setVisibility(View.GONE);
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.empty_view_text)).setText("Nothing here yet");
        }
    }

    @Override
    public void onLocationIdentified() {
        CommonLib.ZLog("loc", "here");
    }

    @Override
    public void onLocationNotIdentified() {
        CommonLib.ZLog("loc", "here");
        findViewById(R.id.content).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.empty_view_text)).setText("Nothing here yet");
    }

    @Override
    public void onDifferentCityIdentified() {
        CommonLib.ZLog("loc", "here");
    }

    @Override
    public void locationNotEnabled() {
        CommonLib.ZLog("loc", "here");
        findViewById(R.id.content).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.empty_view_text)).setText("Nothing here yet");
    }

    @Override
    public void onLocationTimedOut() {
        CommonLib.ZLog("loc", "here");
        findViewById(R.id.content).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.empty_view_text)).setText("Nothing here yet");
    }

    @Override
    public void onNetworkError() {
        CommonLib.ZLog("loc", "here");
        findViewById(R.id.content).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.empty_view_text)).setText("Nothing here yet");
    }

    private class GetUberCabs extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progress_container).setVisibility(View.VISIBLE);

            findViewById(R.id.content).setAlpha(1f);

            findViewById(R.id.content).setVisibility(View.GONE);

            findViewById(R.id.empty_view).setVisibility(View.GONE);
            super.onPreExecute();
        }

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "booking/product" + "?latitude=" + zapp.lat + "&longitude=" + zapp.lon;
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.UBER_LIST, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;

            findViewById(R.id.progress_container).setVisibility(View.GONE);

            if (result != null) {
                findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof ArrayList<?>) {
                    setWishes((ArrayList<CabDetails>) result);
                    if (((ArrayList<CabDetails>) result).size() == 0) {
                        findViewById(R.id.content).setVisibility(View.GONE);
                        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.empty_view_text)).setText("Nothing here yet");
                    } else {
                        findViewById(R.id.content).setVisibility(View.VISIBLE);
                        findViewById(R.id.empty_view).setVisibility(View.GONE);
                    }
                }
            } else {
                if (CommonLib.isNetworkAvailable(CabBookingsPage.this)) {
                    Toast.makeText(CabBookingsPage.this, getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CabBookingsPage.this, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                            .show();

                    findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

                    findViewById(R.id.content).setVisibility(View.GONE);
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    // set all the wishes here
    private void setWishes(ArrayList<CabDetails> wishes) {
        this.wishes = wishes;
//        mAdapter = new BookingAdapter(mContext, R.layout.uber_cab_booking_snippet, this.wishes, zapp);
//        mListView.setAdapter(mAdapter);
    }

}
