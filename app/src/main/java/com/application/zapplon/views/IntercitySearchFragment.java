package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.City;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by apoorvarora on 22/09/16.
 */
public class IntercitySearchFragment extends Fragment {

    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private Context mContext;
    private SharedPreferences prefs;
    private int width;
    private boolean destroyed = false;

    private int starthour,startmin,startDay,startMonth,startyear;
    private int endDay,endMonth,endyear;
    private int journeyType = 0;
    private Bundle bundle;
    private TextView fromCityTv, toCityTv,fromDate,toDate;
    private City fromCity, toCity;
    private View submit;
    private LayoutInflater inflater;
    private View pickupTimerContainer, dropTimerContainer;
    private AlertDialog dialog;

    protected static final int ROUND_TRIP_FRAGMENT = 1;
    protected static final int ONE_WAY_FRAGMENT = 2;

    private int type = ROUND_TRIP_FRAGMENT;
    private boolean uxFlow = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.intercity_home_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = activity.getWindowManager().getDefaultDisplay().getWidth();
        mContext = activity;
        inflater =  LayoutInflater.from(activity);

        if(getArguments() != null && getArguments().containsKey("type")) {
            type = getArguments().getInt("type");
        }

        pickupTimerContainer = getView.findViewById(R.id.pickup_timer_container);
        dropTimerContainer = getView.findViewById(R.id.drop_date_container);
        submit = getView.findViewById(R.id.search_container);
        fromCityTv = (TextView) getView.findViewById(R.id.start_location);
        toCityTv = (TextView) getView.findViewById(R.id.drop_location);
        fromDate = (TextView) getView.findViewById(R.id.pickup_timer);
        toDate = (TextView) getView.findViewById(R.id.drop_timer);

        // initialize the city
        String locationString = zapp.getLocationString();

        if(zapp.cities != null && zapp.cities.size() > 0) {
            for (City city : zapp.cities) {
                if (locationString.contains(city.getName())) {
                    fromCityTv.setText(city.getName());
                    fromCity = city;
                    break;
                }
            }
            if(fromCityTv.getText().toString().length() <= 0) {
                fromCityTv.setText(zapp.cities.get(0).getName());
                fromCity = zapp.cities.get(0);
            }
        }

        switch(type) {
            case ROUND_TRIP_FRAGMENT:
                dropTimerContainer.setVisibility(View.VISIBLE);
                getView.findViewById(R.id.drop_date_label).setVisibility(View.VISIBLE);
                break;
            case ONE_WAY_FRAGMENT:
                dropTimerContainer.setVisibility(View.GONE);
                getView.findViewById(R.id.drop_date_label).setVisibility(View.GONE);
                break;
        }

