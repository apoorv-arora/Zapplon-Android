package com.application.zapplon.views;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.GMapV2Direction;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by apoorvarora on 26/03/16.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private boolean destroyed = false;

    // map object
    private GoogleMap googleMap;
    GMapV2Direction md;

    Bundle bundle;

    public static HomeFragment newInstance(Bundle bundle) {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static View view;
    Polyline polylin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_home, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                view = inflater.inflate(R.layout.fragment_home, null);
            } catch (Exception e1) {
                e1.printStackTrace();
                if(view==null)
                    view = inflater.inflate(R.layout.fragment_home, null);
            }
            return view;
        }

        return view;
    }

    private boolean isLocationChanged = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        vi = LayoutInflater.from(activity.getApplicationContext());

        md = new GMapV2Direction();
        // load data from activity
        try {// Loading map

            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        getView.findViewById(R.id.map).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mMapIsTouched = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        mMapIsTouched = false;
                        break;
                }
                return false;
            }
        });

        getView.findViewById(R.id.set_parent_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((Home)activity).locationAvailable) {
                    //if ((((Home) activity).all_text.getText().toString()).equals(getResources().getString(R.string.cabs))) {
                    if (!((Home) activity).isCabBookingFragmentInitiated) {
                        ((Home) activity).isCabBookingFragmentInitiated = true;
                        TextView back_icon = (TextView) ((Home) activity).actionBarCustomView.findViewById(R.id.drawer_left_icon);
                        back_icon.setText(getResources().getString(R.string.z_left_arrow));
                        ((Home) activity).title.setVisibility(View.GONE);
                        if(googleMap != null)
                            googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                                @Override
                                public void onSnapshotReady(Bitmap bitmap) {
                                    if (!destroyed && activity != null) {
                                        ((Home) activity).setMapBitmap(CommonLib.fastBlur(bitmap, 14));
                                    }
                                }
                            });
                        ((Home) activity).nextFragment();
                    }
                    //}
                }
                else
                {
                    ((Home)activity).LocationCheck(activity);
                }
            }
        });

        getView.post(new Runnable() {
            @Override
            public void run() {
                if(!destroyed) {
                    if (Build.VERSION.SDK_INT > 20){
                        getView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                getView.removeOnLayoutChangeListener(this);
                                final View myView = getView.findViewById(R.id.set_parent_container);
                                enterReveal(myView);
                            }
                        });

                    }
                    else
                        getView.findViewById(R.id.set_parent_container).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @TargetApi(21)
    void enterReveal(final View myView) {
        // previously invisible view
        if(!destroyed && isAdded() && getView != null ){
            //final View myView = getView.findViewById(R.id.set_parent_container);

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            myView.setVisibility(View.VISIBLE);
            anim.setDuration(CommonLib.REVEAL_ANIM_TIMER);
            anim.start();
        }

    }

    /**
     * function to load map. If map is not created it will create it for you
     */

    private void initilizeMap() {
        if (googleMap == null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (!destroyed) {
                        try {
                            ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(HomeFragment.this);
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            e.printStackTrace();
                        }
                    }
                }
            }, 1000);
        }
    }

    private AsyncTask mAsyncRunning;

    public void refreshView(double latitude, double longitude) {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetLocation(latitude, longitude).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private class GetLocation extends AsyncTask<Object, Void, Object> {

        private double latitude;
        private double longitude;

        public GetLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(activity, Locale.getDefault());
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = null;
                if(addresses != null && addresses.get(0) != null) {
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    address = address + ", " + city + ", " + state + ", " + country;

                    zapp.setLocationString(city);
                    zapp.setCountryString(country);
                    zapp.setAddressString(address);
                }

                return address;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;
            if (activity != null && ((Home) activity).getStartLocation() != null) {
                if (result == null) {
                    ((Home) activity).startLocation.setText("Start Location ");
                    ((Home) activity).startLocation.setTextColor(Color.parseColor("#7F7F7F"));
                }
                else
                    ((Home) activity).getStartLocation().setText(String.valueOf(result));
            }
            if(getView != null) {
                if (result == null)
                    result = "No Internet";
            }
        }
    }
    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
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

//            googleMap.setTrafficEnabled(true);

            // Enable / Disable zooming functionality
            googleMap.getUiSettings().setZoomGesturesEnabled(true);

            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (mMapIsTouched) {
                        return;
                    }
                    ((Home)activity).latLngBounds =  googleMap.getProjection().getVisibleRegion().latLngBounds;

                    LatLng bounds = cameraPosition.target;

                    final double latitude = bounds.latitude;
                    final double longitude = bounds.longitude;

                    ((Home) activity).setLatitudeStart(latitude);
                    ((Home) activity).setLongitudeStart(longitude);

                    ((Home) activity).setWishes(null);

                    if(!((Home)activity).setAddress){
                        refreshView(latitude, longitude);
                        ((Home)activity).setAddress = false;
                    }
                }
            });

            refreshMap();
        }
    }

    protected void refreshMap() {

        if(activity != null && googleMap != null && isAdded()) { // added due to crashlytics bugs #184
            LatLng location = new LatLng(((Home) activity).getLatitudeStart(),
                    ((Home) activity).getLongitudeStart());
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(location, (float) 15.0));
        }
    }

    public void drawPath(Document doc){

        if(googleMap != null && isAdded()){  // added due to crashlytics bugs #184

            if(polylin != null){
                polylin.remove();
            }

            ArrayList<LatLng> directionPoint = md.getDirection(doc);
            PolylineOptions rectLine = new PolylineOptions().width(4).color(
                    getResources().getColor(R.color.fb_background));

            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            polylin = googleMap.addPolyline(rectLine);

        }

    }

    private boolean mMapIsTouched;
    private boolean shouldChange = false;
}
