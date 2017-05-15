package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.CabBooking;
import com.application.zapplon.services.TrackingService;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.GMapV2Direction;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;
import com.application.zapplon.utils.location.ZLocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Pratik on 16-May-16.
 */
public class BookingStatusFragment extends Fragment implements OnMapReadyCallback, UploadManagerCallback, ZLocationCallback {

    private ZApplication zapp;
    private SharedPreferences prefs;
    private int width, height;
    private Activity activity;
    private Activity mContext;
    private View getView;

    private boolean destroyed = false;
    private ProgressDialog zProgressDialog;
    private LayoutInflater li;
    LayoutInflater inflater;
    private final int CALL_DIALOG = 1;
    private final int MAKE_CALL_INTENT = 20;

    private CabBooking bookingDetails;
    private GoogleMap googleMap;

    private double savedLat,savedLon;

    private Marker marker;

    private boolean receiveUpdates = false;

    Polyline polylin;
    AlarmManager alarmManager;
    PendingIntent pintent;
    String displayName;

    private AlertDialog dialog;

    public static BookingStatusFragment newInstance(Bundle bundle) {
        BookingStatusFragment fragment = new BookingStatusFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private BroadcastReceiver mNotificationReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!destroyed) {
                try {
                    if (intent != null && intent.hasExtra("booking")) {
                        CabBooking booking = (CabBooking) intent.getSerializableExtra("booking");
                        if (bookingDetails != null && booking != null && bookingDetails.getBookingId() == booking.getBookingId()) {

                            if (booking.getStatus() == CommonLib.TRACK_STAGE_TRIP_END || booking.getStatus() == CommonLib.TRACK_STAGE_TRIP_END_CASHBACK || booking.getStatus() == CommonLib.TRACK_STAGE_BOOKING_CANCELLED) {
                                ((Home) activity).billingFragment(booking);
                            } else {

                                if(booking.getStatus() == CommonLib.TRACK_STAGE_TRIP_START)
                                    receiveUpdates = true;

                                Location oldLocation = new Location("");
                                Location newLocation = new Location("");

                                oldLocation.setLatitude(bookingDetails.getDriverLatitude());
                                oldLocation.setLongitude(bookingDetails.getDriverLongitude());

                                newLocation.setLatitude(booking.getDriverLatitude());
                                newLocation.setLongitude(booking.getDriverLongitude());

                                savedLat = booking.getDriverLatitude();
                                savedLon = booking.getDriverLongitude();

                                LatLng latlng = new LatLng(booking.getDriverLatitude(), booking.getDriverLongitude());
                                float bearing = oldLocation.bearingTo(newLocation);

                                if (marker != null && !receiveUpdates) {
                                    marker.setRotation(bearing);
                                    animateMarker(marker, latlng, googleMap);
                                }

                                bookingDetails = booking;
                                refreshViews();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.booking_fragment, null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                view = inflater.inflate(R.layout.booking_fragment, null);
            } catch (Exception e1) {
                e1.printStackTrace();
                if (view == null)
                    view = inflater.inflate(R.layout.booking_fragment, null);
            }
            return view;
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        getView = getView();
        destroyed = false;
        mContext = activity;
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        inflater = LayoutInflater.from(activity);
        displayName = "";
        alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        ((Home) activity).onBookingStatus = true;

        if (getArguments() != null && getArguments().containsKey("booking_details"))
            bookingDetails = (CabBooking) getArguments().getSerializable("booking_details");

        if(bookingDetails.getDisplayName() != null){
            displayName = bookingDetails.getDisplayName();
        }

        ((ImageView) getView.findViewById(R.id.cab_icon)).setImageBitmap(CommonLib.getBitmap(getActivity(), CommonLib.getBrandBitmap(bookingDetails.getType()), width, width));

        // load data from activity
        try {// Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        UploadManager.addCallback(this);

        if ((bookingDetails.getType()== CommonLib.TYPE_RIDZ || bookingDetails.getType() == CommonLib.TYPE_JUGNOO || bookingDetails.getType() == CommonLib.TYPE_MEGA || bookingDetails.getType() == CommonLib.TYPE_EASY || bookingDetails.getType() == CommonLib.TYPE_OLA) &&

                (bookingDetails.getStatus() != 106 && bookingDetails.getStatus() != 105 && bookingDetails.getStatus() != 104)
                ) {
            getView.findViewById(R.id.cancel_cab).setVisibility(View.VISIBLE);
            getView.findViewById(R.id.cancel_cab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
               /*     final AlertDialog logoutDialog;
                    logoutDialog = new AlertDialog.Builder(activity).setTitle(getResources().getString(R.string.dialog_cancel))
                            .setMessage(getResources().getString(R.string.logout_confirm))
                            .setPositiveButton(getResources().getString(R.string.cancel_cab), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    zProgressDialog = ProgressDialog.show(activity, null, "Cancelling Request. Please wait!!!");
                                    UploadManager.cabCancellationRequest(bookingDetails.getType(), bookingDetails.getAccessToken(), bookingDetails.getCrn(), bookingDetails.getCallId(), bookingDetails.getPickupLatitude(), bookingDetails.getPickupLongitude(), bookingDetails.getBookingId());
                                }
                            }).setNegativeButton(getResources().getString(R.string.nevermind),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .create();
                    logoutDialog.show();*/

                    final View customView = inflater.inflate(R.layout.cancel_dialog, null);
                    dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                            .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {

                                }
                            })
                            .setView(customView)
                            .create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                    final String reason = prefs.getString("cancel_reason",null);

                    final RadioGroup radioGroup = (RadioGroup)customView.findViewById(R.id.radio_group);
                    if(reason != null){
                        String[] reason_arr = reason.split(",");
                        for(int i=0;i<reason_arr.length;i++){
                            RadioButton radioButton = new RadioButton(mContext);
                            radioButton.setText(reason_arr[i]);
                            radioGroup.addView(radioButton);
                        }
                    }


                    customView.findViewById(R.id.radio_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customView.findViewById(R.id.reason_text).setVisibility(View.VISIBLE);
                        }
                    });

                    customView.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int index = radioGroup.indexOfChild(customView.findViewById(radioGroup.getCheckedRadioButtonId()));
                            if(index == -1) {
                                Toast.makeText(mContext,getResources().getString(R.string.pick_reason),Toast.LENGTH_SHORT).show();
                            } else {
                                String cancel_reason = "";
                                if (index != 0) {
                                    cancel_reason = (reason.split(","))[index - 1];
                                } else {
                                    cancel_reason = ((EditText)customView.findViewById(R.id.reason_text)).getText().toString();
                                }
                                zProgressDialog = ProgressDialog.show(activity, null, "Cancelling Request. Please wait!!!");
                                UploadManager.cabCancellationRequest(bookingDetails.getType(), bookingDetails.getAccessToken(), bookingDetails.getCrn(), bookingDetails.getCallId(), bookingDetails.getPickupLatitude(), bookingDetails.getPickupLongitude(), bookingDetails.getBookingId(),cancel_reason, "bsf");
                            }

                        }
                    });