        setListeners();

    }

    private void setListeners() {

        fromCityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View customView = inflater.inflate(R.layout.select_dialog, null);
                dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                        .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        })
                        .setView(customView)
                        .create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                final RadioGroup radioGroup = (RadioGroup)customView.findViewById(R.id.radio_group);
                if(zapp.cities != null) {

                    for (final City city : zapp.cities) {
                        RadioButton radioButton = new RadioButton(activity);
                        radioButton.setText(city.getName());
                        radioButton.setWidth(width);
                        radioButton.setPadding(width / 80, width / 80, width / 80, width / 80);
                        radioButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String fromCityText = "";
                                fromCityText = city.getName();
                                fromCityTv.setText(fromCityText);
                                fromCity = city;
                                dialog.dismiss();
                            }
                        });
                        radioGroup.addView(radioButton);
                    }
                }
            }
        });

        toCityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View customView = inflater.inflate(R.layout.select_dialog, null);
                dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                        .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        })
                        .setView(customView)
                        .create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();



                final RadioGroup radioGroup = (RadioGroup)customView.findViewById(R.id.radio_group);
                if(zapp.cities != null) {

                    for (final City city : zapp.cities) {
                        RadioButton radioButton = new RadioButton(activity);
                        radioButton.setText(city.getName());
                        radioButton.setWidth(width);
                        radioButton.setPadding(width / 80, width / 80, width / 80, width / 80);
                        radioButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String fromCityText = "";
                                fromCityText = city.getName();
                                toCityTv.setText(fromCityText);
                                toCity = city;
                                dialog.dismiss();
                                // trigger the UX
                                if (fromCityTv.getText().toString().length() > 0) {
                                    if (fromDate.getText().toString().length() < 1 && toDate.getText().toString().length() < 1 ) {
                                        uxFlow = true;
                                        pickupTimerContainer.performClick();
                                    }
                                }
                            }
                        });
                        radioGroup.addView(radioButton);
                    }
                }

            }
        });

        pickupTimerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker(true);
            }
        });

        dropTimerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker(false);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fromCityStr = fromCityTv.getText().toString();
                String toCityStr = toCityTv.getText().toString();

                String fromDateStr = fromDate.getText().toString();
                String toDateStr = toDate.getText().toString();

                // random checks everyone does, don't know why!
                if (type == ROUND_TRIP_FRAGMENT) {
                   if(fromCityStr.length() > 0) {
                       if(toCityStr.length() > 0) {
                           if(fromCityStr.equalsIgnoreCase(toCityStr)) {
                               Toast.makeText(activity, activity.getResources().getString(R.string.from_to_same), Toast.LENGTH_LONG).show();
                               return;
                           }

                           if (toDateStr.length() > 0) {
                               if (fromDateStr.length() > 0) {
                                   if (toDateStr.length() > 0) {
                                       // climbed mt. eve. this guy will rock!
                                   } else {
                                       Toast.makeText(activity, activity.getResources().getString(R.string.drop_label_toast), Toast.LENGTH_LONG).show();
                                       return;
                                   }
                               } else {
                                   Toast.makeText(activity, activity.getResources().getString(R.string.pickup_label), Toast.LENGTH_LONG).show();
                                   return;
                               }
                           } else {
                               Toast.makeText(activity, activity.getResources().getString(R.string.location_label), Toast.LENGTH_LONG).show();
                               return;
                           }
                       } else {
                           Toast.makeText(activity, activity.getResources().getString(R.string.drop_point_toast), Toast.LENGTH_LONG).show();
                           return;
                       }
                   } else {
                       Toast.makeText(activity, activity.getResources().getString(R.string.pickup_point_toast), Toast.LENGTH_LONG).show();
                       return;
                   }
                } else if (type == ONE_WAY_FRAGMENT) {
                    if(fromCityStr.length() > 0) {
                        if(toCityStr.length() > 0) {
                            if (toDateStr.length() > 0) {
                                if(fromCityStr.equalsIgnoreCase(toCityStr)) {
                                    Toast.makeText(activity, activity.getResources().getString(R.string.from_to_same), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (fromDateStr.length() > 0) {
                                    // climbed k2. this guy will pop!
                                } else {
                                    Toast.makeText(activity, activity.getResources().getString(R.string.pickup_label), Toast.LENGTH_LONG).show();
                                    return;
                                }
                            } else {
                                Toast.makeText(activity, activity.getResources().getString(R.string.location_label), Toast.LENGTH_LONG).show();
                                return;
                            }
                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.drop_point_toast), Toast.LENGTH_LONG).show();
                            return;
                        }
                    } else {
                        Toast.makeText(activity, activity.getResources().getString(R.string.pickup_point_toast), Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                Intent intent = new Intent(activity, IntercityBookingActivity.class);

                Calendar calendar = Calendar.getInstance();
                calendar.set(startyear,startMonth-1,startDay,starthour,startmin);

                intent.putExtra("toCity", toCity);
                intent.putExtra("fromCity", fromCity);

                intent.putExtra("fromDate", calendar.getTimeInMillis()+"");

                if(!(toDate.getText().toString()).equalsIgnoreCase("Return date")){
                    calendar.set(endyear,endMonth-1,endDay,0,0);
                    intent.putExtra("toDate",calendar.getTimeInMillis()+"");
                }
                else
                    intent.putExtra("toDate","0");

                activity.startActivity(intent);
            }
        });

    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    public void setType(int type) {
        this.type = type;
    }

    private void showDateTimePicker(final boolean showTimePicker) {
        final View customView = inflater.inflate(R.layout.date_time_picker, null);

        // Define your date pickers
        final DatePicker dpStartDate = (DatePicker) customView.findViewById(R.id.date_picker);
        final TimePicker dpStartTime = (TimePicker) customView.findViewById(R.id.time_picker);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        if(showTimePicker && startDay == 0 && startMonth == 0 && startyear == 0) {
            dpStartDate.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dpStartTime.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            dpStartTime.setCurrentMinute(calendar.get(Calendar.MINUTE));
            ((TextView)customView.findViewById(R.id.dp_title)).setText(getResources().getString(R.string.pickup_date));
        } else {
            if(startDay != 0 && startMonth != 0 && startyear != 0) {
                calendar.set(Calendar.DAY_OF_MONTH, startDay);
                calendar.set(Calendar.MONTH, startMonth - 1);
                calendar.set(Calendar.YEAR, startyear);
            }
            dpStartDate.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            ((TextView)customView.findViewById(R.id.dp_title)).setText(getResources().getString(R.string.drop_date));
        }

        // Build the dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(customView); // Set the view of the dialog to your custom layout
        builder.setPositiveButton(mContext.getResources().getString(R.string.submit), null);
        builder.setNegativeButton(mContext.getResources().getString(R.string.cancel), null);

        final AlertDialog dialog = builder.show();
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(customView.findViewById(R.id.dp).getVisibility() == View.VISIBLE) {
                    int startYear = dpStartDate.getYear();
                    int startMonthOfYear = dpStartDate.getMonth() + 1;
                    int startDayOfMonth = dpStartDate.getDayOfMonth();

                    SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM yy", Locale.getDefault());
                    GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());

                    if(showTimePicker) {
                        startDay = startDayOfMonth;
                        startMonth = startMonthOfYear;
                        startyear = startYear;
                        String date = startDayOfMonth + "/" + (startMonthOfYear) + "/" + startYear;

                        //show time picker now
                        customView.findViewById(R.id.dp).setVisibility(View.GONE);
                        customView.findViewById(R.id.tp).setVisibility(View.VISIBLE);
                    } else {
                        endDay = startDayOfMonth;
                        endMonth = startMonthOfYear;
                        endyear = startYear;

                        String date = startDayOfMonth + "/" + (startMonthOfYear) + "/" + startYear;

                        //show time picker now
                        toDate.setText(date);
                        dialog.dismiss();
                    }
                } else if(customView.findViewById(R.id.dp).getVisibility() == View.GONE) {
                    int startHour		=	dpStartTime.getCurrentHour();
                    int startMinute 	=	dpStartTime.getCurrentMinute();

                    starthour = startHour;
                    startmin = startMinute;

                    Time mTime = new Time();
                    mTime.set(0, startMinute, startHour, 1, 1, 1);
                    String startTime = mTime.format("%I:%M %P");
                    String date = startDay + "/" + (startMonth) + "/" + startyear;

                    fromDate.setText(date + " "+ startTime);
                    dialog.dismiss();

                    if(type == ROUND_TRIP_FRAGMENT && uxFlow && toDate.getText().toString().length() < 1) {
                        dropTimerContainer.performClick();
                        uxFlow = false;
                    }
                }
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}