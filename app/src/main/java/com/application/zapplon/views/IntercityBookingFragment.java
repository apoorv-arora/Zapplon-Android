package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.TaxiBookings;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;

import java.text.DecimalFormat;

public class IntercityBookingFragment extends Fragment  implements UploadManagerCallback {

    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private Activity mContext;
    private boolean destroyed = false;
    TaxiBookings bookings;
    private ProgressDialog zProgressDialog;

    public static IntercityBookingFragment newInstance(Bundle bundle) {
        IntercityBookingFragment fragment = new IntercityBookingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intercity_bookings, container, false);
        (view.findViewById(R.id.root)).setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return view;
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
        bookings = null;
        if(getArguments() != null && getArguments().containsKey("booking_details")){
            bookings = (TaxiBookings) getArguments().getSerializable("booking_details");
        }

        if(bookings != null){
            if(bookings.getStatus() == CommonLib.TRACK_STAGE_CALL_DRIVER){

                getView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);

                if(bookings.getPaymentStatus() == CommonLib.INTERCITY_PAYMENT_DONE )
                    ((TextView) getView.findViewById(R.id.status)).setText("Payment successful");
                else if(bookings.getPaymentStatus() == CommonLib.INTERCITY_PAYMENT_PENDING )
                    ((TextView) getView.findViewById(R.id.status)).setText("Payment pending");
                else if(bookings.getPaymentStatus() == CommonLib.INTERCITY_PAYMENT_FAILED )
                    ((TextView) getView.findViewById(R.id.status)).setText("Payment failed");

            } else {
                getView.findViewById(R.id.cancel).setVisibility(View.GONE);
                ((TextView) getView.findViewById(R.id.status)).setText("Booking failed");
            }
        }else{
            getView.findViewById(R.id.cancel).setVisibility(View.GONE);
            ((TextView) getView.findViewById(R.id.status)).setText("Booking failed");
        }


        ((ImageView) (ImageView) getView.findViewById(R.id.cab_icon2)).setImageBitmap(CommonLib.getBitmap(activity, CommonLib.getBrandBitmap(bookings.getType()), width, width));

        ((TextView) getView.findViewById(R.id.start_location)).setText(bookings.getFromCity());
        ((TextView) getView.findViewById(R.id.drop_location)).setText(bookings.getToCity());
        ((TextView) getView.findViewById(R.id.pickup_timer)).setText(CommonLib.getFormattedDate(bookings.getStartDate(), true));

        if (bookings.getReturnDate() == 0) {
            getView.findViewById(R.id.drop_date_container).setVisibility(View.GONE);
            getView.findViewById(R.id.drop_date_label).setVisibility(View.GONE);
        } else {
            ((TextView) getView.findViewById(R.id.drop_timer)).setText(CommonLib.getFormattedDate(bookings.getReturnDate(), false));
            getView.findViewById(R.id.drop_date_container).setVisibility(View.VISIBLE);
            getView.findViewById(R.id.drop_date_label).setVisibility(View.VISIBLE);
        }

        TextView display_name_2 = (TextView) getView.findViewById(R.id.display_name2);
//        display_name_2.setText(bookings.get.getDisplayName());

        String subType = "";
        if (bookings.getSubType() == CommonLib.CAB_SEDAN) {
            subType = getResources().getString(R.string.sedan);
        } else if (bookings.getSubType() == CommonLib.CAB_COMPACT) {
            subType = getResources().getString(R.string.compact);
        } else if (bookings.getSubType() == CommonLib.CAB_LUXURY) {
            subType = getResources().getString(R.string.luxury);
        } else if (bookings.getSubType() == CommonLib.SUV) {
            subType = getResources().getString(R.string.suv);
        } else if (bookings.getSubType() == CommonLib.TEMPO) {
            subType = getResources().getString(R.string.tempo);
        }
        TextView cab_type_2 = (TextView) getView.findViewById(R.id.cab_type2);
        cab_type_2.setText(subType);


        TextView time_2 = (TextView) getView.findViewById(R.id.time2);
//        if (bookings.getCapacity() > 1)
//            time_2.setText(cab.getCapacity() + " SEATS");
//        else
//            time_2.setText(cab.getCapacity() + " SEATS");

        final DecimalFormat format = new DecimalFormat("0.#");
        TextView baseFare_2 = (TextView) getView.findViewById(R.id.travel_cost);
        baseFare_2.setText("₹ " + format.format(bookings.getAmount()));

        TextView rate_per_km_2 = (TextView) getView.findViewById(R.id.rate_per_KM2);
//        rate_per_km_2.setText("₹ " + format.format(bookings.getCostPerDistance()));

//        ((TextView) getView.findViewById(R.id.terms_desc)).setText(bookings.getTerms());

        UploadManager.addCallback(this);
        setupListener();
    }

    public void setupListener(){
        getView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View customView = vi.inflate(R.layout.cancel_dialog, null);
                final AlertDialog dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
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

                if (reason != null) {
                    final String[] reason_arr = reason.split(",");

                    for(int i=0; i<reason_arr.length; i++) {
                        RadioButton radioButton = new RadioButton(mContext);
                        radioButton.setWidth(width);
                        radioButton.setText(reason_arr[i]);
                        radioButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                customView.findViewById(R.id.reason_text).setVisibility(View.GONE);
                                CommonLib.hideKeyBoard(mContext, customView.findViewById(R.id.reason_text));
                            }
                        });
                        radioGroup.addView(radioButton);
                    }
                }
                customView.findViewById(R.id.radio_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customView.findViewById(R.id.reason_text).setVisibility(View.VISIBLE);
                        CommonLib.showSoftKeyboard(mContext, customView.findViewById(R.id.reason_text));
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
                            zProgressDialog =  ProgressDialog.show(activity, null, "Cancelling your ride. Please wait!!!");
                            UploadManager.intercityCancellationRequest(bookings.getBookingCode(), cancel_reason);
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
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.INTERCITY_CANCELLATION_REQUEST) {
            if(status) {
                Toast.makeText(mContext,"Cab has been cancelled",Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(mContext,"Invalid request",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