                    customView.findViewById(R.id.abort).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
            });

        } else
            getView.findViewById(R.id.cancel_cab).setVisibility(View.GONE);

        getView.findViewById(R.id.call_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookingDetails != null)
                    callStore(bookingDetails.getDriverNumber());
            }
        });

        getView.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookingDetails != null) {
                    String url = bookingDetails.getShareUrl();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT,url);
                    startActivity(intent);
                }
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNotificationReceived, new IntentFilter(CommonLib.LOCAL_CAB_TRACKING_BROADCAST));

        try {
            if (!zapp.isMyServiceRunning(TrackingService.class)) {
                boolean alarmUp = (PendingIntent.getService(mContext, 0, new Intent(mContext, TrackingService.class), PendingIntent.FLAG_NO_CREATE) != null);

                if (!alarmUp)
                    startTrackingService();
                else{
                    PendingIntent intent = PendingIntent.getService(mContext, 0, new Intent(mContext, TrackingService.class), PendingIntent.FLAG_NO_CREATE);
                    alarmManager.cancel(intent);
                    startTrackingService();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        zapp.zll.addCallback(this);
        zapp.zll.receiveUpdates = true;
    }

    private void startTrackingService() {

        Intent serviceIntent = new Intent(mContext, TrackingService.class);
        serviceIntent.putExtra("hash",bookingDetails.getHash());
        pintent = PendingIntent.getService(mContext, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC, CommonLib.POLLING_TIMER, CommonLib.POLLING_TIMER, pintent);
    }

    public void drawPath(Document doc) {

        if(isAdded()){   // added due to crashlytics bugs #184
            if (polylin != null) {
                polylin.remove();
            }

            GMapV2Direction md = new GMapV2Direction();

            ArrayList<LatLng> directionPoint = md.getDirection(doc);
            PolylineOptions rectLine = new PolylineOptions().width(4).color(
                    mContext.getResources().getColor(R.color.fb_background));

            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            polylin = googleMap.addPolyline(rectLine);
        }
    }


    private void initilizeMap() {
        if (googleMap == null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (!destroyed) {
                        try {
                            ((MapFragment) getFragmentManager().findFragmentById(R.id.map1)).getMapAsync(BookingStatusFragment.this);
                        } catch (Exception e) {
                            // Crashlytics.logException(e);
                            e.printStackTrace();
                        }
                    }
                }
            }, 1000);
        }


    }

    public void updateBooking(double latitide, double longitude) {
        UploadManager.updateBooking(latitide + "", longitude + "", bookingDetails.getHash());
    }

    @Override
    public void onMapReady(final GoogleMap mMap) {
        this.googleMap = mMap;

        if (googleMap != null) {

            // Changing map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Showing / hiding your current location
            googleMap.setMyLocationEnabled(true);

            // Enable / Disable zooming controls
            googleMap.getUiSettings().setZoomControlsEnabled(false);

            // Enable / Disable my location button
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Enable / Disable Compass icon
            googleMap.getUiSettings().setCompassEnabled(false);

            // Enable / Disable Rotate gesture
            googleMap.getUiSettings().setRotateGesturesEnabled(false);

            // Enable / Disable zooming functionality
            googleMap.getUiSettings().setZoomGesturesEnabled(true);


            refreshMap();
            ((Home)activity).drawPathOnMaps();
        }
    }

    protected void refreshMap() {

        if (activity != null && googleMap != null && isAdded()) {
            LatLng location = new LatLng(bookingDetails.getPickupLatitude(),
                    bookingDetails.getPickupLongitude());

            LatLng driverLocation = new LatLng(bookingDetails.getDriverLatitude(), bookingDetails.getDriverLongitude());

            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(location, (float) 10.0));

            marker = googleMap.addMarker((new MarkerOptions()
                    .position(driverLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow_up))
                    .flat(true)));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("prev_lat", String.valueOf(bookingDetails.getDriverLatitude()));
            editor.putString("prev_lon", String.valueOf(bookingDetails.getDriverLongitude()));
            editor.apply();

            refreshViews();
        }
    }

    public void animateMarker(final Marker marker, final LatLng finalPosition, GoogleMap googleMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!destroyed) {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed
                            / duration);
                    double lng = t * finalPosition.longitude + (1 - t)
                            * startLatLng.longitude;
                    double lat = t * finalPosition.latitude + (1 - t)
                            * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));
                    if (t < 1.0) {
                        handler.postDelayed(this, 16);
                    }
                }
            }
        });
    }

    private void refreshViews() {
        if (bookingDetails == null)
            return;

        // Log.d("hello","reached here");

        if (bookingDetails.getType() == CommonLib.TYPE_EASY || (bookingDetails.getType() == CommonLib.TYPE_OLA && bookingDetails.getCabType() != null && bookingDetails.getCabType().equalsIgnoreCase("auto"))) {
            view.findViewById(R.id.call_driver).setVisibility(View.GONE);
        }

        if (bookingDetails.getDisplayName() != null && bookingDetails.getDisplayName().contains("auto"))
            ((TextView) view.findViewById(R.id.display_name)).setText("Ola Auto");
        else
            ((TextView) view.findViewById(R.id.display_name)).setText(displayName);

        ((TextView) view.findViewById(R.id.travel_cost)).setText(prefs.getString("baseFare", ""));

        ((TextView) view.findViewById(R.id.rate_per_KM2)).setText(prefs.getString("ratePerKm", ""));

//        ((ImageView)view.findViewById(R.id.cab_icon)).setImageBitmap(CommonLib.getBitmap(mContext, CommonLib.getBrandBitmap(prefs.getInt("cabType",6)), width, width));

        if (bookingDetails.getCabModel() != null && bookingDetails.getCabModel().length() > 0)
            ((TextView) view.findViewById(R.id.model)).setText(bookingDetails.getCabModel());

        if (bookingDetails.getCabNumber() != null && bookingDetails.getCabNumber().length() > 0)
            ((TextView) view.findViewById(R.id.vehicle_number)).setText(bookingDetails.getCabNumber());

        if (bookingDetails.getDriverName() != null && bookingDetails.getDriverName().length() > 0)
            ((TextView) view.findViewById(R.id.driver_name)).setText(bookingDetails.getDriverName());


        if (bookingDetails.getDriverNumber() == null || bookingDetails.getDriverNumber().length() <= 1)
            view.findViewById(R.id.call_driver).setVisibility(View.GONE);
        else
            view.findViewById(R.id.call_driver).setVisibility(View.VISIBLE);

    }


    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {

        if (requestType == CommonLib.CAB_CANCELLATION_REQUEST && stringId != null && stringId.equalsIgnoreCase("bsf")) {
            if (destroyed)
                return;
            if (zProgressDialog != null && zProgressDialog.isShowing()) {
                zProgressDialog.dismiss();
            }
            if (status) {
                if( dialog != null && dialog.isShowing() )
                    dialog.dismiss();
                activity.onBackPressed();
            }
        } else if (requestType == CommonLib.CAB_BOOKING_UPDATE_REQUEST) {

        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        zapp.zll.removeCallback(this);
        UploadManager.removeCallback(this);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mNotificationReceived);
        if (zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();
        if(pintent != null) {
            pintent.cancel();
            alarmManager.cancel(pintent);
        } else {
            Intent serviceIntent = new Intent(mContext, TrackingService.class);
            serviceIntent.putExtra("hash",bookingDetails.getHash());
            pintent = PendingIntent.getService(mContext, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if(pintent != null) {
                pintent.cancel();
                alarmManager.cancel(pintent);
            }
        }
        super.onDestroy();
    }

    public void callStore(String contact) {

        StringTokenizer st = new StringTokenizer(contact, ":");
        String phoneNumberToDisplay = "";
        if (st.hasMoreTokens()) {
            phoneNumberToDisplay = st.nextToken();
        }

        if (contact.length() > 3) {
            final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberToDisplay));
            AlertDialog.Builder builder_loc = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK);

            Dialog dialog = null;
            builder_loc.setMessage(phoneNumberToDisplay).setCancelable(true).setPositiveButton(
                    getResources().getString(R.string.dialog_call), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            try {
                                startActivityForResult(intent, MAKE_CALL_INTENT);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton(getResources().getString(R.string.dialog_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            dialog = builder_loc.create();
            dialog.show();

        }
    }

    @Override
    public void onCoordinatesIdentified(Location loc) {
        if(receiveUpdates){

            Location oldLocation = new Location("");
            Location newLocation = new Location("");

            oldLocation.setLatitude(savedLat);
            oldLocation.setLongitude(savedLon);

            newLocation.setLatitude(loc.getLatitude());
            newLocation.setLongitude(loc.getLongitude());

            LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());
            float bearing = oldLocation.bearingTo(newLocation);

            if (marker != null ) {
                marker.setRotation(bearing);
                animateMarker(marker, latlng, googleMap);
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
}
