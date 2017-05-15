package com.application.zapplon.views;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.City;
import com.application.zapplon.data.IntercityCab;
import com.application.zapplon.utils.CommonLib;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by apoorvarora on 24/09/16.
 */
public class IntercityBookingActivity extends ActionBarActivity {

    private ZApplication zapp;
    private SharedPreferences prefs;

    public IntercityListingFragment intercityListFragment;
    public PaymentFragment intercityPaymentFragment;
    public ArrayList<IntercityCab> wishes;
    LayoutInflater inflater;
    View actionBarCustomView;
    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_container);
        getWindow().setBackgroundDrawable(null);
        inflater = LayoutInflater.from(this);
        zapp = (ZApplication) getApplication();
        prefs = getSharedPreferences("application_settings", 0);
        width = getWindowManager().getDefaultDisplay().getWidth();

        setupActionBar();

        intercityListFragment = IntercityListingFragment.newInstance(getIntent().getExtras());
        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, intercityListFragment, "intercityList")
                .commit();

        getSupportFragmentManager().addOnBackStackChangedListener(getListner());
    }

    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBarCustomView = inflater.inflate(R.layout.intercity_action_bar, null);

        TextView sortItem = (TextView)actionBarCustomView.findViewById(R.id.sorting_type);
        sortItem.setText("PRICE");

        actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.VISIBLE);

        actionBarCustomView.findViewById(R.id.sort_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sort by relevance or price
                View customView = inflater.inflate(R.layout.sort_dialog, null);
                final AlertDialog dialog = new AlertDialog.Builder(IntercityBookingActivity.this, AlertDialog.THEME_HOLO_LIGHT)
                        .setCancelable(true)
                        .setView(customView)
                        .create();

                customView.findViewById(R.id.amount).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (intercityListFragment != null) {
                            intercityListFragment.sort(IntercityListingFragment.SORT_BY_PRICE);
                            TextView sort_type = (TextView) actionBarCustomView.findViewById(R.id.sorting_type);
                            sort_type.setText("PRICE");
                        }
                        dialog.dismiss();
                    }
                });
                customView.findViewById(R.id.arrival_time).setVisibility(View.GONE);
                customView.findViewById(R.id.recommended).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (intercityListFragment != null) {
                            intercityListFragment.sort(IntercityListingFragment.SORT_BY_RECOMMENDED);
                            TextView sort_type = (TextView) actionBarCustomView.findViewById(R.id.sorting_type);
                            sort_type.setText("RECOMMENDATION");
                        }
                        dialog.dismiss();
                    }
                });

                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });

        actionBarCustomView.findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        actionBarCustomView.findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // user handle
        TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);
        title.setPadding(width / 40, 0, width / 40, 0);

        if(getIntent() != null){
            Bundle bundle = getIntent().getExtras();
            String fromDate = bundle.getString("fromDate");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(fromDate));
            fromDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR);

            if(bundle.containsKey("toDate")) {
                String toDate = bundle.getString("toDate");
                calendar.setTimeInMillis(Long.parseLong(toDate));
                toDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR);

                title.setText(((City)bundle.getSerializable("fromCity")).getName() + " to " + ((City)bundle.getSerializable("toCity")).getName() + " on " + fromDate + " till " + toDate);
            } else {
                title.setText(((City)bundle.getSerializable("fromCity")).getName() + " to " + ((City)bundle.getSerializable("toCity")).getName() + " on " + fromDate);
            }
        } else
            title.setText(getResources().getString(R.string.intercity));

        actionBar.setCustomView(actionBarCustomView);

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

    public ArrayList<IntercityCab> getWishes() {
        return wishes;
    }

    public void setWishes(ArrayList<IntercityCab> wishes) {
        this.wishes = wishes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonLib.PAYMENT_REQUEST_CODE_PAYMENT_COLLECTOR || requestCode == CommonLib.PAYMENT_REQUEST_CODE_THIRD_PARTY_COLLECTOR) {
            if(intercityPaymentFragment != null) {
                intercityPaymentFragment.onResume();
                intercityPaymentFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private android.support.v4.app.FragmentManager.OnBackStackChangedListener getListner() {

        android.support.v4.app.FragmentManager.OnBackStackChangedListener result = new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    int backStackEntryCount = manager.getBackStackEntryCount();
                    if (manager.getFragments() != null && (backStackEntryCount - 1) >= 0 && manager.getFragments().size() > backStackEntryCount - 1) {
                        if (intercityPaymentFragment != null) {
                            intercityPaymentFragment.onResume();
                            if (actionBarCustomView != null) {
                                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
                            }
                        } else if (manager.getFragments() != null && backStackEntryCount == 0) {
                            if (intercityListFragment != null) {
                                intercityListFragment.onResume();
                                if (actionBarCustomView != null) {
                                    actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.VISIBLE);
                                }
                            } else {
                                // listing fragment does not exist, so recreate it
                                intercityListFragment = IntercityListingFragment.newInstance(getIntent().getExtras());
                                getFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.fragment_container, intercityListFragment, "home")
                                        .commit();

                                TextView sortItem = (TextView)actionBarCustomView.findViewById(R.id.sorting_type);
                                sortItem.setText("RECOMMENDATION");

                                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.VISIBLE);

                                // user handle
                                TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);
                                title.setPadding(0, 0, width / 40, 0);

                                if(getIntent() != null){
                                    Bundle bundle = getIntent().getExtras();
                                    String fromDate = bundle.getString("fromDate");
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(Long.parseLong(fromDate));
                                    fromDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR);

                                    if(bundle.containsKey("toDate")) {
                                        String toDate = bundle.getString("toDate");
                                        calendar.setTimeInMillis(Long.parseLong(toDate));
                                        toDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR);

                                        title.setText(((City)bundle.getSerializable("fromCity")).getName() + " to " + ((City)bundle.getSerializable("toCity")).getName() + " on " + fromDate + " till " + toDate);
                                    } else {
                                        title.setText(((City)bundle.getSerializable("fromCity")).getName() + " to " + ((City)bundle.getSerializable("toCity")).getName() + " on " + fromDate);
                                    }
                                } else
                                    title.setText(getResources().getString(R.string.intercity));
                            }
                        }
                    }
                }
            }

        };

        return result;
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();

            if (getFragmentManager().getBackStackEntryCount() == 1  && actionBarCustomView != null) {
                actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.VISIBLE);
            }

        } else {
            super.onBackPressed();
        }
    }

    public void nextFragment(IntercityCab cabToBook, City fromCity, City toCity, String fromDate, String toDate) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("cabToBook", cabToBook);
        bundle.putSerializable("fromCity", fromCity);
        bundle.putSerializable("toCity", toCity);
        bundle.putSerializable("fromDate", fromDate);
        bundle.putSerializable("toDate", toDate);

        if(actionBarCustomView != null) {
            actionBarCustomView.findViewById(R.id.sort_bar).setVisibility(View.GONE);
        }

        intercityPaymentFragment = PaymentFragment.newInstance(bundle);
        android.app.FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.add(R.id.container, intercityPaymentFragment, "intercityPaymentFragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

}
