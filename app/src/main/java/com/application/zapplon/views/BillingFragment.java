package com.application.zapplon.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.application.zapplon.R;
import com.application.zapplon.data.CabBooking;
import com.application.zapplon.data.TaxiBookings;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;

public class BillingFragment extends Fragment implements UploadManagerCallback {

    private CabBooking booking;
    private TaxiBookings intercityBooking;
    private RatingBar ratingBar;
    private Activity activity;
    private View getView;

    public static BillingFragment newInstance(Bundle bundle) {
        BillingFragment fragment = new BillingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_billing, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();

        if(getArguments() != null) {
            if(getArguments().containsKey("intracityBooking")) {
                booking = (CabBooking) getArguments().get("intracityBooking");
                setupListener();
            } else if(getArguments().containsKey("intercityBooking")) {
                intercityBooking = (TaxiBookings) getArguments().get("intercityBooking");
                setupListener();
            }
        }

        ratingBar = (RatingBar) getView.findViewById(R.id.rating);
        UploadManager.addCallback(this);
    }

    public void setupListener(){
        if(booking != null) {

            ((TextView)getView.findViewById(R.id.bill_amount)).setText(booking.getAmount()+"");
            if(booking.getPickupAddress() != null)
                ((TextView)getView.findViewById(R.id.start_location)).setText(booking.getPickupAddress());
            if(booking.getDropAddress() != null)
                ((TextView)getView.findViewById(R.id.drop_location)).setText(booking.getDropAddress());

            ((TextView)getView.findViewById(R.id.pickupTime)).setText(booking.getDropAddress());

            getView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UploadManager.updateTripRating(String.valueOf(ratingBar.getRating()),booking.getHash(), CommonLib.INTRACITY);
                }
            });

            if(booking.getStatus()==CommonLib.TRACK_STAGE_BOOKING_CANCELLED){
                ((TextView)getView.findViewById(R.id.trip_info)).setText(getResources().getString(R.string.trip_cancel));
            }else if(booking.getStatus()==CommonLib.TRACK_STAGE_TRIP_END || booking.getStatus()==CommonLib.TRACK_STAGE_TRIP_END_CASHBACK)
                ((TextView)getView.findViewById(R.id.trip_info)).setText(getResources().getString(R.string.trip_complete));

        } else if (intercityBooking != null) {

            if(intercityBooking.getPickupAddress() != null)
                ((TextView)getView.findViewById(R.id.start_location)).setText(intercityBooking.getPickupAddress());
            if(intercityBooking.getDropAddress() != null)
                ((TextView)getView.findViewById(R.id.drop_location)).setText(intercityBooking.getDropAddress());

            ((TextView)getView.findViewById(R.id.pickupTime)).setText(intercityBooking.getDropAddress());

            getView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UploadManager.updateTripRating(String.valueOf(ratingBar.getRating()),intercityBooking.getHash(), CommonLib.INTERCITY);
                }
            });

            if(booking.getStatus()==CommonLib.TRACK_STAGE_BOOKING_CANCELLED){
                ((TextView)getView.findViewById(R.id.trip_info)).setText(getResources().getString(R.string.trip_cancel));
            }else if(booking.getStatus()==CommonLib.TRACK_STAGE_TRIP_END || booking.getStatus()==CommonLib.TRACK_STAGE_TRIP_END_CASHBACK)
                ((TextView)getView.findViewById(R.id.trip_info)).setText(getResources().getString(R.string.trip_complete));

        }
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.ADD_RATING){
            if(status){
                //finish();
                if(!((Home)activity).hasDestroyed())
                    ((Home)activity).onBackPressed();
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
