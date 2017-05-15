package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.AppConfig;
import com.application.zapplon.data.CabBooking;
import com.application.zapplon.data.CabDetails;
import com.application.zapplon.data.ConnectedAccount;
import com.application.zapplon.data.OfflineAddress;
import com.application.zapplon.data.TaxiBookings;
import com.application.zapplon.db.AddressDBWrapper;
import com.application.zapplon.db.LocationDBWrapper;
import com.application.zapplon.services.AddressService;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.location.ZLocationCallback;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.plus.PlusOneButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Home extends ActionBarActivity implements ZLocationCallback {

    private ZApplication zapp;
    private SharedPreferences prefs;
    private int width;

    LayoutInflater inflater;

    protected boolean destroyed = false;

    // rate us on the play store
    boolean rateDialogShow = false;
    private PlusOneButton mPlusOneButton;
    CabBookingsFragment cabBookingsFragment;
    BillingFragment billingFragment;

    int START_LOCATION_CODE = 10000, DROP_LOCATION_CODE = 10002;
    private static final int REQUEST_INVITE = 1010;

    TextView startLocation, dropLocation;
    LinearLayout startLocationLayout;
    TextView title,all_text,cab_text,auto_text,bike_text,all_icon,cab_icon,auto_icon,bike_icon;
    View view1;
    double latitudeStart, longitudeStart, latitudeEnd, longitudeEnd;
    boolean onHome = false;
    boolean onBookingStatus = false;

    HomeFragment homeFragment;
    BookingStatusFragment bookingStatusFragment;
    IntercityBookingFragment intercityBookingFragment;

    boolean isCabBookingFragmentInitiated =false;
    String showView ="all";

    private ArrayList<CabDetails> wishes;

    private Bitmap mapBitmap;

    public LatLngBounds latLngBounds;

    private Activity mContext;

    private boolean isOfflineVisible = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater = LayoutInflater.from(this);
        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        latLngBounds = new LatLngBounds(new LatLng(8.074444444444444,68.13138888888888), new LatLng(37.29805555555556, 97.41305555555556));

        setContentView(R.layout.activity_home);
        getWindow().setBackgroundDrawable(null);

        LocalBroadcastManager.getInstance(Home.this).registerReceiver(mNotificationReceived, new IntentFilter(CommonLib.LOCAL_CAB_TRACKING_BROADCAST));

        Uri data = getIntent().getData();
        if (data!=null &&data.getHost()!=null) {
            String url = data.toString().trim();
            String host = data.getHost();
            List<String> params = data.getPathSegments();
            if (host.equalsIgnoreCase("www.google.com")) {
                if ( params.size() > 0 && params.get(1).equalsIgnoreCase("place")) {
                    String[] string = url.split("@");
                    String[] ltlng = string[1].split(",");
                    setLatitudeStart(Double.parseDouble(ltlng[0]));
                    setLongitudeStart(Double.parseDouble(ltlng[1]));
                }else
                {
                    latitudeStart = zapp.lat;
                    longitudeStart = zapp.lon;
                }
            } else if (host.equalsIgnoreCase("maps.google.com")) {
                String[] string = url.split(":");
                String[] finalString = string[2].split(",");
                String[] lonString = finalString[1].split(" ");
                setLatitudeStart(Double.parseDouble(finalString[0]));
                setLongitudeStart(Double.parseDouble(lonString[0]));
            } else {
                latitudeStart = zapp.lat;
                longitudeStart = zapp.lon;
            }
        }else
        {
            latitudeStart = zapp.lat;
            longitudeStart = zapp.lon;
        }
        // UI Related stuff
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupActionBar();

        if (Build.VERSION.SDK_INT>=21) {
            getWindow().setSharedElementExitTransition(TransitionInflater.from(Home.this).inflateTransition(R.transition.shared_element_transition));
        }

        zapp.zll.addCallback(Home.this);
        LocationCheck(this);

        rateDialogShow = prefs.getBoolean("rate_dialog_show", true);

        view1 = findViewById(R.id.view1);
        startLocation = (TextView) findViewById(R.id.start_location);
        startLocationLayout = (LinearLayout) findViewById(R.id.start_location_container);
        dropLocation = (TextView) findViewById(R.id.drop_location);
        all_text = (TextView) findViewById(R.id.all_text);
        cab_text = (TextView) findViewById(R.id.car_text);
        auto_text = (TextView) findViewById(R.id.auto_text);
        bike_text = (TextView) findViewById(R.id.bike_text);

        all_icon = (TextView) findViewById(R.id.all_icon);
        cab_icon = (TextView) findViewById(R.id.car_icon);
        auto_icon = (TextView) findViewById(R.id.auto_icon);
        bike_icon = (TextView) findViewById(R.id.bike_icon);
        startLocation.setText(zapp.getAddressString());

        if (getIntent() != null && getIntent().getBooleanExtra("bookingStatus", false) && getIntent().hasExtra("booking")) {
            CabBooking cabBooking = (CabBooking) getIntent().getSerializableExtra("booking");
            goToBookingFragment(cabBooking);
        } else if (getIntent() != null && getIntent().getBooleanExtra("intercityBookingStatus", false) && getIntent().hasExtra("booking")) {
            TaxiBookings cabBooking = (TaxiBookings) getIntent().getSerializableExtra("booking");
            goToIntercityBookingFragment(cabBooking);
        }
        else {
            homeFragment = HomeFragment.newInstance(getIntent().getExtras());
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, homeFragment, "home")
                    .commit();
        }

        appConfig();
        setListeners();

        if(!CommonLib.isNetworkAvailable(this)) {
            new AsyncTask<Object, Void, Object> (){
                @Override
                protected Object doInBackground(Object... params) {
                    return LocationDBWrapper.getAddresses(prefs.getInt("uid", 0));
                }
                @Override
                protected void onPostExecute(Object result) {
                    if(!destroyed && result != null && result instanceof ArrayList<?> && ((ArrayList<?>)result).size() > 0) {
                        Intent offlineDialog = new Intent(Home.this, OfflineDialog.class);
                        mContext.startActivity(offlineDialog);
                    } else {
                        if(!destroyed) {
                            Intent offlineDialog = new Intent(Home.this, OfflineDialog.class);
                            offlineDialog.putExtra("addresses", AddressDBWrapper.getAddresses(prefs.getInt("uid", 0)));
                            mContext.startActivity(offlineDialog);
                        }
                    }
                }
            }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    private void setListeners() {

        getSupportFragmentManager().addOnBackStackChangedListener(getListner());

        startLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startLocationIntent = new Intent(Home.this, SelectCity.class);
                startLocationIntent.putExtra("result", true);
                startLocationIntent.putExtra("bounds",latLngBounds);
                startActivityForResult(startLocationIntent, START_LOCATION_CODE);
            }
        });
        dropLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startLocationIntent = new Intent(Home.this, SelectCity.class);
                startLocationIntent.putExtra("result", true);
                startLocationIntent.putExtra("bounds",latLngBounds);
                startActivityForResult(startLocationIntent, DROP_LOCATION_CODE);
            }
        });

        findViewById(R.id.search_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.search_close_btn).setVisibility(View.GONE);
                if(dropLocation != null) {
                    dropLocation.setText("");
                }
                if(cabBookingsFragment != null) {
                    latitudeEnd = 0;
                    longitudeEnd = 0;
                    cabBookingsFragment.refreshView();
                }
            }
        });

        dropLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s != null) {
                    String str = s.toString();
                    if(str.length() < 1) {
                        findViewById(R.id.search_close_btn).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.search_close_btn).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        findViewById(R.id.all_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                showView = "all";
                if(cabBookingsFragment != null && isCabBookingFragmentInitiated){
                    cabBookingsFragment.returnFilter(showView);
                }
                changeViews(Home.TYPE_ALL);
            }
        });
        findViewById(R.id.car_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showView = "cab";
                if(cabBookingsFragment != null && isCabBookingFragmentInitiated){
                    cabBookingsFragment.returnFilter(showView);
                }
                changeViews(Home.TYPE_CAR);
            }
        });
        findViewById(R.id.auto_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showView = "auto";
                if(cabBookingsFragment != null && isCabBookingFragmentInitiated){
                    cabBookingsFragment.returnFilter(showView);
                }

                changeViews(Home.TYPE_AUTO);
            }
        });
        findViewById(R.id.bike_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showView = "bike";
                if(cabBookingsFragment != null && isCabBookingFragmentInitiated){
                    cabBookingsFragment.returnFilter(showView);
                }
                changeViews(Home.TYPE_BIKE);
            }
        });

        actionBarCustomView.findViewById(R.id.bar_zapps_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this,Redeem.class));
            }
        });
    }

    private android.support.v4.app.FragmentManager.OnBackStackChangedListener getListner() {

        android.support.v4.app.FragmentManager.OnBackStackChangedListener result = new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    int backStackEntryCount = manager.getBackStackEntryCount();
                    if (manager.getFragments() != null && (backStackEntryCount - 1) >= 1 && manager.getFragments().size() > backStackEntryCount - 1) {
                        if (bookingStatusFragment != null) {
                            bookingStatusFragment.onResume();
                            if (actionBarCustomView != null){
                                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
                                if(prefs.getInt("uid",0) > 0){
                                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);
                                }
                            }
                        } else if (intercityBookingFragment != null) {
                            intercityBookingFragment.onResume();
                            if (actionBarCustomView != null){
                                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
                                if(prefs.getInt("uid",0) > 0){
                                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                    else if (manager.getFragments() != null && (backStackEntryCount - 1) >= 0 && manager.getFragments().size() > backStackEntryCount - 1) {
                        if (cabBookingsFragment != null) {
                            cabBookingsFragment.onResume();
                            if (actionBarCustomView != null) {
                                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.VISIBLE);
                                if (prefs.getInt("uid", 0) > 0) {
                                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);
                                }
                            }
                        }
                    } else if (manager.getFragments() != null && backStackEntryCount == 0) {
                        if (homeFragment != null) {
                            homeFragment.onResume();

                            prefs = getSharedPreferences("application_settings", 0);
                            title = (TextView) actionBarCustomView.findViewById(R.id.title);
                            title.setPadding(0, 0, width / 40, 0);

                            String userName = prefs.getString("username", "");

                            if( userName != null && !userName.isEmpty() ) {
                                title.setText(getResources().getString(R.string.going_somewhere_text, userName));
                            } else if (prefs.getString("user_name", "") != null) {
                                userName = prefs.getString("user_name", "");
                                title.setText(getResources().getString(R.string.going_somewhere_text, userName));
                            }

                            findViewById(R.id.bottom_container).setVisibility(View.VISIBLE);
                            findViewById(R.id.location_container).setVisibility(View.VISIBLE);

                            TextView back_icon = (TextView) actionBarCustomView.findViewById(R.id.drawer_left_icon);
                            back_icon.setText(getResources().getString(R.string.z_drawer));

                            if (actionBarCustomView != null) {
                                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
                                actionBarCustomView.findViewById(R.id.title).setVisibility(View.VISIBLE);
                                if(prefs.getInt("uid",0)>0){
                                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            // home fragment does not exist, so recreate it
                            homeFragment = HomeFragment.newInstance(getIntent().getExtras());
                            getFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.fragment_container, homeFragment, "home")
                                    .commit();

                            prefs = getSharedPreferences("application_settings", 0);
                            title = (TextView) actionBarCustomView.findViewById(R.id.title);
                            title.setPadding(0, 0, width / 40, 0);

                            String userName = prefs.getString("username", "");

                            if( userName != null && !userName.isEmpty() ) {
                                title.setText(getResources().getString(R.string.going_somewhere_text, userName));
                            } else if (prefs.getString("user_name", "") != null) {
                                userName = prefs.getString("user_name", "");
                                title.setText(getResources().getString(R.string.going_somewhere_text, userName));
                            }

                            findViewById(R.id.bottom_container).setVisibility(View.VISIBLE);
                            findViewById(R.id.location_container).setVisibility(View.VISIBLE);

                            TextView back_icon = (TextView) actionBarCustomView.findViewById(R.id.drawer_left_icon);
                            back_icon.setText(getResources().getString(R.string.z_drawer));

                            if (actionBarCustomView != null) {
                                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
                                actionBarCustomView.findViewById(R.id.title).setVisibility(View.VISIBLE);
                                if(prefs.getInt("uid",0)>0){
                                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }
            }


        };

        return result;
    }

    private BroadcastReceiver mNotificationReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(getIntent() != null && getIntent().hasExtra("booking_status") && actionBarCustomView != null) {
                int bookingStatus = getIntent().getIntExtra("booking_status", CommonLib.TRACK_STAGE_CLIENT_LOCATED);
                TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);
                title.setVisibility(View.VISIBLE);

                title.setText(getResources().getString(R.string.booking_successful));

                switch (bookingStatus) {
                    case CommonLib.TRACK_STAGE_BOOKING_FAILED:
                        title.setText(getResources().getString(R.string.booking_failed));
                        break;
                    case CommonLib.TRACK_STAGE_BOOKING_PENDING:
                        title.setText(getResources().getString(R.string.booking_pending));
                        break;
                    case CommonLib.TRACK_STAGE_BOOKING_RETRYING:
                        title.setText(getResources().getString(R.string.booking_retrying));
                        break;
                    case CommonLib.TRACK_STAGE_INVOICE:
                        title.setText(getResources().getString(R.string.invoice));
                        break;
                    case CommonLib.TRACK_STAGE_NO_BOOKING:
                        title.setText(getResources().getString(R.string.no_booking));
                        break;
                    case CommonLib.TRACK_STAGE_CALL_DRIVER:
                        title.setText(getResources().getString(R.string.call_driver));
                        break;
                    case CommonLib.TRACK_STAGE_CLIENT_LOCATED:
                        title.setText(getResources().getString(R.string.client_located));
                        break;
                    case CommonLib.TRACK_STAGE_TRIP_START:
                        title.setText(getResources().getString(R.string.trip_start));
                        break;
                    case CommonLib.TRACK_STAGE_TRIP_END:
                        title.setText(getResources().getString(R.string.trip_end));
                        break;
                    case CommonLib.TRACK_STAGE_TRIP_END_CASHBACK:
                        title.setText(getResources().getString(R.string.cancelled));
                        break;
                }
            }
        }
    };

    private class GetPoints extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "user/viewPoints?";
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_POINTS, RequestWrapper.FAV);
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

            if (result != null && result instanceof Integer && ((int)result) != -1) {
                if(actionBarCustomView != null) {
                    actionBarCustomView.findViewById(R.id.zapp_count2).setVisibility(View.VISIBLE);
                    ((TextView) actionBarCustomView.findViewById(R.id.zapp_count2)).setText(String.valueOf(result));
                }
            }
        }
    }



    View actionBarCustomView;

    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBarCustomView = inflater.inflate(R.layout.home_action_bar, null);

        TextView sortItem = (TextView)actionBarCustomView.findViewById(R.id.sorting_type);
        sortItem.setText("TIME");

        actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
        actionBarCustomView.findViewById(R.id.drawer_left_icon).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Fragment currentFragment = getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);

                if (Build.VERSION.SDK_INT >= 21) {
                    actionBarCustomView.findViewById(R.id.drawer_left_icon).setTransitionName("animation");
                }

                TextView back_icon = (TextView) actionBarCustomView.findViewById(R.id.drawer_left_icon);
                if(back_icon.getText().toString().matches(getResources().getString(R.string.z_drawer))){
                    //back_icon.setText("C");
                    if (prefs.getInt("uid", 0) != 0) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(Home.this, actionBarCustomView.findViewById(R.id.drawer_left_icon)
                                    , "animation");
                            Intent userSettingsIntent = new Intent(Home.this, UserSetting.class);
                            if (Build.VERSION.SDK_INT >= 16) {
                                startActivity(userSettingsIntent,optionsCompat.toBundle());
                            }
                        } else {
                            Intent userSettingsIntent = new Intent(Home.this, UserSetting.class);
                            startActivity(userSettingsIntent);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    } else {
                        Toast.makeText(Home.this, "Please login to continue", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Home.this, SplashScreen.class);
                        intent.putExtra("insideApp", true);
                        startActivity(intent);
                    }
                }
                else{
                    onBackPressed();
                    back_icon.setText(getResources().getString(R.string.z_drawer));
                }


            }
        });

        actionBarCustomView.findViewById(R.id.sort_bar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //sort by relevance or price
                View customView = inflater.inflate(R.layout.sort_dialog, null);
                final AlertDialog dialog = new AlertDialog.Builder(Home.this, AlertDialog.THEME_HOLO_LIGHT)
                        .setCancelable(true)
                        .setView(customView)
                        .create();

                customView.findViewById(R.id.amount).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cabBookingsFragment != null) {
                            cabBookingsFragment.sort(CabBookingsFragment.SORT_BY_PRICE);
                            TextView sort_type = (TextView) actionBarCustomView.findViewById(R.id.sorting_type);
                            sort_type.setText("PRICE");
                        }
                        dialog.dismiss();
                    }
                });

                customView.findViewById(R.id.arrival_time).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cabBookingsFragment != null) {
                            cabBookingsFragment.sort(CabBookingsFragment.SORT_BY_ARRIVAL_TIME);
                            TextView sort_type = (TextView) actionBarCustomView.findViewById(R.id.sorting_type);
                            sort_type.setText("TIME");
                        }
                        dialog.dismiss();
                    }
                });

                customView.findViewById(R.id.recommended).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cabBookingsFragment != null) {
                            cabBookingsFragment.sort(CabBookingsFragment.SORT_BY_RECOMMENDED);
                            TextView sort_type = (TextView) actionBarCustomView.findViewById(R.id.sorting_type);
                            sort_type.setText(getResources().getString(R.string.recommended));
                        }
                        dialog.dismiss();
                    }
                });

                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });
        // user handle
        title = (TextView) actionBarCustomView.findViewById(R.id.title);
        title.setPadding(0, 0, width / 40, 0);

        String userName = prefs.getString("username", "");

        if( userName != null && !userName.isEmpty() ) {
            title.setText(getResources().getString(R.string.going_somewhere_text, userName));
        } else if (prefs.getString("user_name", "") != null) {
            userName = prefs.getString("user_name", "");
            title.setText(getResources().getString(R.string.going_somewhere_text, userName));
        }

        actionBar.setCustomView(actionBarCustomView);

        if (prefs.getInt("uid",0) > 0) {
            actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.VISIBLE);
            new GetPoints().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else
            actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);


        new offline().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class GetLocationInfo extends AsyncTask<Object, Void, JSONObject> {

        private double lat;
        private double lon;

        // execute the api
        @Override
        protected JSONObject doInBackground(Object... params) {
            lat = (Double) params[0];
            lon = (Double) params[1];

            HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng="+lat+","+lon+"&sensor=true");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;

        }

        @Override
        protected void onPostExecute(JSONObject ret) {
            if (destroyed)
                return;

            JSONObject location;
            String location_string;
            try {
                location = ret.getJSONArray("results").getJSONObject(0);
                location_string = location.getString("formatted_address");
                zapp.setAddressString(location_string);
            } catch (JSONException e1) {
                e1.printStackTrace();

            }


        }
    }


    public JSONObject getLocationInfo(Double lat, Double lng) {

        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng="+lat+","+lng+"&sensor=true");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void onCoordinatesIdentified(android.location.Location loc) {
        if(loc != null) {
            locationAvailable = true;

            latitudeStart = loc.getLatitude();
            longitudeStart = loc.getLongitude();

            float lat = (float) loc.getLatitude();
            float lon = (float)loc.getLongitude();
            Editor editor = prefs.edit();
            editor.putString("lat1", lat+"");
            editor.putString("lon1", lon+"");
            editor.commit();

            UploadManager.updateLocation(lat, lon);

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if(addresses != null && addresses.size()>0 && addresses.get(0) != null) {
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    address = address + ", " + city + ", " + state + ", " + country;
                    zapp.setLocationString(city);
                    zapp.setCountryString(country);
                    zapp.setAddressString(address);
                } else {
                    /*JSONObject ret = getLocationInfo(loc.getLatitude(),loc.getLongitude());
                    JSONObject location;
                    String location_string;
                    try {
                        location = ret.getJSONArray("results").getJSONObject(0);
                        location_string = location.getString("formatted_address");
                        zapp.setAddressString(location_string);
                    } catch (JSONException e1) {
                        e1.printStackTrace();

                    }*/
                    // network call on main thread
                    new GetLocationInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{loc.getLatitude(),loc.getLongitude()});
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            startLocation.setText(zapp.getAddressString());

            if( locationDisabled && homeFragment != null) {
                try {
                    homeFragment.refreshMap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onLocationIdentified() {

    }

    @Override
    public void onLocationNotIdentified() {

    }

    @Override
    public void onDifferentCityIdentified() {

    }

    @Override
    public void locationNotEnabled() {

    }

    @Override
    public void onLocationTimedOut() {

    }

    @Override
    public void onNetworkError() {

    }

    private void showRateUsDialog() {
        try {
            View customView = inflater.inflate(R.layout.rate_dialog, null);
            mPlusOneButton = (PlusOneButton) customView.findViewById(R.id.plus_one_button);
            mPlusOneButton.initialize("https://market.android.com/details?id=" + getPackageName(), 0);

            final AlertDialog dialog = new AlertDialog.Builder(Home.this, AlertDialog.THEME_HOLO_LIGHT)
                    .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            CommonLib.ZLog("rate dialog", "onCancel");
                            int rateDialogCounterTrigger = prefs.getInt("rate_dialog_trigger", 3);
                            CommonLib.ZLog("rate dialog",
                                    "onCancel() rateDialogCounterTrigger = " + rateDialogCounterTrigger);

                            if (rateDialogCounterTrigger == 3)
                                rateDialogCounterTrigger = 8;
                            else if (rateDialogCounterTrigger == 8)
                                rateDialogCounterTrigger = 13;
                            else if (rateDialogCounterTrigger == 13)
                                rateDialogCounterTrigger = 20;
                            else if (rateDialogCounterTrigger > 13)
                                rateDialogCounterTrigger = rateDialogCounterTrigger + 10;

                            Editor edit = prefs.edit();
                            edit.putInt("rate_dialog_trigger", rateDialogCounterTrigger);
                            edit.commit();

                            CommonLib.ZLog("rate dialog",
                                    "onCancel() rateDialogCounterTrigger is now " + rateDialogCounterTrigger);
                        }
                    })

                    // .setTitle(getResources().getString(R.string.rate_dialog_title))
                    .setView(customView)
                    // .setMessage(getResources().getString(R.string.rate_dialog_message))
                    .create();

            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            customView.findViewById(R.id.rate_dialog_rate_now).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    CommonLib.ZLog("rate dialog", "rate now");

                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + getPackageName()));
                        startActivity(browserIntent);

                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Editor edit = prefs.edit();
                    edit.putBoolean("rate_dialog_show", false);
                    edit.commit();
                    dialog.dismiss();
                }
            });

            customView.findViewById(R.id.rate_dialog_remind).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    int rateDialogCounterTrigger = prefs.getInt("rate_dialog_trigger", 3);
                    CommonLib.ZLog("rate dialog", "remind rateDialogCounterTrigger = " + rateDialogCounterTrigger);

                    if (rateDialogCounterTrigger == 3)
                        rateDialogCounterTrigger = 8;
                    else if (rateDialogCounterTrigger == 8)
                        rateDialogCounterTrigger = 13;
                    else if (rateDialogCounterTrigger == 13)
                        rateDialogCounterTrigger = 20;
                    else if (rateDialogCounterTrigger > 13)
                        rateDialogCounterTrigger = rateDialogCounterTrigger + 10;

                    Editor edit = prefs.edit();
                    edit.putInt("rate_dialog_trigger", rateDialogCounterTrigger);
                    edit.commit();
                    dialog.dismiss();
                    CommonLib.ZLog("rate dialog", "rateDialogCounterTrigger is now at " + rateDialogCounterTrigger);
                }
            });

            customView.findViewById(R.id.rate_dialog_never).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    CommonLib.ZLog("rate dialog", "never");

                    Editor edit = prefs.edit();
                    edit.putBoolean("rate_dialog_show", false);
                    edit.commit();

                    dialog.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appConfig() {
        // run the app config.
        new GetCabSessionObjects().execute(null, null, null);
        new getOfflineAddressAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR); //#bug 188

        new getActiveTripAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        String verified = prefs.getString("verified", "");
        if (verified != null && (verified.equalsIgnoreCase("0") || verified.equalsIgnoreCase(""))) {
            new AppConfigAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        } else {
            //check for app config dialog
            if(prefs.getInt("appConfig_hasChanged", 0) != prefs.getInt("appConfig_hasChanged_new", 0)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("appConfig_dialog", true);
                editor.putInt("appConfig_hasChanged", prefs.getInt("appConfig_hasChanged_new", 0));
                editor.commit();
            }

            if( prefs.getBoolean("appConfig_dialog", true))  {
                final String title = prefs.getString("appConfig_title", null);
                final String imageUrl = prefs.getString("appConfig_imageUrl", null);
                final String description = prefs.getString("appConfig_description", null);
                final String footer = prefs.getString("appConfig_footer", null);
                final boolean finishOnTouchOutside = prefs.getBoolean("appConfig_finishonTouchOutside", true);
                final boolean showAlways = prefs.getBoolean("appConfig_showAlways", false);
                final String buttonString = prefs.getString("buttonString",null);
                final String buttonAction = prefs.getString("buttonAction",null);
                final boolean buttonVisibility = prefs.getBoolean("buttonVisibility",false);

                if(title != null && imageUrl != null && description != null && footer != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(destroyed)
                                return;

                            Intent intent = new Intent(Home.this, AppConfigActivity.class);
                            intent.putExtra("title", title);
                            intent.putExtra("image", imageUrl);
                            intent.putExtra("description", description);
                            intent.putExtra("footer", footer);
                            intent.putExtra("finish_on_touch_outside", finishOnTouchOutside);
                            intent.putExtra("showAlways", showAlways);
                            intent.putExtra("buttonString",buttonString);
                            intent.putExtra("buttonAction",buttonAction);
                            intent.putExtra("buttonVisibility",buttonVisibility);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_bottom, 0);

                            if(!showAlways) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.remove("appConfig_title");
                                editor.remove("appConfig_description");
                                editor.remove("appConfig_imageUrl");
                                editor.remove("appConfig_footer");
                                editor.remove("appConfig_dialog");
                                editor.remove("appConfig_finishonTouchOutside");
                                editor.putBoolean("appConfig_dialog", false);
                                editor.putBoolean("appConfig_showAlways", false);
                                editor.remove("buttonString");
                                editor.remove("buttonAction");
                                editor.remove("buttonVisibility");
                                editor.commit();
                            }
                        }
                    }, 3000);
                }
            }
        }
    }

    private class AppConfigAsync extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {

                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "appConfig/isVerified?";
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.APP_CONFIG, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null) {
                if (result instanceof ArrayList<?>) {
                    ArrayList<AppConfig> appConfigs = (ArrayList<AppConfig>) result;
                    SharedPreferences sharedPreferences = getSharedPreferences("application_settings", 0);
                    Editor editor = sharedPreferences.edit();
                    for (AppConfig appConfig : appConfigs) {

                        if (prefs.getInt("uid", 0) != 0) {
                            if (appConfig.getKey().equalsIgnoreCase("verified")) {
                                if (appConfig.getValue() != null) {
                                    editor.putString("verified", appConfig.getValue());
                                } else
                                    editor.putString("verified", "");
                            }
                            if (appConfig.getKey().equalsIgnoreCase("phone")) {
                                if (appConfig.getValue() != null) {
                                    editor.putString("phone", appConfig.getValue());
                                } else
                                    editor.putString("phone", "");
                            }
                        }
                    }
                    editor.commit();
                }
            }

            if (!destroyed) {
                String verified = prefs.getString("verified", "");
                if (verified != null && verified.equalsIgnoreCase("0")) {
                    //show verification dialog
                    Intent intent = new Intent(Home.this, CheckPhoneVerificationActivity.class);
                    startActivity(intent);
                } //else phone is verified. No need to input.
            }
        }
    }

    private class getActiveTripAsync extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {

                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "appConfig/activeTrip?";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
                nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
                nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));

                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_ACTIVE_TRIP, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null && result instanceof Object[]) {
                Object[] response = (Object[])result;
                if (response[0].equals("success") && response[1] instanceof Object[]) {
                    Object[] bookingResponse = (Object[]) response[1];
                    if(bookingResponse != null && bookingResponse.length > 1) {
                        if(bookingResponse[0] != null) {
                            CabBooking booking = (CabBooking) bookingResponse[0];
                            billingFragment(booking);
                        } else if(bookingResponse[1] != null) {
                            TaxiBookings booking = (TaxiBookings) bookingResponse[1];
                            billingFragment(booking);
                        }
                    }
                }
            }
        }
    }

    private class offline extends AsyncTask<Void, Void, Void> {

        // execute the api
        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<com.application.zapplon.data.Address> addresses = AddressDBWrapper.getAddresses(prefs.getInt("uid", 0));
            ArrayList<com.application.zapplon.data.OfflineAddress> offlineAddresses = LocationDBWrapper.getAddresses(prefs.getInt("uid", 0));
            if((offlineAddresses != null && offlineAddresses.size() >0) || (addresses != null && addresses.size() >0 )){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("offlineVisibility",true);
                editor.commit();
            }
            return null;
        }

    }


    private class getOfflineAddressAsync extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {

            String locationAddress = zapp.getAddressString();
            if(locationAddress != null) {
                boolean shouldAddressDBUpdate = prefs.getBoolean("LOCAL_ADDRESS_UPDATE", false);
                boolean fetch = true;
                if(!shouldAddressDBUpdate) {
                    ArrayList<OfflineAddress> offlineList = LocationDBWrapper.getAddresses(prefs.getInt("uid", 0));
                    if (offlineList != null && offlineList.size() > 0) {
                        OfflineAddress offlineAddress = offlineList.get(0);
                        String city = offlineAddress.getCity();
                        if (locationAddress != null && city != null && !locationAddress.contains(city)) {
                            // gotta fetch em all ~ Pokemon
                        } else
                            fetch = false;
                    }
                }
                return fetch;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null && result instanceof Boolean && (boolean)result) {
                String locationAddress = zapp.getAddressString();
                LocationDBWrapper.removeAddresses();
                Intent intent = new Intent(Home.this, AddressService.class);
                intent.putExtra("address",locationAddress);
                startService(intent);
            }
        }
    }


    private class getDirections extends AsyncTask<Object, Void, Document> {

        // execute the api
        @Override
        protected Document doInBackground(Object... params) {
            String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                    + "origin=" + latitudeStart + "," + longitudeStart
                    + "&destination=" + latitudeEnd + "," + longitudeEnd
                    + "&sensor=false&units=metric&mode=driving";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse response = httpClient.execute(httpPost, localContext);
                InputStream in = response.getEntity().getContent();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document doc = builder.parse(in);
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Document doc) {
            if (doc != null) {
                if(bookingStatusFragment != null && bookingStatusFragment.isAdded()){
                    bookingStatusFragment.refreshMap();
                    bookingStatusFragment.drawPath(doc);
                }
                else if(homeFragment != null && homeFragment.isAdded()){    // added due to crashlytics bug #184
                    homeFragment.refreshMap();
                    homeFragment.drawPath(doc);
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (zapp != null && zapp.getLocationString() == null || zapp.getLocationString().length() == 0) {
            Intent intent = new Intent(Home.this, SelectCity.class);
            startActivityForResult(intent, START_LOCATION_CODE);
        }
    }

    boolean setAddress = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonLib.REQUEST_CODE_OLA_WEB_VIEW) {
            if (cabBookingsFragment != null)
                cabBookingsFragment.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == CommonLib.REQUEST_CODE_UBER_WEB_VIEW) {
            if (cabBookingsFragment != null)
                cabBookingsFragment.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == START_LOCATION_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String location = data.getStringExtra("location");
            latitudeStart = data.getDoubleExtra("lat", -1);
            longitudeStart = data.getDoubleExtra("longitude", -1);
            startLocation.setText(location);
            setAddress = true;
            wishes = null;
            if (cabBookingsFragment != null) {
                cabBookingsFragment.refreshView();
            }
            drawPathOnMaps();
        } else if (requestCode == DROP_LOCATION_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String location = data.getStringExtra("location");
            latitudeEnd = data.getDoubleExtra("lat", -1);
            longitudeEnd = data.getDoubleExtra("longitude", -1);
            setAddress = true;
            dropLocation.setText(location);
            wishes = null;

            drawPathOnMaps();

            if(bookingStatusFragment != null){
                bookingStatusFragment.updateBooking(latitudeEnd,longitudeEnd);
            } else if (cabBookingsFragment != null) {
                cabBookingsFragment.refreshView();
            }
        } else if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                StringBuilder invitationIds = new StringBuilder();
                for (int i = 0; i < ids.length; i++) {
                    invitationIds.append(ids[i]);
                    invitationIds.append(",");
                    CommonLib.ZLog("ids", ids[i]);
                }
                if (invitationIds.length() > 1) {
                    String invites = invitationIds.toString().substring(0, invitationIds.length() - 1);
                    UploadManager.updateInvitationId(invites);
                }
            } else {
                CommonLib.ZLog("ids", "failed");
            }
        } else if (requestCode == 1000) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    zapp.zll.forced = true;
                    zapp.startLocationCheck();
                    locationDisabled = true;
                    break;
                case Activity.RESULT_CANCELED:
                    //LocationCheck(this);
                    locationAvailable = false;
                    Toast.makeText(this, "Please enable location services",
                            Toast.LENGTH_LONG).show();//keep asking if imp or do whatever
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void drawPathOnMaps(){
        if(startLocation != null && dropLocation != null){
            new getDirections().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            TextView back_icon = (TextView) actionBarCustomView.findViewById(R.id.drawer_left_icon);
            if (back_icon.getText().toString().matches(getResources().getString(R.string.z_left_arrow))){
                back_icon.setText(getResources().getString(R.string.z_drawer));
            }
            if(getFragmentManager().getBackStackEntryCount() == 1  ){
                findViewById(R.id.bottom_container).setVisibility(View.VISIBLE);
                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);

            }
            String userName = prefs.getString("username", "");
            if( userName != null && !userName.isEmpty() ) {
                title.setText(getResources().getString(R.string.going_somewhere_text, userName));
            } else if (prefs.getString("user_name", "") != null) {
                userName = prefs.getString("user_name", "");
                title.setText(getResources().getString(R.string.going_somewhere_text, userName));
            }
            if(prefs.getInt("uid",0) > 0){
                findViewById(R.id.bar_zapps_container).setVisibility(View.VISIBLE);
            } else
                findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
            try{
                getFragmentManager().popBackStack();
            }catch(IllegalStateException ignored){

            }

        } else {
            try{
                super.onBackPressed();
            }catch(IllegalStateException ignored){

            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private boolean locationDisabled = false;

    boolean locationAvailable = false;

    public void LocationCheck(final Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        CommonLib.ZLog("a", "All location settings are satisfied.");
                        locationAvailable = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        CommonLib.ZLog("b", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            if(!locationAvailable){
                                status.startResolutionForResult(Home.this, 1000);
                            }
                        } catch (IntentSender.SendIntentException e) {
                            CommonLib.ZLog("c", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        CommonLib.ZLog("d", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        finish();
                        break;
                }
            }
        });
    }


    private class GetCabSessionObjects extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "booking/connectedAccounts?deviceId=" + CommonLib.getIMEI(Home.this);
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.CONNECTED_ACCOUNTS, RequestWrapper.FAV);
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
            if (result != null) {
                if (result instanceof ArrayList<?>) {
                    setConnectedAccounts((ArrayList<ConnectedAccount>) result);
                }
            }
        }
    }

    private void setConnectedAccounts(ArrayList<ConnectedAccount> connectedAccounts) {

        if (connectedAccounts == null)
            return;
        Editor editor = prefs.edit();
        editor.remove("ola_access_token");
        editor.remove("ola_cab_session_id");
        editor.remove("uber_access_token");
        editor.remove("uber_cab_session_id");
        for (ConnectedAccount account : connectedAccounts) {
            switch (account.getCabCompany()) {
                case CommonLib.TYPE_OLA:
                    if (account.getAccessToken() != null && !account.getAccessToken().equals("")) {
                        editor.putString("ola_access_token", account.getAccessToken());
                        editor.putString("ola_cab_session_id", account.getCabSessionId() + "");
                    }
                    break;
                case CommonLib.TYPE_UBER:
                    if (account.getAccessToken() != null && !account.getAccessToken().equals("")) {
                        editor.putString("uber_access_token", account.getAccessToken());
                        editor.putString("uber_cab_session_id", account.getCabSessionId() + "");
                    }
                    break;
            }
        }
        editor.commit();
    }

    public TextView getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(TextView startLocation) {
        this.startLocation = startLocation;
    }

    public TextView getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(TextView dropLocation) {
        this.dropLocation = dropLocation;
    }

    public double getLatitudeStart() {
        return latitudeStart;
    }

    public void setLatitudeStart(double latitudeStart) {
        this.latitudeStart = latitudeStart;
    }

    public double getLongitudeStart() {
        return longitudeStart;
    }

    public void setLongitudeStart(double longitudeStart) {
        this.longitudeStart = longitudeStart;
    }

    public double getLatitudeEnd() {
        return latitudeEnd;
    }

    public void setLatitudeEnd(double latitudeEnd) {
        this.latitudeEnd = latitudeEnd;
    }

    public double getLongitudeEnd() {
        return longitudeEnd;
    }

    public void setLongitudeEnd(double longitudeEnd) {
        this.longitudeEnd = longitudeEnd;
    }

    public String getStartLocationText() {
        return startLocation.getText().toString();
    }

    public String getEndLocationText() {
        return dropLocation.getText().toString();
    }

    protected void nextFragment() {
        if(!destroyed){
            if(actionBarCustomView != null) {
                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.VISIBLE);
                if (prefs.getInt("uid",0) > 0) {
                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);
                }
            }
            onHome = true;
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitudeStart);
            bundle.putDouble("longitude", longitudeStart);
            cabBookingsFragment = CabBookingsFragment.newInstance(bundle);
            android.app.FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            transaction.add(R.id.fragment_container, cabBookingsFragment, "cabs");
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    protected void billingFragment(Object booking) {

        if(!destroyed){
            if(booking == null)
                return;

            if(getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();

            } else if(getSupportFragmentManager().getBackStackEntryCount()>0) {
                getFragmentManager().popBackStack();

            }

            findViewById(R.id.bottom_container).setVisibility(View.GONE);
            findViewById(R.id.location_container).setVisibility(View.GONE);

            TextView back_icon = (TextView) actionBarCustomView.findViewById(R.id.drawer_left_icon);
            back_icon.setText(getResources().getString(R.string.z_drawer));

            if(actionBarCustomView != null && booking instanceof CabBooking) {
                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.VISIBLE);
                if (prefs.getInt("uid",0) > 0) {
                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.VISIBLE);
                    ((TextView)actionBarCustomView.findViewById(R.id.zapp_count2)).setText(((CabBooking)booking).getCashback()+"");
                    ((TextView)actionBarCustomView.findViewById(R.id.zapp)).setText(getResources().getString(R.string.zapps_earned));
                }
            }
            onHome = true;
            Bundle bundle = new Bundle();
            if(booking instanceof CabBooking)
                bundle.putSerializable("intracityBooking", (CabBooking)booking);
            else if(booking instanceof TaxiBookings)
                bundle.putSerializable("intercityBooking", (TaxiBookings)booking);
            billingFragment = BillingFragment.newInstance(bundle);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.add(R.id.fragment_container, billingFragment, "billingFragment");
            transaction.addToBackStack(null);
            //transaction.commit();
            // this method is being called after a asynctask postexecute method. To avoid crash, below line is used. Ref :- http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
            transaction.commitAllowingStateLoss();
        }
    }


    protected void goToBookingFragment(CabBooking booking) {

        if(!destroyed){
            if(booking == null)
                return;

            if(getFragmentManager().getBackStackEntryCount()>0){
                getFragmentManager().popBackStack();

            } else if(getSupportFragmentManager().getBackStackEntryCount()>0) {
                getFragmentManager().popBackStack();

            }

            if(actionBarCustomView != null) {
                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
                if (prefs.getInt("uid", 0) > 0) {
                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);
                }
            }

            findViewById(R.id.bottom_container).setVisibility(View.GONE);
            TextView back_icon = (TextView) actionBarCustomView.findViewById(R.id.drawer_left_icon);
            back_icon.setText(getResources().getString(R.string.z_drawer));

            if(booking.getCreated() > (System.currentTimeMillis() + 60*1000*10)) {
                TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);
                title.setVisibility(View.VISIBLE);

                title.setText(getResources().getString(R.string.booking_successful));

                findViewById(R.id.bottom_container).setVisibility(View.GONE);
            }

            onHome = true;

            Bundle bundle = new Bundle();
            bundle.putSerializable("booking_details", booking);

            bookingStatusFragment = BookingStatusFragment.newInstance(bundle);
            android.app.FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();

            transaction.add(R.id.fragment_container, bookingStatusFragment, "booking");
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        }
    }

    protected void goToIntercityBookingFragment(TaxiBookings booking) {

        if(!destroyed){
            if(booking == null)
                return;

            if(getFragmentManager().getBackStackEntryCount()>0){
                getFragmentManager().popBackStack();

            } else if(getSupportFragmentManager().getBackStackEntryCount()>0) {
                getFragmentManager().popBackStack();

            }

            if(actionBarCustomView != null) {
                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
                if (prefs.getInt("uid", 0) > 0) {
                    actionBarCustomView.findViewById(R.id.bar_zapps_container).setVisibility(View.GONE);
                }
            }

            findViewById(R.id.bottom_container).setVisibility(View.GONE);
            TextView back_icon = (TextView) actionBarCustomView.findViewById(R.id.drawer_left_icon);
            back_icon.setText(getResources().getString(R.string.z_drawer));

            if(booking.getCreated() > (System.currentTimeMillis() + 60*1000*10)) {
                TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);
                title.setVisibility(View.VISIBLE);

                title.setText(getResources().getString(R.string.booking_successful));

                findViewById(R.id.bottom_container).setVisibility(View.GONE);
            }

            onHome = true;

            Bundle bundle = new Bundle();
            bundle.putSerializable("booking_details", booking);

            intercityBookingFragment = IntercityBookingFragment.newInstance(bundle);
            android.app.FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();

            transaction.add(R.id.fragment_container, intercityBookingFragment, "intercityBookingFragment");
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        }
    }


    public ArrayList<CabDetails> getWishes() {
        return wishes;
    }

    public void setWishes(ArrayList<CabDetails> wishes) {
        this.wishes = wishes;
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        zapp.zll.removeCallback(this);
        LocalBroadcastManager.getInstance(Home.this).unregisterReceiver(mNotificationReceived);
        super.onDestroy();
    }


    public static int selectedType = Home.TYPE_CAR;
    public static final int TYPE_ALL = 1;
    public static final int TYPE_CAR = 2;
    public static final int TYPE_AUTO = 3;
    public static final int TYPE_BIKE = 4;

    public void changeViews (int type) {

        if(destroyed)
            return;

        switch(type) {
            case TYPE_ALL:
                findViewById(R.id.all_selector).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.active_color));

                findViewById(R.id.car_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.auto_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.bike_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.bike_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.bike_text)).setTextColor(getResources().getColor(R.color.all_icon_color));
                break;
            case TYPE_CAR:
                findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.car_selector).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.active_color));

                findViewById(R.id.auto_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.bike_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.bike_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.bike_text)).setTextColor(getResources().getColor(R.color.all_icon_color));
                break;
            case TYPE_AUTO:
                findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.car_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.auto_selector).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.active_color));

                findViewById(R.id.bike_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.bike_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.bike_text)).setTextColor(getResources().getColor(R.color.all_icon_color));
                break;
            case TYPE_BIKE:
                findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.car_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.auto_selector).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.bike_selector).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.bike_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)findViewById(R.id.bike_text)).setTextColor(getResources().getColor(R.color.active_color));
                break;

        }
    }

    public boolean hasDestroyed() {
        return destroyed;
    }

    public void setMapBitmap(Bitmap mapBitmap) {
        this.mapBitmap = mapBitmap;
    }

    public Bitmap getMapBitmap() {
        return mapBitmap;
    }
}
