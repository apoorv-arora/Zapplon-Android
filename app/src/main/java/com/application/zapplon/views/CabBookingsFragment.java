package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.CabBooking;
import com.application.zapplon.data.CabDetails;
import com.application.zapplon.data.ConnectedAccount;
import com.application.zapplon.data.Surcharge;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.IconView;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;
import com.application.zapplon.utils.ZCabWebView;
import com.application.zapplon.utils.ZTracker;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by apoorvarora on 09/02/16.
 */
public class CabBookingsFragment extends Fragment implements UploadManagerCallback {

    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private LinearLayout absoluteLayout;

    private AsyncTask mAsyncRunning;
    private Activity mContext;
    private boolean destroyed = false;
    private BookingAdapter mAdapter;

    // Load more part
    private ListView mListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog zProgressDialog;
    private View customView;
    private View drawerView;

    private ArrayList<String> filter;

    private ImageView ola, mega, easy, jugnoo,ridz;
    private Object filterObject;
    private boolean olaCheck, megaCheck, easyCheck, jugnooCheck, ridzCheck,isOpened;

    private RelativeLayout clickedView;
    private Animation animation;

    LayoutInflater inflater;

    public static CabBookingsFragment newInstance(Bundle bundle) {
        CabBookingsFragment fragment = new CabBookingsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cab_bookings, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        mContext = activity;
        destroyed = false;
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        isOpened = false;

        filter = new ArrayList<>();

        filterObject = new Object();

        absoluteLayout = (LinearLayout) getView.findViewById(R.id.arrow_sec);
        inflater = LayoutInflater.from(activity);
        swipeRefreshLayout = (SwipeRefreshLayout) getView.findViewById(R.id.content);
        mListView = (ListView) getView.findViewById(R.id.cabs_list);
        mListView.setDivider(null);
        mListView.setDividerHeight(width / 20);
        mListView.setClipToPadding(false);

        View headerView = new View(mContext);
        headerView.setMinimumHeight(width / 40);
        mListView.addHeaderView(headerView);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (mListView == null || mListView.getChildCount() == 0) ?
                                0 : mListView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        //((Home)activity).homeSelection = ((Home)activity).listSelection;

        //((Home)activity).changeViews(((Home)activity).TYPE_ALL);
        try {
            drawerView = inflater.inflate(R.layout.cab_drawer_layout, absoluteLayout, false);
            drawerView.setVisibility(View.GONE);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        View randomView = new View(activity);
        randomView.setMinimumHeight(width / 3);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CommonLib.ZLog("onRefresh", "called");
                refreshView();
            }

        });

        getView.findViewById(R.id.overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOpened = false;
                drawerView.animate().translationYBy(60).translationY(0).alpha(0.0f);
                absoluteLayout.removeViewInLayout(drawerView);
                (getView.findViewById(R.id.arrow_close)).animate().rotationX((float) -180);
                getView.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
                getView.findViewById(R.id.arrow_close).setVisibility(View.GONE);

                getView.findViewById(R.id.overlay).setVisibility(View.GONE);

            }
        });

        refreshView();

        olaCheck = false;
        easyCheck = false;
        megaCheck = false;
        jugnooCheck = false;
        ridzCheck = false;

        getView.findViewById(R.id.arrow_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOpened){
                    isOpened = true;
                    if (drawerView != null) {

                        getView.findViewById(R.id.overlay).setVisibility(View.VISIBLE);

                        drawerView.setVisibility(View.VISIBLE);
                        drawerView.animate().translationYBy(0).translationY(60).alpha(1.0f);
                        absoluteLayout.addView(drawerView, 0);
                        getView.findViewById(R.id.arrow_close).setVisibility(View.VISIBLE);
                        getView.findViewById(R.id.arrow).setVisibility(View.GONE);
                        (getView.findViewById(R.id.arrow_close)).animate().rotationX((float) -180);

                        ola = (ImageView) getView.findViewById(R.id.ola_icon);
                        mega = (ImageView) getView.findViewById(R.id.mega_icon);
                        easy = (ImageView) getView.findViewById(R.id.easy_icon);
                        jugnoo = (ImageView) getView.findViewById(R.id.jugnoo_icon);
                        ridz = (ImageView) getView.findViewById(R.id.ridz_icon);

                        if(olaCheck){
                            ola.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                        }else{
                            ola.setColorFilter(Color.parseColor("#baffffff"));
                        }
                        if(megaCheck){
                            mega.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                        }else{
                            mega.setColorFilter(Color.parseColor("#baffffff"));
                        }
                        if(easyCheck){
                            easy.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                        }else{
                            easy.setColorFilter(Color.parseColor("#baffffff"));
                        }
                        if(jugnooCheck){
                            jugnoo.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                        }else{
                            jugnoo.setColorFilter(Color.parseColor("#baffffff"));
                        }
                        if(ridzCheck){
                            ridz.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                        }else{
                            ridz.setColorFilter(Color.parseColor("#baffffff"));
                        }

                        ola.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!olaCheck) {
                                    ola.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                                    filter.add("ola");
                                    filter(filter);
                                    olaCheck = true;
                                } else {
                                    olaCheck = false;
                                    ola.setColorFilter(Color.parseColor("#baffffff"));
                                    if (!olaCheck && !easyCheck && !megaCheck && !jugnooCheck && !ridzCheck) {
                                        if (getFilterObject() != null && getFilterObject() instanceof ArrayList<?>) {
                                            ArrayList<CabDetails> cabs = new ArrayList<>();
                                            for (CabDetails cab : (ArrayList<CabDetails>) getFilterObject()) {
                                                cabs.add(cab);
                                            }
                                            updateFilterWishes(cabs);
                                            filter.clear();
                                        }
                                    } else {
                                        if ((filter != null)) {
                                            filter.remove("ola");
                                            filter(filter);
                                        }
                                    }
                                }
                            }
                        });
                        easy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!easyCheck) {
                                    easy.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                                    filter.add("easy");
                                    filter(filter);
                                    easyCheck = true;
                                } else {
                                    easyCheck = false;
                                    easy.setColorFilter(Color.parseColor("#baffffff"));
                                    if (!olaCheck && !easyCheck && !megaCheck && !jugnooCheck && !ridzCheck) {
                                        if (getFilterObject() != null && getFilterObject() instanceof ArrayList<?>) {
                                            ArrayList<CabDetails> cabs = new ArrayList<>();
                                            for (CabDetails cab : (ArrayList<CabDetails>) getFilterObject()) {
                                                cabs.add(cab);
                                            }
                                            updateFilterWishes(cabs);
                                            filter.clear();
                                        }
                                    } else {
                                        if ((filter != null)) {
                                            filter.remove("easy");
                                            filter(filter);
                                        }
                                    }
                                }
                            }
                        });
                        mega.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!megaCheck) {
                                    mega.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                                    filter.add("mega");
                                    filter(filter);
                                    megaCheck = true;
                                } else {
                                    megaCheck = false;
                                    mega.setColorFilter(Color.parseColor("#baffffff"));
                                    if (!olaCheck && !easyCheck && !megaCheck && !jugnooCheck && !ridzCheck) {
                                        if (getFilterObject() != null && getFilterObject() instanceof ArrayList<?>) {
                                            ArrayList<CabDetails> cabs = new ArrayList<>();
                                            for (CabDetails cab : (ArrayList<CabDetails>) getFilterObject()) {
                                                cabs.add(cab);
                                            }
                                            updateFilterWishes(cabs);
                                            filter.clear();
                                        }
                                    } else {
                                        if ((filter != null)) {
                                            filter.remove("mega");
                                            filter(filter);
                                        }
                                    }
                                }
                            }
                        });
                        jugnoo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!jugnooCheck) {
                                    jugnoo.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                                    filter.add("jugnoo");
                                    filter(filter);
                                    jugnooCheck = true;
                                } else {
                                    jugnooCheck = false;
                                    jugnoo.setColorFilter(Color.parseColor("#baffffff"));
                                    if (!olaCheck && !easyCheck && !megaCheck && !jugnooCheck && !ridzCheck) {
                                        if (getFilterObject() != null && getFilterObject() instanceof ArrayList<?>) {
                                            ArrayList<CabDetails> cabs = new ArrayList<>();
                                            for (CabDetails cab : (ArrayList<CabDetails>) getFilterObject()) {
                                                cabs.add(cab);
                                            }
                                            updateFilterWishes(cabs);
                                            filter.clear();
                                        }
                                    } else {
                                        if ((filter != null)) {
                                            filter.remove("jugnoo");
                                            filter(filter);
                                        }
                                    }
                                }
                            }
                        });

                        ridz.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!ridzCheck) {
                                    ridz.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                                    filter.add("ridz");
                                    filter(filter);
                                    ridzCheck = true;
                                } else {
                                    ridzCheck = false;
                                    ridz.setColorFilter(Color.parseColor("#baffffff"));
                                    if (!olaCheck && !easyCheck && !megaCheck && !jugnooCheck && !ridzCheck) {
                                        if (getFilterObject() != null && getFilterObject() instanceof ArrayList<?>) {
                                            ArrayList<CabDetails> cabs = new ArrayList<>();
                                            for (CabDetails cab : (ArrayList<CabDetails>) getFilterObject()) {
                                                cabs.add(cab);
                                            }
                                            updateFilterWishes(cabs);
                                            filter.clear();
                                        }
                                    } else {
                                        if ((filter != null)) {
                                            filter.remove("ridz");
                                            filter(filter);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }else{
                    isOpened = false;
                    drawerView.animate().translationYBy(60).translationY(0).alpha(0.0f);
                    absoluteLayout.removeViewInLayout(drawerView);
                    (getView.findViewById(R.id.arrow_close)).animate().rotationX((float) -180);
                    getView.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
                    getView.findViewById(R.id.arrow_close).setVisibility(View.GONE);

                    getView.findViewById(R.id.overlay).setVisibility(View.GONE);
                }

            }
        });


        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        setListeners();

        refreshView();

        ((Home)activity).all_text.setText(getResources().getString(R.string.all));
        ((Home)activity).cab_text.setText(getResources().getString(R.string.car));
        ((Home)activity).bike_text.setText(getResources().getString(R.string.bike));
        ((Home)activity).auto_text.setText(getResources().getString(R.string.auto));

        ((Home)activity).all_icon.setText(getResources().getString(R.string.z_all));
        ((Home)activity).cab_icon.setText(getResources().getString(R.string.z_cab));
        ((Home)activity).auto_icon.setText(getResources().getString(R.string.z_auto));
        ((Home)activity).bike_icon.setText(getResources().getString(R.string.z_bike));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!destroyed && activity != null) {
                    if(((Home)activity).getMapBitmap() != null)
                        swipeRefreshLayout.setBackgroundDrawable(new BitmapDrawable(((Home) activity).getMapBitmap()));
                    else
                        swipeRefreshLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.bmap));
                }
            }
        }, 2000);

        UploadManager.addCallback(this);
    }

    private void setListeners() {
        getView.findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView();
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;

        ((Home)activity).isCabBookingFragmentInitiated = false;
        UploadManager.removeCallback(this);
        if (zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();
        super.onDestroy();
    }

    public void scrollCabToTop() {
        try {
            if (mListView != null)
                mListView.setSelection(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshView() {
        //if(!(((Home) activity).showView.equalsIgnoreCase("auto")) && !(((Home) activity).showView.equalsIgnoreCase("bike"))){
        mListView.setVisibility(View.VISIBLE);
        //getView.findViewById(R.id.coming_soon).setVisibility(View.GONE);
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetUberCabs().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        //}

    }

    @Override
    public void uploadFinished(int requestType, int userId, final int objectId, Object data,
                               final int uploadId, boolean status, final String stringId) {
        if (requestType == CommonLib.SEND_CAB_TOKEN && stringId != null && stringId.equals("CabBookingsFragment")) {
            if (destroyed)
                return;
            if (zProgressDialog != null && zProgressDialog.isShowing())
                zProgressDialog.dismiss();
            if (status) {
                //save the token here to preferences.
                if (data != null && data instanceof ConnectedAccount) {
                    SharedPreferences.Editor editor = prefs.edit();
                    final ConnectedAccount account = (ConnectedAccount) data;

                    if (objectId == CommonLib.TYPE_OLA) {
                        editor.putString("ola_access_token", account.getAccessToken());
                        editor.putString("ola_cab_session_id", account.getCabSessionId() + "");
                    } else if (objectId == CommonLib.TYPE_UBER) {
                        editor.putString("uber_access_token", account.getAccessToken());
                        editor.putString("uber_cab_session_id", account.getCabSessionId() + "");
                    }
                    editor.commit();

                    customView = inflater.inflate(R.layout.booking_dialog, null);
                    final AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                            .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                }
                            })
                            .setView(customView)
                            .create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                    customView.findViewById(R.id.confirm_cab).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((Home) activity).getWishes() != null && ((Home) activity).getWishes().size() > uploadId) {
                                CabDetails details = ((Home) activity).getWishes().get(uploadId);
                                zProgressDialog = ProgressDialog.show(activity, null, "Booking your request. Please wait!!!");
                                String couponCode = null;
                                if (customView != null)
                                    couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();
                                UploadManager.cabBookingRequest(details.getType(), account.getAccessToken(), details.getProductId(), ((Home) activity).getLatitudeStart(), ((Home) activity).getLongitudeStart(), ((Home) activity).getStartLocationText(), ((Home) activity).getEndLocationText(), ((Home) activity).getLatitudeEnd(), ((Home) activity).getLongitudeEnd(), couponCode, "",stringId);
                                dialog.dismiss();
                            }
                        }
                    });

                    customView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    if (objectId == CommonLib.TYPE_OLA) {
                        customView.findViewById(R.id.coupon_container).setVisibility(View.VISIBLE);
                        customView.findViewById(R.id.apply_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();

                                if (couponCode == null || couponCode.isEmpty()) {
                                    Toast.makeText(activity, "Invalid code", Toast.LENGTH_SHORT).show();
                                }
                                CabDetails details = ((Home) activity).getWishes().get(uploadId);

                                UploadManager.validateCoupon(account.getAccessToken(), details.getProductId(), couponCode, objectId);

                            }
                        });
                    }
                }
            }
        } else if (requestType == CommonLib.CAB_BOOKING_REQUEST) {
            if (!destroyed) {
                if (zProgressDialog != null && zProgressDialog.isShowing())
                    zProgressDialog.dismiss();
                if (status && data != null && data instanceof CabBooking) {
                    CabBooking booking = (CabBooking) data;
                    //SharedPreferences.Editor editor = prefs.edit();
                    //booking.setDisplayName(prefs.getString("cabName",""));
                    booking.setDisplayName(stringId);
                    if (booking.getType() == CommonLib.TYPE_OLA || booking.getType() == CommonLib.TYPE_EASY || booking.getType() == CommonLib.TYPE_MEGA || booking.getType() == CommonLib.TYPE_JUGNOO || booking.getType() == CommonLib.TYPE_RIDZ) {

//                        activity.onBackPressed();
                        ((Home)activity).goToBookingFragment(booking);
                    }
                } else {
                    if (CommonLib.isNetworkAvailable(mContext)) {
//                        Toast.makeText(mContext, getResources().getString(R.string.error_try_again),
//                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        } else if (requestType == CommonLib.VALIDATE_COUPON) {
            if (!destroyed) {
                if (zProgressDialog != null && zProgressDialog.isShowing())
                    zProgressDialog.dismiss();
                if (status && data != null && customView != null) {
                    customView.findViewById(R.id.coupon_code_validity).setVisibility(View.VISIBLE);
                    customView.findViewById(R.id.apply_button).setVisibility(View.GONE);
                    ((TextView) customView.findViewById(R.id.coupon_code_validity)).setText(String.valueOf(data));
                }
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
        if (requestType == CommonLib.SEND_CAB_TOKEN) {
            //do something
        }
    }

    private class GetUberCabs extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            getView.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);

            getView.findViewById(R.id.content).setAlpha(1f);

            getView.findViewById(R.id.content).setVisibility(View.GONE);

            getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
            super.onPreExecute();
        }

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                StringBuilder url = new StringBuilder();
                url.append(CommonLib.SERVER + "booking/product" + "?latitude=" + ((Home) activity).getLatitudeStart() + "&longitude=" + ((Home) activity).getLongitudeStart());
                if (((Home) activity).getLatitudeEnd() != 0 && ((Home) activity).getLongitudeEnd() != 0)
                    url.append("&end_latitude=" + ((Home) activity).getLatitudeEnd() + "&end_longitude=" + ((Home) activity).getLongitudeEnd());
                //if(((Home)activity).homeSelection == ((Home)activity).TYPE_CAR)
                //  url.append("&type=1");
                Object info = RequestWrapper.RequestHttp(url.toString(), RequestWrapper.UBER_LIST, RequestWrapper.FAV);
                CommonLib.ZLog("url", url.toString());
                setFilterObject(info);
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

            getView.findViewById(R.id.progress_container).setVisibility(View.GONE);

            if (result != null) {
                getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof ArrayList<?>) {
                    setWishes((ArrayList<CabDetails>) result);
                    if (((ArrayList<CabDetails>) result).size() == 0) {
                        getView.findViewById(R.id.content).setVisibility(View.GONE);
                        getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        ((TextView) getView.findViewById(R.id.empty_view_text)).setText("Nothing here yet");
                    } else {
                        getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                        getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
                    }
                }
            } else {
                if (CommonLib.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext, getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                            .show();

                    getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

                    getView.findViewById(R.id.content).setVisibility(View.GONE);
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void makeLinksFocusable(TextView tv) {
        MovementMethod m = tv.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (tv.getLinksClickable()) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }


    // set all the wishes here
    private void setWishes(ArrayList<CabDetails> wishes) {
        ((Home) activity).setWishes(wishes);

        mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet, ((Home) activity).getWishes(), zapp);
        mListView.setAdapter(mAdapter);
        returnFilter(((Home)activity).showView);
        final LinearLayout layout = ((LinearLayout)getView.findViewById(R.id.arrow_container));
        sort(SORT_BY_ARRIVAL_TIME);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!destroyed){
                    animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left);
                    animation.setDuration(500);
                    animation.restrictDuration(700);
                    animation.scaleCurrentDuration(1);
                    layout.setVisibility(View.VISIBLE);
                    layout.startAnimation(animation);
                    //makeLinksFocusable();
                   // makeLinksFocusable(((TextView)getView.findViewById(R.id.arrow)));
                   // makeLinksFocusable(((TextView)getView.findViewById(R.id.arrow_close)));
                }

            }
        }, 300);

    }

    private void updateFilterWishes(ArrayList<CabDetails> wishes)
    {
        ((Home) activity).setWishes(wishes);
        mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet, wishes, zapp);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        returnFilter(((Home)activity).showView);
        sort(SORT_BY_ARRIVAL_TIME);
    }

    private void setFilterObject(Object object)
    {
        this.filterObject = object;
    }

    private Object getFilterObject()
    {
        return filterObject;
    }

    public void refreshFilter()
    {
        if(getFilterObject() != null && getFilterObject() instanceof ArrayList<?>) {
            ArrayList<CabDetails> cabs = new ArrayList<>();
            for (CabDetails cab : (ArrayList<CabDetails>) getFilterObject()) {
                cabs.add(cab);
            }
            setWishes(cabs);
        }
    }

    private void filter(ArrayList<String> filter)
    {
        if(getFilterObject() != null && getFilterObject() instanceof ArrayList<?>) {
            ArrayList<CabDetails> cabs = new ArrayList<>();
            for (int i=0;i< filter.size();i++)
            {
                if (filter.get(i).equalsIgnoreCase("ola"))
                {
                    for(CabDetails cab:(ArrayList<CabDetails>)getFilterObject()) {
                        if(cab.getType() == CommonLib.TYPE_OLA)
                            cabs.add(cab);
                    }
                }
                else if (filter.get(i).equalsIgnoreCase("easy"))
                {
                    for(CabDetails cab:(ArrayList<CabDetails>)getFilterObject()) {
                        if(cab.getType() == CommonLib.TYPE_EASY)
                            cabs.add(cab);
                    }
                }
                else if (filter.get(i).equalsIgnoreCase("mega"))
                {
                    for(CabDetails cab:(ArrayList<CabDetails>)getFilterObject()) {
                        if(cab.getType() == CommonLib.TYPE_MEGA)
                            cabs.add(cab);
                    }
                }

                else if (filter.get(i).equalsIgnoreCase("ridz"))
                {
                    for(CabDetails cab:(ArrayList<CabDetails>)getFilterObject()) {
                        if(cab.getType() == CommonLib.TYPE_RIDZ)
                            cabs.add(cab);
                    }
                }
                else if (filter.get(i).equalsIgnoreCase("jugnoo"))
                {
                    for(CabDetails cab:(ArrayList<CabDetails>)getFilterObject()) {
                        if(cab.getType() == CommonLib.TYPE_JUGNOO)
                            cabs.add(cab);
                    }
                }
                else
                {
                    for(CabDetails cab:(ArrayList<CabDetails>)getFilterObject()) {
                        cabs.add(cab);
                        continue;
                    }
                }
            }
            updateFilterWishes(cabs);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CommonLib.REQUEST_CODE_OLA_WEB_VIEW) {
                Bundle bundle = data.getExtras();
                String token = bundle.getString("token");
                int position = bundle.getInt("position");
                long deletionTime = bundle.getLong("deletionTime", 0);
                zProgressDialog = ProgressDialog.show(mContext, null, "Logging you in OLA. Please wait!!!");
                UploadManager.sendCabToken(token, deletionTime, CommonLib.TYPE_OLA, "CabBookingsFragment", position);
            } else if (requestCode == CommonLib.REQUEST_CODE_UBER_WEB_VIEW) {
                Bundle bundle = data.getExtras();
                String token = bundle.getString("token");
                int position = bundle.getInt("position");
                long deletionTime = bundle.getLong("deletionTime", 0);
                zProgressDialog = ProgressDialog.show(mContext, null, "Logging you in UBER. Please wait!!!");
                UploadManager.sendCabToken(token, deletionTime, CommonLib.TYPE_UBER, "CabBookingsFragment", position);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    protected static class ViewHolder {
        ImageView cabIcon;
        TextView surgeIcon;
        TextView displayName;
        TextView cabType;
        TextView time;
        TextView ratePerKm;
        IconView surge_icon1;
        RelativeLayout mainLayout;
        RelativeLayout containerLayout;
        TextView zappsCount;
        TextView recommended;
    }

    int savedPosition = -1;

    public void returnFilter(String s){
        if(mAdapter!=null){
            mAdapter.getFilter().filter(s);
        }
    }

    private class BookingAdapter extends ArrayAdapter<CabDetails> implements Filterable {

        private List<CabDetails> wishes;
        private List<CabDetails> filteredList = null;
        private Activity mContext;
        private int width;
        private ZApplication zapp;
        private LayoutInflater vi;
        private ItemFilter mFilter = new ItemFilter();

        public BookingAdapter(Activity context, int resourceId, List<CabDetails> wishes, ZApplication zapp) {
            super(context.getApplicationContext(), resourceId, wishes);
            mContext = context;
            this.wishes = wishes;
            this.filteredList = wishes;
            this.zapp = zapp;
            width = mContext.getWindowManager().getDefaultDisplay().getWidth();
            vi = LayoutInflater.from(mContext);
            clickedView = null;
        }

        @Override
        public int getCount() {
            if (filteredList == null) {
                return 0;
            } else {
                return filteredList.size();
            }
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.new_cab_snippet, null);
                viewHolder = new ViewHolder();
                viewHolder.mainLayout = (RelativeLayout) convertView.findViewById(R.id.new_cab_snippet_container);
                viewHolder.cabIcon = (ImageView) convertView.findViewById(R.id.cab_icon);
                viewHolder.surgeIcon = (TextView) convertView.findViewById(R.id.surge_icon1);
                viewHolder.displayName = (TextView) convertView.findViewById(R.id.display_name1);
                viewHolder.cabType = (TextView) convertView.findViewById(R.id.cab_type);
                viewHolder.ratePerKm = (TextView) convertView.findViewById(R.id.rate_per_KM1);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time1);
                viewHolder.containerLayout = (RelativeLayout) convertView.findViewById(R.id.cab_details_container);
                viewHolder.surge_icon1 = (IconView) convertView.findViewById(R.id.surge_icon1);
                viewHolder.recommended = (TextView) convertView.findViewById(R.id.recommended);
                viewHolder.zappsCount = (TextView) convertView.findViewById(R.id.zapp_count1);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if(position != savedPosition)
                viewHolder.containerLayout.removeAllViews();

            String subType ="";
            final CabDetails cab = filteredList.get(position);

            try {
                viewHolder.cabIcon.setImageBitmap(CommonLib.getBitmap(mContext, CommonLib.getBrandBitmap(cab.getType()), width, width));
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
            if(cab.getSubType()== CommonLib.CAB_SEDAN){
                subType = getResources().getString(R.string.sedan);
            }else if(cab.getSubType()== CommonLib.CAB_COMPACT){
                subType = getResources().getString(R.string.compact);
            }else if(cab.getSubType()== CommonLib.CAB_LUXURY){
                subType = getResources().getString(R.string.luxury);
            }else if(cab.getSubType()== CommonLib.SUV){
                subType = getResources().getString(R.string.suv);
            }else if(cab.getSubType()== CommonLib.BIKE){
                subType = getResources().getString(R.string.bike);
            }else if(cab.getSubType()== CommonLib.AUTO){
                subType = getResources().getString(R.string.auto);
            }

            if(cab.getIsRecommended() == 1)
                viewHolder.recommended.setVisibility(View.VISIBLE);
            else
                viewHolder.recommended.setVisibility(View.GONE);

            if(cab.getZapp_count() > 0) {
                convertView.findViewById(R.id.zapps_container).setVisibility(View.VISIBLE);
                viewHolder.zappsCount.setText(cab.getZapp_count()+"");

                if(cab.getZapp_count() > 1)
                    ((TextView) convertView.findViewById(R.id.zapp)).setText(getResources().getString(R.string.zapps));
                else
                    ((TextView) convertView.findViewById(R.id.zapp)).setText(getResources().getString(R.string.zapp));

            } else {
                convertView.findViewById(R.id.zapps_container).setVisibility(View.INVISIBLE);
            }

            viewHolder.displayName.setText(cab.getDisplayName());
            viewHolder.cabType.setText(subType);
            Double price = cab.getCostPerDistance();
            final DecimalFormat format = new DecimalFormat("0.#");
            viewHolder.ratePerKm.setText("₹ "+format.format(price)+"/km");

            if(cab.getEstimatedTimeOfArrival() > 1)
                viewHolder.time.setText(cab.getEstimatedTimeOfArrival()+" MINS");
            else
                viewHolder.time.setText(cab.getEstimatedTimeOfArrival()+" MIN");

            if (cab.getEstimatedTimeOfArrival() > 0) {
                viewHolder.time.setVisibility(View.VISIBLE);
            } else {
                viewHolder.time.setVisibility(View.INVISIBLE);
            }

            if(cab.getSurcharge() != null && cab.getSurcharge().getValue() > 1) {
                viewHolder.surge_icon1.setVisibility(View.VISIBLE);
            } else {
                viewHolder.surge_icon1.setVisibility(View.INVISIBLE);
            }

            viewHolder.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(savedPosition == position) {
                        if(clickedView!=null)
                            clickedView.removeAllViews();
                        savedPosition = -1;
                    } else {
                        View menuLayout = inflater.inflate(R.layout.new_cab_snippet_detail, null);
                        if(clickedView!=null)
                            clickedView.removeAllViews();
                        viewHolder.containerLayout.addView(menuLayout, 0);
                        clickedView = viewHolder.containerLayout;

                        savedPosition = position;

                        TextView recommend = (TextView)menuLayout.findViewById(R.id.recommended2);

                        if(cab.getIsRecommended() == 1)
                            recommend.setVisibility(View.VISIBLE);
                        else
                            recommend.setVisibility(View.GONE);


                        ((ImageView) (ImageView) menuLayout.findViewById(R.id.cab_icon2)).setImageBitmap(CommonLib.getBitmap(mContext, CommonLib.getBrandBitmap(cab.getType()), width, width));

                        TextView display_name_2 = (TextView) menuLayout.findViewById(R.id.display_name2);
                        display_name_2.setText(viewHolder.displayName.getText());

                        TextView cab_type_2 = (TextView) menuLayout.findViewById(R.id.cab_type2);
                        cab_type_2.setText(viewHolder.cabType.getText());

                        TextView time_2 = (TextView) menuLayout.findViewById(R.id.time2);
                        if (cab.getEstimatedTimeOfArrival() > 1)
                            time_2.setText(cab.getEstimatedTimeOfArrival() + " mins");
                        else
                            time_2.setText(cab.getEstimatedTimeOfArrival() + " min");

                        TextView baseFare_2 = (TextView) menuLayout.findViewById(R.id.travel_cost);
                        baseFare_2.setText("₹ " + format.format(cab.getBase()));

                        TextView rate_per_km_2 = (TextView) menuLayout.findViewById(R.id.rate_per_KM2);
                        rate_per_km_2.setText("₹ " +format.format(cab.getCostPerDistance()) );

                        if (cab.getZapp_count() > 0) {
                            menuLayout.findViewById(R.id.zapps_container).setVisibility(View.VISIBLE);
                            TextView zappCount = (TextView) menuLayout.findViewById(R.id.zapp_count2);
                            zappCount.setText(cab.getZapp_count() + "");

                            if (cab.getZapp_count() > 1)
                                ((TextView) menuLayout.findViewById(R.id.zapp)).setText(getResources().getString(R.string.zapps));
                            else
                                ((TextView) menuLayout.findViewById(R.id.zapp)).setText(getResources().getString(R.string.zapp));

                        } else {
                            menuLayout.findViewById(R.id.zapps_container).setVisibility(View.INVISIBLE);
                        }

                        if (cab.getSurcharge() != null && cab.getSurcharge().getValue() > 1) {
                            menuLayout.findViewById(R.id.surge_container).setVisibility(View.VISIBLE);
                            Surcharge surcharge = cab.getSurcharge();
                            StringBuilder builder = new StringBuilder();
                            if (surcharge.getType() != null) {
                                if (surcharge.getType().startsWith("m")) {
                                    builder.append(surcharge.getValue() + "x" + " SURGE");
                                } else
                                    builder.append("+ ₹" + surcharge.getValue() + " SURGE");
                            } else {
                                builder.append(surcharge.getValue() + "x" + " SURGE");
                            }
                            ((TextView) menuLayout.findViewById(R.id.surge_value2)).setText(builder.toString());
                        } else {
                            menuLayout.findViewById(R.id.surge_container).setVisibility(View.INVISIBLE);
                        }

                        if (cab.getPriceEstimate() != null) {
                            TextView costEstimate = (TextView) menuLayout.findViewById(R.id.EST_COST);
                            costEstimate.setText(cab.getPriceEstimate()+"");
                        }

                        if (cab.getTimeEstimate() != null) {

                            long timeInMs = System.currentTimeMillis();
                            String timeEstimate = cab.getTimeEstimate();

                            if (timeEstimate.contains("-")) {
                                String timeEst = "";

                                if (Integer.parseInt(timeEstimate.split("-")[0]) > Integer.parseInt(timeEstimate.split("-")[1]))
                                    timeEst = timeEstimate.split("-")[1];
                                else
                                    timeEst = timeEstimate.split("-")[0];

                                long eta = timeInMs + Integer.parseInt(timeEst) * 60 * 1000;


                                Date date = new Date(eta);
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // Set your date format
                                String currentData = sdf.format(date);

                                menuLayout.findViewById(R.id.lb1).setVisibility(View.GONE);
                                menuLayout.findViewById(R.id.lb2).setVisibility(View.VISIBLE);
                                TextView eta_value = (TextView) menuLayout.findViewById(R.id.ETA_VALUE);
                                eta_value.setText(currentData);
                            } else {
                                long eta = timeInMs + Integer.parseInt(timeEstimate) * 60 * 1000;

                                Date date = new Date(eta);
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // Set your date format
                                String currentData = sdf.format(date);

                                menuLayout.findViewById(R.id.lb1).setVisibility(View.GONE);
                                menuLayout.findViewById(R.id.lb2).setVisibility(View.VISIBLE);
                                TextView eta_value = (TextView) menuLayout.findViewById(R.id.ETA_VALUE);
                                eta_value.setText(currentData);
                            }
                        } else {
                            TextView blankView = (TextView) menuLayout.findViewById(R.id.lb1);
                            if (((Home) activity).getLatitudeEnd() != 0 && ((Home) activity).getLongitudeEnd() != 0) {
                                blankView.setText(getResources().getString(R.string.add_destination));
                                blankView.setVisibility(View.GONE);
                            } else {
                                blankView.setText(getResources().getString(R.string.add_destination));
                                blankView.setVisibility(View.VISIBLE);
                            }
                        }

                        menuLayout.findViewById(R.id.book_now).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ZTracker.logGAEvent(mContext, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_STORE_CABS_BOOKING_PRESSED, "");
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("cabName", cab.getDisplayName());
                                editor.putString("baseFare", cab.getBase() + "");
                                editor.putString("ratePerKm", cab.getCostPerDistance() + "");
                                editor.putInt("cabType", cab.getType());
                                editor.commit();

                                if (prefs.getInt("uid", 0) != 0) {
                                    if (cab.getType() == CommonLib.TYPE_UBER) {
                                        final String startLocationText = ((Home) activity).getStartLocation().getText().toString();
                                        final String endLocationText = ((Home) activity).getDropLocation().getText().toString();
                                        try {
                                            PackageManager pm = mContext.getPackageManager();
                                            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
                                            String locationString = ((Home) activity).getLatitudeStart() + "&" + ((Home) activity).getLongitudeStart();
                                            String uri =
                                                    "uber://?action=setPickup&pickup=" + locationString + "&product_id=" + cab.getProductId() + "&client_id=" + CommonLib.UBER_CLIENT_ID;
                                            //TODO: try adding pickup_latitude, pickup_longitude
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(uri));
                                            mContext.startActivity(intent);
                                        } catch (PackageManager.NameNotFoundException e) {
                                            // No Uber app! Open mobile website.
                                            String url = "https://m.uber.com/sign-up?product_id=" + cab.getProductId() + "&client_id=" + CommonLib.UBER_CLIENT_ID;
                                            //TODO: try adding pickup_latitude, pickup_longitude
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse(url));
                                            mContext.startActivity(i);
                                        }
                                        String couponCode = null;
                                        if (customView != null)
                                            couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();
                                        UploadManager.cabBookingRequest(CommonLib.TYPE_UBER, "", cab.getProductId(), ((Home) activity).getLatitudeStart(), ((Home) activity).getLongitudeStart(), startLocationText, endLocationText, ((Home) activity).getLatitudeEnd(), ((Home) activity).getLongitudeEnd(), couponCode, "", cab.getDisplayName());
                                    } else if (cab.getType() == CommonLib.TYPE_OLA) {
                                        SharedPreferences prefs = mContext.getSharedPreferences("application_settings", 0);
                                        final String olaToken = prefs.getString("ola_access_token", "");
                                        if (olaToken != null && olaToken.length() > 0) {

                                            if (cab.getProductId() != null && cab.getProductId().equalsIgnoreCase("auto")) {
                                                final String endLocationText = ((Home) activity).getDropLocation().getText().toString();
                                                if (endLocationText == null || endLocationText.length() < 1) {
                                                    Toast.makeText(activity, "Please select drop location", Toast.LENGTH_SHORT).show();
                                                    Animation shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
                                                    ((Home) activity).getDropLocation().startAnimation(shake);
                                                    return;
                                                }
                                            }

                                            customView = inflater.inflate(R.layout.booking_dialog, null);
                                            final AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                                                    .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                        @Override
                                                        public void onCancel(DialogInterface dialog) {
                                                        }
                                                    })
                                                    .setView(customView)
                                                    .create();
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.show();

                                            customView.findViewById(R.id.confirm_cab).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    zProgressDialog = ProgressDialog.show(activity, null, "Booking your request. Please wait!!!");
                                                    String couponCode = null;
                                                    if (customView != null)
                                                        couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();
                                                    UploadManager.cabBookingRequest(cab.getType(), olaToken, cab.getProductId(), ((Home) activity).getLatitudeStart(), ((Home) activity).getLongitudeStart(), ((Home) activity).getStartLocationText(), ((Home) activity).getEndLocationText(), ((Home) activity).getLatitudeEnd(), ((Home) activity).getLongitudeEnd(), couponCode, "", cab.getDisplayName());
                                                    dialog.dismiss();
                                                }
                                            });

                                            customView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                }
                                            });

                                            customView.findViewById(R.id.coupon_container).setVisibility(View.VISIBLE);
                                            customView.findViewById(R.id.apply_button).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();

                                                    if (couponCode == null || couponCode.isEmpty()) {
                                                        Toast.makeText(activity, "Invalid code", Toast.LENGTH_SHORT).show();
                                                    }
                                                    UploadManager.validateCoupon(olaToken, cab.getProductId(), couponCode, cab.getType());
                                                }
                                            });

                                        } else {
                                            Intent intent = new Intent(mContext, ZCabWebView.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("url", "https://devapi.olacabs.com/oauth2/authorize?response_type=token&client_id=ODA4OGRkZDUtM2EzZS00YWQ5LTlmOTktNDI2MTg1NTU1YTQz&redirect_uri=http://zapplon.com/&scope=profile%20booking&state=state123\n");
                                            bundle.putString("title", "Ola");
                                            bundle.putInt("position", position);
                                            intent.putExtras(bundle);
                                            mContext.startActivityForResult(intent, CommonLib.REQUEST_CODE_OLA_WEB_VIEW);
                                        }
                                    } else if (cab.getType() == CommonLib.TYPE_EASY) {
                                        if (activity instanceof Home) {
                                            final String startLocationText = ((Home) activity).getStartLocation().getText().toString();
                                            final String endLocationText = ((Home) activity).getDropLocation().getText().toString();

                                            if (startLocationText == null || startLocationText.length() < 1) {
                                                Toast.makeText(activity, "Please select start location", Toast.LENGTH_SHORT).show();
                                                Animation shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
                                                ((Home) activity).getStartLocation().startAnimation(shake);
                                                return;
                                            }

                                            if (endLocationText == null || endLocationText.length() < 1) {
                                                Toast.makeText(activity, "Please select drop location", Toast.LENGTH_SHORT).show();
                                                Animation shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
                                                ((Home) activity).getDropLocation().startAnimation(shake);
                                                return;
                                            }


                                            customView = vi.inflate(R.layout.booking_dialog, null);
                                            final AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                                                    .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                        @Override
                                                        public void onCancel(DialogInterface dialog) {
                                                        }
                                                    })
                                                    .setView(customView)
                                                    .create();
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.show();

                                            customView.findViewById(R.id.confirm_cab).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    zProgressDialog = ProgressDialog.show(activity, null, "Booking your request. Please wait!!!");
                                                    String couponCode = null;
                                                    if (customView != null)
                                                        couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();
                                                    Calendar calender = Calendar.getInstance();
                                                    calender.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 17));

                                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    String formattedDate = dateFormat.format(calender.getTime());
                                                    String[] args = formattedDate.split(" ");
                                                    if (args.length == 2) {
                                                        formattedDate = args[0] + "T" + args[1];
                                                    }
                                                    UploadManager.cabBookingRequest(cab.getType(), "", cab.getProductId(), ((Home) activity).getLatitudeStart(), ((Home) activity).getLongitudeStart(), startLocationText, endLocationText, ((Home) activity).getLatitudeEnd(), ((Home) activity).getLongitudeEnd(), couponCode, formattedDate, cab.getDisplayName());
                                                    dialog.dismiss();
                                                }
                                            });

                                            customView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    } else if (cab.getType() == CommonLib.TYPE_JUGNOO) {
                                        if (activity instanceof Home) {
                                            final String startLocationText = ((Home) activity).getStartLocation().getText().toString();
                                            final String endLocationText = ((Home) activity).getDropLocation().getText().toString();

                                            if (startLocationText == null || startLocationText.length() < 1) {
                                                Toast.makeText(activity, "Please select start location", Toast.LENGTH_SHORT).show();
                                                Animation shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
                                                ((Home) activity).getStartLocation().startAnimation(shake);
                                                return;
                                            }

                                            customView = vi.inflate(R.layout.booking_dialog, null);
                                            final AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                                                    .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                        @Override
                                                        public void onCancel(DialogInterface dialog) {
                                                        }
                                                    })
                                                    .setView(customView)
                                                    .create();
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.show();

                                            customView.findViewById(R.id.confirm_cab).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    zProgressDialog = ProgressDialog.show(activity, null, "Booking your request. Please wait!!!");
                                                    String couponCode = null;
                                                    if (customView != null)
                                                        couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();
                                                    UploadManager.cabBookingRequest(cab.getType(), "", cab.getProductId(), ((Home) activity).getLatitudeStart(), ((Home) activity).getLongitudeStart(), startLocationText, endLocationText, ((Home) activity).getLatitudeEnd(), ((Home) activity).getLongitudeEnd(), couponCode, "", cab.getDisplayName());
                                                    dialog.dismiss();
                                                }
                                            });

                                            customView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                }
                                            });
                                        }

                                    }else if (cab.getType() == CommonLib.TYPE_RIDZ) {
                                        if (activity instanceof Home) {
                                            final String startLocationText = ((Home) activity).getStartLocation().getText().toString();
                                            final String endLocationText = ((Home) activity).getDropLocation().getText().toString();

                                            if (startLocationText == null || startLocationText.length() < 1) {
                                                Toast.makeText(activity, "Please select start location", Toast.LENGTH_SHORT).show();
                                                Animation shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
                                                ((Home) activity).getStartLocation().startAnimation(shake);
                                                return;
                                            }

                                            customView = vi.inflate(R.layout.booking_dialog, null);
                                            final AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                                                    .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                        @Override
                                                        public void onCancel(DialogInterface dialog) {
                                                        }
                                                    })
                                                    .setView(customView)
                                                    .create();
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.show();

                                            customView.findViewById(R.id.confirm_cab).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    zProgressDialog = ProgressDialog.show(activity, null, "Booking your request. Please wait!!!");
                                                    String couponCode = null;
                                                    if (customView != null)
                                                        couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();
                                                    UploadManager.cabBookingRequest(cab.getType(), "", cab.getSubType()+"", ((Home) activity).getLatitudeStart(), ((Home) activity).getLongitudeStart(), startLocationText, endLocationText, ((Home) activity).getLatitudeEnd(), ((Home) activity).getLongitudeEnd(), couponCode, "", cab.getDisplayName());
                                                    dialog.dismiss();
                                                }
                                            });

                                            customView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                }
                                            });
                                        }

                                    } else if (cab.getType() == CommonLib.TYPE_MEGA) {
                                        if (activity instanceof Home) {
                                            final String startLocationText = ((Home) activity).getStartLocation().getText().toString();
                                            final String endLocationText = ((Home) activity).getDropLocation().getText().toString();

                                            if (startLocationText == null || startLocationText.length() < 1) {
                                                Toast.makeText(activity, "Please select start location", Toast.LENGTH_SHORT).show();
                                                Animation shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
                                                ((Home) activity).getStartLocation().startAnimation(shake);
                                                return;
                                            }

                                            if (endLocationText == null || endLocationText.length() < 1) {
                                                Toast.makeText(activity, "Please select drop location", Toast.LENGTH_SHORT).show();
                                                Animation shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
                                                ((Home) activity).getDropLocation().startAnimation(shake);
                                                return;
                                            }


                                            customView = vi.inflate(R.layout.booking_dialog, null);
                                            final AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                                                    .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                        @Override
                                                        public void onCancel(DialogInterface dialog) {
                                                        }
                                                    })
                                                    .setView(customView)
                                                    .create();
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.show();

                                            customView.findViewById(R.id.confirm_cab).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    zProgressDialog = ProgressDialog.show(activity, null, "Booking your request. Please wait!!!");
                                                    String couponCode = null;
                                                    if (customView != null)
                                                        couponCode = ((TextView) customView.findViewById(R.id.coupon_code)).getText().toString();
                                                    Calendar calender = Calendar.getInstance();
                                                    calender.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 17));

                                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    String formattedDate = dateFormat.format(calender.getTime());
                                                    String[] args = formattedDate.split(" ");
                                                    if (args.length == 2) {
                                                        formattedDate = args[0] + "T" + args[1];
                                                    }
                                                    UploadManager.cabBookingRequest(cab.getType(), "", cab.getProductId(), ((Home) activity).getLatitudeStart(), ((Home) activity).getLongitudeStart(), startLocationText, endLocationText, ((Home) activity).getLatitudeEnd(), ((Home) activity).getLongitudeEnd(), couponCode, formattedDate, cab.getDisplayName());
                                                    dialog.dismiss();
                                                }
                                            });

                                            customView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    Intent intent = new Intent(activity, SplashScreen.class);
                                    Toast.makeText(activity, "Please login to continue", Toast.LENGTH_SHORT).show();
                                    intent.putExtra("insideApp", true);
                                    startActivity(intent);
                                }
                            }
                        });
                    }

                }
            });

            return convertView;
        }

        public Filter getFilter() {
            return mFilter;
        }
        private class ItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String filterString = constraint.toString().toLowerCase();

                FilterResults results = new FilterResults();

                final List<CabDetails> list = wishes;

                int count = list.size();
                final ArrayList<CabDetails> nlist = new ArrayList<CabDetails>(count);

                CabDetails filterableCab ;

                for (int i = 0; i < count; i++) {
                    filterableCab = list.get(i);
                    if(filterString.toLowerCase().matches("all")){
                        nlist.add(filterableCab);
                    }
                    else if (getCabSubType(filterableCab.getSubType()).toLowerCase().contains(filterString)) {
                        nlist.add(filterableCab);
                    }
                }

                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<CabDetails>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    public String getCabSubType(int subType){
        if((subType == CommonLib.CAB_COMPACT) || (subType== CommonLib.CAB_SEDAN) || (subType== CommonLib.CAB_LUXURY ) || (subType == CommonLib.SUV)){
            return "cab";
        }else if(subType== CommonLib.BIKE){
            return "bike";
        }else
            return "auto";
    }

    public static final int SORT_BY_PRICE = 101;
    public static final int SORT_BY_ARRIVAL_TIME = 102;
    public static final int SORT_BY_RECOMMENDED = 103;


    public void sort(int type) {
        switch (type) {

            case SORT_BY_ARRIVAL_TIME:
                if (((Home) activity).getWishes() != null) {
                    Collections.sort(((Home) activity).getWishes(), new Comparator<CabDetails>() {
                        @Override
                        public int compare(CabDetails lhs, CabDetails rhs) {
                            return (int) (lhs.getEstimatedTimeOfArrival() - rhs.getEstimatedTimeOfArrival());
                        }
                    });
                    mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet, ((Home) activity).getWishes(), zapp);
                    mListView.setAdapter(mAdapter);
                    returnFilter(((Home)activity).showView);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case SORT_BY_PRICE:
                if (((Home) activity).getWishes() != null) {
                    Collections.sort(((Home) activity).getWishes(), new Comparator<CabDetails>() {
                        @Override
                        public int compare(CabDetails lhs, CabDetails rhs) {

                            if (lhs.getPriceEstimate() != null && rhs.getPriceEstimate() != null)
                                return (lhs.getPriceEstimate().compareTo(rhs.getPriceEstimate()));
                            else if (lhs.getCostPerDistance() != 0 && rhs.getCostPerDistance() != 0) {
                                return (int) (lhs.getCostPerDistance() - rhs.getCostPerDistance());
                            } else {
                                return (int) (lhs.getBase() - rhs.getBase());
                            }
                        }
                    });
                    mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet, ((Home) activity).getWishes(), zapp);
                    mListView.setAdapter(mAdapter);
                    returnFilter(((Home)activity).showView);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case SORT_BY_RECOMMENDED:
                if (((Home) activity).getWishes() != null) {
                    Collections.sort(((Home) activity).getWishes(), new Comparator<CabDetails>() {
                        @Override
                        public int compare(CabDetails lhs, CabDetails rhs) {
                            return (int) (lhs.getIsRecommended() - rhs.getIsRecommended());
                        }
                    });
                    mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet, ((Home) activity).getWishes(), zapp);
                    mListView.setAdapter(mAdapter);
                    returnFilter(((Home)activity).showView);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

}