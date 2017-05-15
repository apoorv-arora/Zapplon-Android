package com.application.zapplon.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.City;
import com.application.zapplon.data.IntercityCab;
import com.application.zapplon.data.TaxiBookings;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.GMapV2Direction;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.location.PolylineEncoder;
import com.application.zapplon.utils.location.Track;
import com.application.zapplon.utils.location.Trackpoint;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by dell on 02-Sep-16.
 */
public class IntercityListingFragment extends Fragment{

    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private Activity mContext;
    private boolean destroyed = false;
    private ListView mListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog zProgressDialog;
    private AsyncTask mAsyncRunning;
    private BookingAdapter mAdapter;
    private String fromDate,toDate;
    private City fromCity, toCity;
    private TaxiBookings bookings;
    private IntercityCab cabToBook;

    public static int selectedType = IntercityListingFragment.TYPE_ALL;
    public static final int TYPE_ALL = 1;
    public static final int TYPE_SEDAN = 2;
    public static final int TYPE_COMPACT = 3;
    public static final int TYPE_SUV = 4;
    public static final int TYPE_LUXURY = 5;
    public static final int TYPE_TEMPO = 6;

    public static final int SORT_BY_PRICE = 101;
    public static final int SORT_BY_RECOMMENDED = 103;

    public static LinearLayout root;

    String showView ="all";

