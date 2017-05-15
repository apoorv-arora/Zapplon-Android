package com.application.zapplon.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.adapter.PlaceAutocompleteAdapter;
import com.application.zapplon.db.AddressDBWrapper;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.CustomAutoCompleteTextView;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.TypefaceSpan;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;
import com.application.zapplon.utils.location.ZLocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectCity extends AppCompatActivity implements UploadManagerCallback, ZLocationCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    TextView myLocation;
    private ZApplication zapp;
    private GoogleApiClient mGoogleApiClient;
    CustomAutoCompleteTextView queryBox;
    AlertDialog.Builder dialog;
    private GoogleApiClient googleApiClient;
    LayoutInflater inflater;

    TextView homeLocation, workLocation;

    ProgressDialog progressDialog;
    ProgressBar progressBar;
    private SharedPreferences prefs;
    //tracks whether the activity has been started for result
    boolean isForResult, isDestroyed;

    PlaceAutocompleteAdapter mAdapter;

    SearchAdapter searchAdapter;

    private AsyncTask mAsyncRunning;

    LatLngBounds latLngBounds;

    com.application.zapplon.data.Address homeAddress, workAddress;

    private ListView mListView;

    private boolean addressExists = false;

    private ArrayList<com.application.zapplon.data.Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        prefs = getSharedPreferences("application_settings", 0);
        inflater = LayoutInflater.from(this);
        zapp = (ZApplication) getApplication();
        queryBox = (CustomAutoCompleteTextView) findViewById(R.id.query_string);
        myLocation = (TextView) findViewById(R.id.my_location);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        homeLocation = (TextView) findViewById(R.id.add_home_text);
        workLocation = (TextView) findViewById(R.id.add_work_text);
        mListView = (ListView) findViewById(R.id.search_list);

        latLngBounds = new LatLngBounds(new LatLng(8.074444444444444, 68.13138888888888), new LatLng(37.29805555555556, 97.41305555555556));

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("result")) {
            isForResult = true;
        }

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("bounds")) {
            latLngBounds = (LatLngBounds) getIntent().getParcelableExtra("bounds");
        }
        // start location check
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zapp.zll.forced = true;
                zapp.zll.addCallback(SelectCity.this);
                zapp.startLocationCheck();
                if (!isDestroyed)
                    progressDialog = ProgressDialog.show(SelectCity.this, null, "Fetching Location. Please wait..");
            }
        });

        findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView();
            }
        });

        findViewById(R.id.search_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryBox.setText("");
            }
        });
        queryBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    handled = true;
                    progressBar.setVisibility(View.VISIBLE);
                }
                return handled;
            }
        });
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
//        queryBox.setAdapter(new SearchresultsAdapter(SelectCity.this));
        if (isForResult) {
            mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, latLngBounds, new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                    .build());

            queryBox.setAdapter(mAdapter);
        } else {
            mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, latLngBounds, new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                    .build());
            queryBox.setAdapter(mAdapter);
        }


        setupActionBar();

        queryBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (!isForResult) {
                        if (parent != null && parent.getAdapter() != null) {
                            final AutocompletePrediction item = (AutocompletePrediction) parent.getAdapter().getItem(position);
                            zapp.setAddressString(item.getFullText(null).toString());
                            zapp.setLocationString(item.getSecondaryText(null).toString().split(",")[0]);
                            final Intent intent = new Intent();
                            intent.putExtra("location", item.getFullText(null));
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, item.getPlaceId())
                                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                        @Override
                                        public void onResult(PlaceBuffer places) {
                                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                                final Place myPlace = places.get(0);
                                                double latitude = myPlace.getLatLng().latitude;
                                                double longitude = myPlace.getLatLng().longitude;

                                                com.application.zapplon.data.Address address = new com.application.zapplon.data.Address();
                                                address.setAddress(item.getFullText(null).toString());
                                                address.setAddressLatitude(latitude);
                                                address.setAddressLongitude(longitude);

                                                AddressDBWrapper.addAddress(address, prefs.getInt("uid", 0), System.currentTimeMillis());

                                                //place found
                                                intent.putExtra("lat", latitude);
                                                intent.putExtra("longitude", longitude);
                                                setResult(RESULT_OK, intent);
                                                finish();


                                            } else {
                                                //place not found
                                            }
                                            places.release();
                                        }
                                    });
                        }
                    } else if (mAdapter != null) {
                        final Intent intent = new Intent();
                        final AutocompletePrediction item = mAdapter.getItem(position);
                        intent.putExtra("location", item.getFullText(null));
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, item.getPlaceId())
                                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                    @Override
                                    public void onResult(PlaceBuffer places) {
                                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                            final Place myPlace = places.get(0);
                                            double latitude = myPlace.getLatLng().latitude;
                                            double longitude = myPlace.getLatLng().longitude;

                                            com.application.zapplon.data.Address address = new com.application.zapplon.data.Address();
                                            address.setAddress(item.getFullText(null).toString());
                                            address.setAddressLatitude(latitude);
                                            address.setAddressLongitude(longitude);

                                            AddressDBWrapper.addAddress(address, prefs.getInt("uid", 0), System.currentTimeMillis());
                                            //place found
                                            intent.putExtra("lat", latitude);
                                            intent.putExtra("longitude", longitude);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        } else {
                                            //place not found
                                        }
                                        places.release();
                                    }
                                });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        homeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefs.getInt("uid", 0) != 0) {
                    // if the location is not set
                    if (homeLocation.getText() != null && homeAddress != null && !homeLocation.getText().toString().equals(getResources().getString(R.string.add_home_location))) {
                        Intent intent = new Intent();

                        intent.putExtra("location", homeAddress.getAddress());
                        intent.putExtra("lat", homeAddress.getAddressLatitude());
                        intent.putExtra("longitude", homeAddress.getAddressLongitude());

                        if(zapp != null && zapp.getLocationString() == null || zapp.getLocationString().length() == 0){

                            zapp.setLocationString(homeAddress.getAddress());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("lat1",homeAddress.getAddressLatitude()+"");
                            editor.putString("lon1",homeAddress.getAddressLongitude()+"");
                            editor.commit();
                        }

                        setResult(RESULT_OK, intent);
                        SelectCity.this.finish();
                    } else {
                        Intent homeLocation = new Intent(SelectCity.this, AddHomeLocation.class);
                        homeLocation.putExtra("type", "home");
                        homeLocation.putExtra("addressType", CommonLib.ADDRESS_TYPE_HOME);
                        startActivityForResult(homeLocation, CommonLib.ADDRESS_TYPE_HOME);
                    }
                } else {
                    Toast.makeText(SelectCity.this, "Please login to continue", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SelectCity.this, SplashScreen.class);
                    intent.putExtra("insideApp", true);
                    startActivity(intent);
                }
            }
        });

        workLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefs.getInt("uid", 0) != 0) {
                    if (workLocation.getText() != null && workAddress != null && !workLocation.getText().toString().equals(getResources().getString(R.string.add_home_location))) {
                        Intent intent = new Intent();
                        intent.putExtra("location", workAddress.getAddress());
                        intent.putExtra("lat", workAddress.getAddressLatitude());
                        intent.putExtra("longitude", workAddress.getAddressLongitude());


                        if(zapp != null && zapp.getLocationString() == null || zapp.getLocationString().length() == 0){

                            zapp.setLocationString(workAddress.getAddress());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("lat1",workAddress.getAddressLatitude()+"");
                            editor.putString("lon1",workAddress.getAddressLongitude()+"");
                            editor.commit();
                        }

                        setResult(RESULT_OK, intent);
                        SelectCity.this.finish();
                    } else {
                        Intent homeLocation = new Intent(SelectCity.this, AddHomeLocation.class);
                        homeLocation.putExtra("type", "work");
                        homeLocation.putExtra("addressType", CommonLib.ADDRESS_TYPE_WORK);
                        startActivityForResult(homeLocation, CommonLib.ADDRESS_TYPE_WORK);
                    }
                } else {
                    Intent intent = new Intent(SelectCity.this, SplashScreen.class);
                    Toast.makeText(SelectCity.this, "Please login to continue", Toast.LENGTH_SHORT).show();
                    intent.putExtra("insideApp", true);
                    startActivity(intent);
                }

            }
        });

        findViewById(R.id.add_home_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadManager.deleteAddress(CommonLib.ADDRESS_TYPE_HOME);
                progressDialog = ProgressDialog.show(SelectCity.this, null, "Removing location. Please wait!!!");
            }
        });

        findViewById(R.id.add_work_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadManager.deleteAddress(CommonLib.ADDRESS_TYPE_WORK);
                progressDialog = ProgressDialog.show(SelectCity.this, null, "Removing location. Please wait!!!");
            }
        });

        //fetch the addresses
        refreshView();
        refreshList();
        UploadManager.addCallback(this);
    }

    private void refreshList(){

        addresses = AddressDBWrapper.getAddresses(prefs.getInt("uid", 0));
        searchAdapter = new SearchAdapter(SelectCity.this, R.layout.recent_search_snippet, addresses);
        mListView.setAdapter(searchAdapter);
        CommonLib.setListViewHeightBasedOnChildren(mListView);
    }

    private class SearchAdapter extends ArrayAdapter<com.application.zapplon.data.Address> {
        private ArrayList<com.application.zapplon.data.Address> wishes;
        private Activity mContext;

        public SearchAdapter(Activity context, int resourceId, ArrayList<com.application.zapplon.data.Address> wishes) {
            super(context.getApplicationContext(), resourceId, wishes);
            mContext = context;
            this.wishes = wishes;
        }

        @Override
        public int getCount() {
            if (wishes == null) {
                return 0;
            } else {
                return wishes.size();
            }
        }

        protected class ViewHolder {
            TextView text;
            LinearLayout layout;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final com.application.zapplon.data.Address address = wishes.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.recent_search_snippet, null);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) convertView.findViewById(R.id.result_string);
                viewHolder.layout = (LinearLayout)convertView.findViewById(R.id.root_container);
                convertView.setTag(viewHolder);
            }

            viewHolder.text.setText(address.getAddress());
            viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (prefs.getInt("uid", 0) != 0) {
                        // if the location is not set
                        Intent intent = new Intent();
                        intent.putExtra("location", address.getAddress());
                        intent.putExtra("lat", address.getAddressLatitude());
                        intent.putExtra("longitude", address.getAddressLongitude());
                        if(zapp != null && zapp.getLocationString() == null || zapp.getLocationString().length() == 0){

                            zapp.setLocationString(address.getAddress());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("lat1",address.getAddressLatitude()+"");
                            editor.putString("lon1",address.getAddressLongitude()+"");
                            editor.commit();
                        }
                        setResult(RESULT_OK, intent);
                        SelectCity.this.finish();
                    } else {
                        Toast.makeText(SelectCity.this, "Please login to continue", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SelectCity.this, SplashScreen.class);
                        intent.putExtra("insideApp", true);
                        startActivity(intent);
                    }
                }
            });

            return convertView;
        }

    }


    @Override
    protected void onPause() {
        try {
            CommonLib.hideKeyBoard(SelectCity.this, queryBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString(getResources().getString(R.string.location));
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
        if (!isAndroidL)
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

        actionBar.setTitle(s);

    }

    @Override
    public void onCoordinatesIdentified(Location loc) {
        Geocoder geocoder;
        List<Address> addresses;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            address = address + ", " + city + ", " + state;
            queryBox.setText(address);
            if (!isForResult) {
                zapp.setLocationString(city);
                zapp.setCountryString(country);
                zapp.setAddressString(address);
            } else {
                Intent intent = new Intent();
                intent.putExtra("location", address);
                intent.putExtra("lat", loc.getLatitude());
                intent.putExtra("longitude", loc.getLongitude());
                setResult(RESULT_OK, intent);
            }
            SelectCity.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationIdentified() {
        CommonLib.ZLog("SelectCity", "OnLocationIdentifeid");
    }

    @Override
    public void onLocationNotIdentified() {
        CommonLib.ZLog("SelectCity", "OnLocationNotIdentifeid");
    }

    @Override
    public void onDifferentCityIdentified() {
        CommonLib.ZLog("SelectCity", "OnDifferentCityIdentified");
    }

    @Override
    public void locationNotEnabled() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(SelectCity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            // in current scenario, user should never come here
                            zapp.startLocationCheck();
                            if (!isDestroyed)
                                progressDialog = ProgressDialog.show(SelectCity.this, null, "Fetching Location. Please wait..");
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        SelectCity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        zapp.startLocationCheck();
                        if (!isDestroyed)
                            progressDialog = ProgressDialog.show(SelectCity.this, null, "Fetching Location. Please wait..");

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
            case CommonLib.ADDRESS_TYPE_HOME:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String location = data.getStringExtra("location");
                        double latitude = data.getDoubleExtra("lat", 0.0);
                        double longitude = data.getDoubleExtra("longitude", 0.0);
                        homeLocation.setText(location);
                        findViewById(R.id.add_home_close).setVisibility(View.VISIBLE);
                        homeAddress = new com.application.zapplon.data.Address();
                        homeAddress.setAddressType(CommonLib.ADDRESS_TYPE_HOME);
                        homeAddress.setAddress(location);
                        homeAddress.setAddressLatitude(latitude);
                        homeAddress.setAddressLongitude(longitude);
                        AddressDBWrapper.addAddress(homeAddress,prefs.getInt("uid", 0),System.currentTimeMillis());
                }
                break;
            case CommonLib.ADDRESS_TYPE_WORK:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String location = data.getStringExtra("location");
                        workLocation.setText(location);
                        double latitude = data.getDoubleExtra("lat", 0.0);
                        double longitude = data.getDoubleExtra("longitude", 0.0);
                        findViewById(R.id.add_work_close).setVisibility(View.VISIBLE);
                        workAddress = new com.application.zapplon.data.Address();
                        workAddress.setAddressType(CommonLib.ADDRESS_TYPE_WORK);
                        workAddress.setAddress(location);
                        workAddress.setAddressLatitude(latitude);
                        workAddress.setAddressLongitude(longitude);
                        AddressDBWrapper.addAddress(workAddress,prefs.getInt("uid", 0),System.currentTimeMillis());
                }
                break;
        }
    }

    @Override
    public void onLocationTimedOut() {
        CommonLib.ZLog("SelectCity", "OnLocationTimedOut");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onNetworkError() {
        CommonLib.ZLog("SelectCity", "OnNetworkError");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        CommonLib.ZLog("SelectCity", "OnConnectionFailed");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        CommonLib.ZLog("SelectCity", "OnConnectionFailed");
        /*if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        CommonLib.ZLog("SelectCity", "onConnectionSuspended");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CommonLib.hideKeyBoard(SelectCity.this, queryBox);
                onBackPressed();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        CommonLib.hideKeyBoard(SelectCity.this, queryBox);
        super.onBackPressed();
    }


    @Override
    protected void onResume() {
        CommonLib.showSoftKeyboard(SelectCity.this, queryBox);
        super.onResume();
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if (requestType == CommonLib.DELETE_ADDRESS) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (status && !isDestroyed) {
                int addressType = CommonLib.ADDRESS_TYPE_HOME;
                try {
                    addressType = Integer.parseInt(String.valueOf(data));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                switch (addressType) {
                    case CommonLib.ADDRESS_TYPE_HOME:
                        homeLocation.setText(getResources().getString(R.string.add_home_location));
                        findViewById(R.id.add_home_close).setVisibility(View.GONE);
                        AddressDBWrapper.deleteAddress(prefs.getInt("uid", 0),CommonLib.ADDRESS_TYPE_HOME);
                        homeAddress = null;
                        break;
                    case CommonLib.ADDRESS_TYPE_WORK:
                        workLocation.setText(getResources().getString(R.string.add_work_location));
                        findViewById(R.id.add_work_close).setVisibility(View.GONE);
                        AddressDBWrapper.deleteAddress(prefs.getInt("uid", 0),CommonLib.ADDRESS_TYPE_WORK);
                        workAddress = null;
                        break;
                }
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }

    @Override
    public void onDestroy() {
        zapp.zll.removeCallback(this);
        UploadManager.removeCallback(this);
        isDestroyed = true;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetAddresses().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    private class GetAddresses extends AsyncTask<Object, Void, Object> {

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
                ArrayList<com.application.zapplon.data.Address> list = AddressDBWrapper.getAllAddresses(prefs.getInt("uid", 0));
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                Object info = null;
                ArrayList<com.application.zapplon.data.Address> result = null;
                int len =0;
                if(list != null && list.size() > 0){
                    for(com.application.zapplon.data.Address address : list){
                        if(address.getAddressType() == CommonLib.ADDRESS_TYPE_WORK ){
                            if(result == null){
                                result = new ArrayList<>();
                            }
                            addressExists = true;
                            result.add(address);
                            len++;
                        }else if(address.getAddressType() == CommonLib.ADDRESS_TYPE_HOME){
                            if(result == null){
                                result = new ArrayList<>();
                            }
                            addressExists = true;
                            result.add(address);
                            len++;
                        }
                    }
                }
                if(len > 0){
                    info = result;
                    return info;
                }else{
                    String url = "";
                    url = CommonLib.SERVER + "address/list?";
                    info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_ADDRESS_LIST, RequestWrapper.FAV);
                    CommonLib.ZLog("url", url);
                    return info;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (isDestroyed)
                return;

            findViewById(R.id.progress_container).setVisibility(View.GONE);
            if (result != null) {
                findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof ArrayList<?>) {
                    prefs.edit().putBoolean("offlineVisibility",true).commit();
                    for (com.application.zapplon.data.Address address : (ArrayList<com.application.zapplon.data.Address>) result) {
                        switch (address.getAddressType()) {
                            case CommonLib.ADDRESS_TYPE_HOME:
                                homeLocation.setText(address.getAddress());
                                findViewById(R.id.add_home_close).setVisibility(View.VISIBLE);
                                homeAddress = address;
                                if(!addressExists)
                                AddressDBWrapper.addAddress(address, prefs.getInt("uid", 0), System.currentTimeMillis());
                                break;
                            case CommonLib.ADDRESS_TYPE_WORK:
                                workLocation.setText(address.getAddress());
                                findViewById(R.id.add_work_close).setVisibility(View.VISIBLE);
                                workAddress = address;
                                if(!addressExists)
                                AddressDBWrapper.addAddress(address, prefs.getInt("uid", 0), System.currentTimeMillis());
                                break;
                        }
                    }

                }
            } else {
                if (CommonLib.isNetworkAvailable(SelectCity.this)) {
                    Toast.makeText(SelectCity.this, getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SelectCity.this, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                            .show();

                    findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

                    findViewById(R.id.content).setVisibility(View.GONE);
                }
            }
        }

    }
}
