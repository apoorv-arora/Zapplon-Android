package com.application.zapplon.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.adapter.PlaceAutocompleteAdapter;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.CustomAutoCompleteTextView;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddHomeLocation extends AppCompatActivity implements UploadManagerCallback, ZLocationCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    String locationType;
    TextView myLocation;
    private ZApplication zapp;
    private GoogleApiClient mGoogleApiClient;
    CustomAutoCompleteTextView queryBox;
    AlertDialog.Builder dialog;
    private GoogleApiClient googleApiClient;
    LayoutInflater inflater;

    ProgressDialog progressDialog;
    ProgressBar progressBar;
    private SharedPreferences prefs;
    //tracks whether the activity has been started for result
    boolean isForResult, isDestroyed;

    PlaceAutocompleteAdapter mAdapter;
    int addressType;
    ProgressDialog zProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_home_location);
        myLocation = (TextView) findViewById(R.id.my_location);
        zapp = (ZApplication) getApplication();
        queryBox = (CustomAutoCompleteTextView) findViewById(R.id.query_string);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        prefs = getSharedPreferences("application_settings", 0);

        inflater = LayoutInflater.from(this);
        locationType= "";
        if(getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("addressType")) {
            addressType = getIntent().getIntExtra("addressType", CommonLib.ADDRESS_TYPE_HOME);
            locationType = getIntent().getStringExtra("type");
        }

        // start location check
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zapp.zll.forced = true;
                zapp.zll.addCallback(AddHomeLocation.this);
                zapp.startLocationCheck();
                if (!isDestroyed)
                    progressDialog = ProgressDialog.show(AddHomeLocation.this, null, "Fetching Location. Please wait..");
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

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, new LatLngBounds(new LatLng(8.074444444444444,68.13138888888888), new LatLng(37.29805555555556, 97.41305555555556)), new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build());
        queryBox.setAdapter(mAdapter);

        setupActionBar();

        queryBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (mAdapter != null) {
                        final Intent intent = new Intent();
                        final AutocompletePrediction item = mAdapter.getItem(position);
                        final String location = (String) item.getFullText(null);
                        intent.putExtra("location", location);
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, item.getPlaceId())
                                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                    @Override
                                    public void onResult(PlaceBuffer places) {
                                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                            final Place myPlace = places.get(0);
                                            //place found
                                            UploadManager.addAddress(addressType, myPlace.getLatLng().latitude, myPlace.getLatLng().longitude, location);
                                            zProgressDialog = ProgressDialog.show(AddHomeLocation.this, null, "Adding location. Please wait!!!");
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
        UploadManager.addCallback(this);
    }

    @Override
    public void onDestroy() {
        zapp.zll.removeCallback(this);
        UploadManager.removeCallback(this);
        isDestroyed = true;
        if(zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        CommonLib.hideKeyBoard(AddHomeLocation.this, queryBox);
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

        SpannableString s;
        if(addressType == CommonLib.ADDRESS_TYPE_HOME) {
            s = new SpannableString(getResources().getString(R.string.add_home_location));
        } else if(addressType == CommonLib.ADDRESS_TYPE_WORK) {
            s = new SpannableString(getResources().getString(R.string.add_work_location));
        } else
            s = new SpannableString(getResources().getString(R.string.location));
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
        try {
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if(addresses != null && addresses.size() > 0){    //#issue 66 fixed
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                address = address + ", " + city + ", " + state;
                queryBox.setText(address);
                if (!isForResult) {
//                zapp.setLocationString(city);
//                zapp.setAddressString(address);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("location", address);
                    intent.putExtra("lat", loc.getLatitude());
                    intent.putExtra("longitude", loc.getLongitude());
                    setResult(RESULT_OK, intent);
                }
                UploadManager.addAddress(addressType, loc.getLatitude(), loc.getLongitude(), address);
                if( !isDestroyed )
                    zProgressDialog = ProgressDialog.show(AddHomeLocation.this, null, "Adding location. Please wait!!!");
            }else
                Toast.makeText(AddHomeLocation.this,"Some error occured",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
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
            googleApiClient = new GoogleApiClient.Builder(AddHomeLocation.this)
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
                                progressDialog = ProgressDialog.show(AddHomeLocation.this, null, "Fetching Location. Please wait..");
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        AddHomeLocation.this, 1000);
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
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        zapp.startLocationCheck();
                        if (!isDestroyed)
                            progressDialog = ProgressDialog.show(AddHomeLocation.this, null, "Fetching Location. Please wait..");

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
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
        CommonLib.ZLog("AddHomeLocation", "OnConnectionFailed");
        /*if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        CommonLib.ZLog("AddHomeLocation", "onConnectionSuspended");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CommonLib.hideKeyBoard(AddHomeLocation.this, queryBox);
                finish();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonLib.showSoftKeyboard(AddHomeLocation.this, queryBox);
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.ADD_ADDRESS) {
            if(zProgressDialog != null && zProgressDialog.isShowing())
                zProgressDialog.dismiss();
            if(!isDestroyed && status && data instanceof Object[]) {
                Intent intent = new Intent();
                intent.putExtra("location", (String) ((Object[])data)[0]);
                intent.putExtra("lat", (double) ((Object[])data)[1]);
                intent.putExtra("longitude", (double) ((Object[])data)[2]);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}