    public static IntercityListingFragment newInstance(Bundle bundle) {
        IntercityListingFragment fragment = new IntercityListingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.intercity_list_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        mContext = activity;
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        vi = LayoutInflater.from(activity.getApplicationContext());
        cabToBook = null;
        root = (LinearLayout)getView.findViewById(R.id.root);

        swipeRefreshLayout = (SwipeRefreshLayout) getView.findViewById(R.id.content);
        mListView = (ListView) getView.findViewById(R.id.cabs_list);
        mListView.setDivider(null);
        mListView.setDividerHeight(width / 20);
        mListView.setClipToPadding(false);

        View headerView = new View(mContext);
        headerView.setMinimumHeight(width / 40);
        mListView.addHeaderView(headerView);

        fromDate = "";
        toDate = "";

        if(getArguments() != null){
            Bundle bundle = getArguments();
            fromCity = (City)bundle.getSerializable("fromCity");
            toCity = (City)bundle.getSerializable("toCity");
            fromDate = bundle.getString("fromDate");
            toDate = bundle.getString("toDate");
        }

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

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CommonLib.ZLog("onRefresh", "called");
                refreshView();
            }
        });
        refreshView();
        setListeners();

        new GetDirections(fromCity.getLatitude(), fromCity.getLongitude(), toCity.getLatitude(), toCity.getLongitude()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void setListeners() {
        getView.findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshView();
            }
        });

        getView.findViewById(R.id.all_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showView ="all";
                returnFilter(showView);

                changeViews(IntercityListingFragment.TYPE_ALL);
            }
        });

        getView.findViewById(R.id.compact_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showView ="compact";
                returnFilter(showView);

                changeViews(IntercityListingFragment.TYPE_COMPACT);
            }
        });

        getView.findViewById(R.id.sedan_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showView ="sedan";
                returnFilter(showView);

                changeViews(IntercityListingFragment.TYPE_SEDAN);
            }
        });

        getView.findViewById(R.id.suv_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showView ="suv";
                returnFilter(showView);

                changeViews(IntercityListingFragment.TYPE_SUV);
            }
        });
        getView.findViewById(R.id.luxury_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showView ="luxury";
                returnFilter(showView);

                changeViews(IntercityListingFragment.TYPE_LUXURY);
            }
        });
        getView.findViewById(R.id.tempo_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showView ="tempo";
                returnFilter(showView);

                changeViews(IntercityListingFragment.TYPE_TEMPO);
            }
        });
    }

    public void returnFilter(String s) {
        if(mAdapter!=null){
            mAdapter.getFilter().filter(s);
        }
    }

    public void refreshView() {
        mListView.setVisibility(View.VISIBLE);
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetIntercityCabs().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

    }

    private void setWishes(ArrayList<IntercityCab> wishes) {
        ((IntercityBookingActivity) activity).setWishes(wishes);
        mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet_intercity, ((IntercityBookingActivity) activity).getWishes(), zapp);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void confirmBooking(Intent data){
        zProgressDialog = ProgressDialog.show(activity, null, "Booking your request. Please wait!!!");

        if(cabToBook != null){
            if( cabToBook.getType()==CommonLib.TYPE_AHA){
                String bookingId = data.getStringExtra("bookingId");
                UploadManager.getIntercityBookings(bookingId);
            }else
                UploadManager.intercityBookingRequest(cabToBook.getType(), cabToBook.getCabType(), fromCity.getName(), toCity.getName(), cabToBook.getAdvance(), cabToBook.getFare(), fromDate, toDate, cabToBook.getDisplayName(),cabToBook.getBookingId(),"NETBANKING","REF1000", CommonLib.TYPE_PAYMENT_DONE);
        }
    }

    private class GetIntercityCabs extends AsyncTask<Object, Void, Object> {

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

                String url = CommonLib.SERVER + "intercity/availability?fromCity="+fromCity.getName()+"&toCity="+toCity.getName()+"&fromDate="+fromDate+"&toDate="+toDate;

                Object info = RequestWrapper.RequestHttp(url.toString(), RequestWrapper.Intercity_List, RequestWrapper.FAV);
                CommonLib.ZLog("url", url.toString());
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

            if (result != null && result instanceof ArrayList<?>) {
                getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                ArrayList<IntercityCab> cabList = (ArrayList<IntercityCab>) result;
                setWishes((ArrayList<IntercityCab>) result);
                if (((ArrayList<IntercityCab>) result).size() == 0) {
                    getView.findViewById(R.id.content).setVisibility(View.GONE);
                    getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                    ((TextView) getView.findViewById(R.id.empty_view_text)).setText("Nothing here yet");
                } else {
                    getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                    getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
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

    protected static class ViewHolder {
        ImageView cabIcon;
        TextView displayName;
        TextView cabType;
        TextView pricePerKm;
        TextView totalFare;
        TextView seats, seatsText;
        RelativeLayout mainLayout;
        RelativeLayout containerLayout;
    }

    int savedPosition = -1;
    private RelativeLayout clickedView;

    private class BookingAdapter extends ArrayAdapter<IntercityCab> {

        private List<IntercityCab> wishes;
        private List<IntercityCab> filteredList = null;
        private Activity mContext;
        private int width;
        private ZApplication zapp;
        private LayoutInflater vi;
        private ItemFilter mFilter = new ItemFilter();

        public BookingAdapter(Activity context, int resourceId, List<IntercityCab> wishes, ZApplication zapp) {
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.new_cab_snippet_intercity, null);
                viewHolder = new ViewHolder();
                viewHolder.mainLayout = (RelativeLayout) convertView.findViewById(R.id.new_cab_snippet_container);
                viewHolder.cabIcon = (ImageView) convertView.findViewById(R.id.cab_icon);
                viewHolder.displayName = (TextView) convertView.findViewById(R.id.display_name1);
                viewHolder.cabType = (TextView) convertView.findViewById(R.id.cab_type);
                viewHolder.totalFare = (TextView) convertView.findViewById(R.id.total_fare_1);
                viewHolder.seats = (TextView) convertView.findViewById(R.id.seats);
                viewHolder.seatsText = (TextView) convertView.findViewById(R.id.seats_text);
                viewHolder.pricePerKm = (TextView) convertView.findViewById(R.id.price_per_km);
                viewHolder.containerLayout = (RelativeLayout) convertView.findViewById(R.id.cab_details_container);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (position != savedPosition)
                viewHolder.containerLayout.removeAllViews();

            String subType = "";
            final IntercityCab cab = filteredList.get(position);

            try {
                viewHolder.cabIcon.setImageBitmap(CommonLib.getBitmap(mContext, CommonLib.getBrandBitmap(cab.getType()), width, width));
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
            if (cab.getSubType() == CommonLib.CAB_SEDAN) {
                subType = getResources().getString(R.string.sedan);
            } else if (cab.getSubType() == CommonLib.CAB_COMPACT) {
                subType = getResources().getString(R.string.compact);
            } else if (cab.getSubType() == CommonLib.CAB_LUXURY) {
                subType = getResources().getString(R.string.luxury);
            } else if (cab.getSubType() == CommonLib.SUV) {
                subType = getResources().getString(R.string.suv);
            }else if (cab.getSubType() == CommonLib.TEMPO) {
                subType = getResources().getString(R.string.tempo);
            }

            convertView.findViewById(R.id.recommended).setVisibility(View.GONE);
            viewHolder.displayName.setText(cab.getDisplayName());
            viewHolder.cabType.setText(subType);

            // total fare
            Double price = cab.getFare();
            final DecimalFormat format = new DecimalFormat("0.#");
            viewHolder.totalFare.setText("₹ " + format.format(price));

            // total price per km
            Double pricePerKm = cab.getCostPerDistance();
            viewHolder.pricePerKm.setText("₹ " + format.format(pricePerKm) + "/km");

            // capacity
            if (cab.getCapacity() > 1)
                viewHolder.seats.setText(cab.getCapacity()+"");
            else
                viewHolder.seats.setText(cab.getCapacity()+"");
            if (cab.getCapacity() > 0) {
                viewHolder.seats.setVisibility(View.VISIBLE);
                viewHolder.seatsText.setVisibility(View.VISIBLE);
            } else {
                viewHolder.seats.setVisibility(View.INVISIBLE);
                viewHolder.seatsText.setVisibility(View.INVISIBLE);
            }

            viewHolder.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (savedPosition == position) {
                        if (clickedView != null)
                            clickedView.removeAllViews();
                        savedPosition = -1;
                    } else {
                        View menuLayout = LayoutInflater.from(activity).inflate(R.layout.new_cab_snippet_intercity_detail, null);
                        if (clickedView != null)
                            clickedView.removeAllViews();
                        viewHolder.containerLayout.addView(menuLayout, 0);
                        clickedView = viewHolder.containerLayout;

                        savedPosition = position;

                        menuLayout.findViewById(R.id.recommended2).setVisibility(View.GONE);

                        TextView recommend = (TextView) menuLayout.findViewById(R.id.recommended2);

                        recommend.setVisibility(View.GONE);

                        ((ImageView) menuLayout.findViewById(R.id.cab_icon2)).setImageBitmap(CommonLib.getBitmap(mContext, CommonLib.getBrandBitmap(cab.getType()), width, width));

                        TextView display_name_2 = (TextView) menuLayout.findViewById(R.id.display_name2);
                        display_name_2.setText(viewHolder.displayName.getText());

                        // total fare
                        Double price = cab.getFare();
                        final DecimalFormat format = new DecimalFormat("0.#");
                        ((TextView) menuLayout.findViewById(R.id.total_fare_2)).setText("₹ " + format.format(price));

                        TextView cab_type_2 = (TextView) menuLayout.findViewById(R.id.cab_type2);
                        cab_type_2.setText(viewHolder.cabType.getText());

                        TextView time_2 = (TextView) menuLayout.findViewById(R.id.time2);
                        if (cab.getCapacity() > 1)
                            time_2.setText(cab.getCapacity() + " SEATS");
                        else
                            time_2.setText(cab.getCapacity() + " SEATS");

                        TextView baseFare_2 = (TextView) menuLayout.findViewById(R.id.travel_cost);
                        baseFare_2.setText("₹ " + format.format(cab.getFare()));

                        TextView rate_per_km_2 = (TextView) menuLayout.findViewById(R.id.rate_per_KM2);
                        rate_per_km_2.setText("₹ " + format.format(cab.getCostPerDistance()));

                        menuLayout.findViewById(R.id.bottom_container).setVisibility(View.GONE);
                        menuLayout.findViewById(R.id.away).setVisibility(View.GONE);

                        menuLayout.findViewById(R.id.book_now).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cabToBook = cab;
                                ((IntercityBookingActivity)activity).nextFragment(cab, fromCity, toCity, fromDate, toDate);
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

                final List<IntercityCab> list = wishes;

                int count = list.size();
                final ArrayList<IntercityCab> nlist = new ArrayList<IntercityCab>(count);

                IntercityCab filterableCab;

                for (int i = 0; i < count; i++) {
                    filterableCab = list.get(i);
                    if (filterString.toLowerCase().matches("all")) {
                        nlist.add(filterableCab);
                    } else if (getCabSubType(filterableCab.getSubType()).toLowerCase().contains(filterString)) {
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
                filteredList = (ArrayList<IntercityCab>) results.values;
                notifyDataSetChanged();
            }
        }
    }


    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    public void changeViews (int type) {
        if(destroyed)
            return;

        switch(type) {
            case TYPE_ALL:
                getView.findViewById(R.id.all_selector).setVisibility(View.VISIBLE);
                ((TextView)getView.findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)getView.findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.active_color));

                getView.findViewById(R.id.compact_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.compact_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.compact_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.sedan_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.sedan_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.sedan_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.suv_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.suv_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.suv_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.luxury_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.luxury_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.luxury_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.tempo_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.tempo_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.tempo_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;
            case TYPE_COMPACT:
                getView.findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.compact_selector).setVisibility(View.VISIBLE);
                ((TextView)getView.findViewById(R.id.compact_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)getView.findViewById(R.id.compact_text)).setTextColor(getResources().getColor(R.color.active_color));

                getView.findViewById(R.id.sedan_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.sedan_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.sedan_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.suv_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.suv_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.suv_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.luxury_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.luxury_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.luxury_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.tempo_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.tempo_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.tempo_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;
            case TYPE_SEDAN:
                getView.findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.compact_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.compact_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.compact_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.sedan_selector).setVisibility(View.VISIBLE);
                ((TextView)getView.findViewById(R.id.sedan_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)getView.findViewById(R.id.sedan_text)).setTextColor(getResources().getColor(R.color.active_color));

                getView.findViewById(R.id.suv_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.suv_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.suv_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.luxury_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.luxury_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.luxury_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.tempo_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.tempo_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.tempo_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;
            case TYPE_SUV:
                getView.findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.compact_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.compact_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.compact_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.sedan_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.sedan_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.sedan_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.suv_selector).setVisibility(View.VISIBLE);
                ((TextView)getView.findViewById(R.id.suv_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)getView.findViewById(R.id.suv_text)).setTextColor(getResources().getColor(R.color.active_color));

                getView.findViewById(R.id.luxury_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.luxury_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.luxury_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.tempo_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.tempo_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.tempo_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;
            case TYPE_LUXURY:
                getView.findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.compact_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.compact_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.compact_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.sedan_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.sedan_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.sedan_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.suv_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.suv_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.suv_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.luxury_selector).setVisibility(View.VISIBLE);
                ((TextView)getView.findViewById(R.id.luxury_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)getView.findViewById(R.id.luxury_text)).setTextColor(getResources().getColor(R.color.active_color));

                getView.findViewById(R.id.tempo_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.tempo_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.tempo_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;
            case TYPE_TEMPO:
                getView.findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.compact_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.compact_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.compact_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.sedan_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.sedan_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.sedan_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.suv_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.suv_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.suv_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.luxury_selector).setVisibility(View.INVISIBLE);
                ((TextView)getView.findViewById(R.id.luxury_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView)getView.findViewById(R.id.luxury_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                getView.findViewById(R.id.tempo_selector).setVisibility(View.VISIBLE);
                ((TextView)getView.findViewById(R.id.tempo_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView)getView.findViewById(R.id.tempo_text)).setTextColor(getResources().getColor(R.color.active_color));

                break;
        }
    }

    public String getCabSubType(int subType){
        if(subType == CommonLib.CAB_COMPACT){
            return "compact";
        } else if(subType== CommonLib.CAB_SEDAN){
            return "sedan";
        } else if(subType== CommonLib.SUV){
            return "suv";
        } else if(subType== CommonLib.CAB_LUXURY){
            return "luxury";
        } else if(subType== CommonLib.TEMPO){
            return "tempo";
        } else
            return "all";
    }

    public void sort(int type) {
        switch (type) {

            case SORT_BY_PRICE:
                if (((IntercityBookingActivity) activity).getWishes() != null) {
                    Collections.sort(((IntercityBookingActivity) activity).getWishes(), new Comparator<IntercityCab>() {
                        @Override
                        public int compare(IntercityCab lhs, IntercityCab rhs) {

                            if (lhs.getFare() != 0 && rhs.getFare() != 0)
                                return (int) (lhs.getFare() - rhs.getFare());
                            else if (lhs.getCostPerDistance() != 0 && rhs.getCostPerDistance() != 0) {
                                return (int) (lhs.getCostPerDistance() - rhs.getCostPerDistance());
                            } else {
                                return (int) (lhs.getBasePrice() - rhs.getBasePrice());
                            }
                        }
                    });
                    mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet_intercity, ((IntercityBookingActivity) activity).getWishes(), zapp);
                    mListView.setAdapter(mAdapter);
                    returnFilter(showView);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case SORT_BY_RECOMMENDED:
                if (((IntercityBookingActivity) activity).getWishes() != null) {
                    Collections.sort(((IntercityBookingActivity) activity).getWishes(), new Comparator<IntercityCab>() {
                        @Override
                        public int compare(IntercityCab lhs, IntercityCab rhs) {
                            return (int) (lhs.getBasePrice() - rhs.getBasePrice());
                        }
                    });
                    mAdapter = new BookingAdapter(mContext, R.layout.new_cab_snippet_intercity, ((IntercityBookingActivity) activity).getWishes(), zapp);
                    mListView.setAdapter(mAdapter);
                    returnFilter(showView);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private class GetDirections extends AsyncTask<Object, Void, Document> {

        private double latitudeStart, longitudeStart, latitudeEnd, longitudeEnd;

        public GetDirections(double latitudeStart, double longitudeStart, double latitudeEnd, double longitudeEnd) {
            this.latitudeStart = latitudeStart;
            this.longitudeStart = longitudeStart;
            this.latitudeEnd = latitudeEnd;
            this.longitudeEnd = longitudeEnd;
        }

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
                GMapV2Direction md = new GMapV2Direction();
                ArrayList<LatLng> directionPoints = md.getDirection(doc);

                // run polyline encoder
                Track tracker = new Track();
                int i=1;
                for(LatLng point:directionPoints) {
                    if( i++ / 200 == 0 ) {
                        Trackpoint trackPt = new Trackpoint(point.latitude, point.longitude);
                        tracker.addTrackpoint(trackPt);
                    }
                }
                HashMap<String, String> hashMap = PolylineEncoder.createEncodings(tracker, 17, 1);
                String encodedPoints = hashMap.get("encodedPoints");

                String mapUrl = "http://maps.google.com/maps/api/staticmap?markers=icon:http://i.imgur.com/Kn5aI2q.png?1|scale:2|" + latitudeStart + "," + longitudeStart+"&markers=icon:http://i.imgur.com/Kn5aI2q.png?1|scale:2|"+latitudeEnd + "," + longitudeEnd + "&path=color:0x0000ff|weight:3"+"&zoom=8&size="+width+"x"+height+"&sensor=false&enc:"+encodedPoints;
                if(!destroyed)
                    setImageFromUrlOrDisk(mapUrl, (ImageView) getView.findViewById(R.id.map_image_holder), "static_map", width, height, false);
            }
        }
    }

    private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
                                       int height, boolean useDiskCache) {

        if (cancelPotentialWork(url, imageView)) {

            GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), zapp.cache.get(url + type), task);
            imageView.setImageDrawable(asyncDrawable);
            if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
            }
            if (zapp.cache.get(url + type) == null) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1L);
            } else if (imageView != null && imageView.getDrawable() != null
                    && ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
                imageView.setBackgroundResource(0);
                Bitmap blurBitmap = null;
                if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                }
            }
        }
    }

    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<GetImage> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<GetImage>(bitmapWorkerTask);
        }

        public GetImage getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public boolean cancelPotentialWork(String data, ImageView imageView) {
        final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.url;
            if (!bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    private GetImage getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class GetImage extends AsyncTask<Object, Void, Bitmap> {

        String url = "";
        private WeakReference<ImageView> imageViewReference;
        private int width;
        private int height;
        boolean useDiskCache;
        String type;
        Bitmap blurBitmap;

        public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type) {
            this.url = url;
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
            this.useDiskCache = true;// useDiskCache;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar)
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            try {

                String url2 = url + type;

                if (destroyed) {
                    return null;
                }

                if (useDiskCache) {
                    bitmap = CommonLib.getBitmapFromDisk(url2, activity.getApplicationContext());
                }

                if (bitmap == null) {
                    try {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
                        opts.inJustDecodeBounds = false;

                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        if (useDiskCache) {
                            CommonLib.writeBitmapToDisk(url2, bitmap, activity.getApplicationContext(),
                                    Bitmap.CompressFormat.JPEG);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    synchronized (zapp.cache) {
                        zapp.cache.put(url2, bitmap);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (!destroyed) {
                if (isCancelled()) {
                    bitmap = null;
                }
                if (imageViewReference != null && bitmap != null) {
                    final ImageView imageView = imageViewReference.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(CommonLib.fastBlur(bitmap, 4));
                        if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                            ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }
}
