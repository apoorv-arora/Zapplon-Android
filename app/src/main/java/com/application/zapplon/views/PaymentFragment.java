package com.application.zapplon.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.City;
import com.application.zapplon.data.IntercityCab;
import com.application.zapplon.data.TaxiBookings;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;
import com.application.zapplon.utils.ZPaymentWebView;

import java.text.DecimalFormat;

public class PaymentFragment extends Fragment implements UploadManagerCallback {

    private ZApplication zapp;
    private int width, height;
    private LayoutInflater vi;
    private boolean destroyed = false;
    private ProgressDialog zProgressDialog;
    private EditText et;
    private View bookBt;
//    private PayuConfig payuConfig;

    //data
//    int env = PayuConstants.PRODUCTION_ENV;
    String key = "3s6sTaSM"; // live:3s6sTaSM, dev:gtKFFx
    String salt = "1ec1B3P2r7"; //live: 1ec1B3P2r7, dev: eCwWELxi
    String merchantId = "5611077"; //live: 1ec1B3P2r7, dev: eCwWELxi

    //payment variables
    String userName = "";
    String amount = "0";
    String tranId = "";
    String productInfo = "";
    private City fromCity, toCity;
    private String fromDate, toDate;

    private IntercityCab cab;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;

    public static PaymentFragment newInstance(Bundle bundle) {
        PaymentFragment fragment = new PaymentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.intercity_payment_fragment, null);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = activity.getWindowManager().getDefaultDisplay().getWidth();
        height = activity.getWindowManager().getDefaultDisplay().getHeight();
        vi = LayoutInflater.from(activity.getApplicationContext());

        bookBt = getView.findViewById(R.id.submit_container);

        if (getArguments() != null && getArguments().containsKey("cabToBook")) {
            cab = (IntercityCab) getArguments().get("cabToBook");
            Bundle bundle = getArguments();
            fromCity = (City) bundle.getSerializable("fromCity");
            toCity = (City) bundle.getSerializable("toCity");
            fromDate = bundle.getString("fromDate");
            toDate = bundle.getString("toDate");
        }

        userName = prefs.getString("username", "");

        ((ImageView) (ImageView) getView.findViewById(R.id.cab_icon2)).setImageBitmap(CommonLib.getBitmap(activity, CommonLib.getBrandBitmap(cab.getType()), width, width));

        ((TextView) getView.findViewById(R.id.start_location)).setText(fromCity.getName());
        ((TextView) getView.findViewById(R.id.drop_location)).setText(toCity.getName());
        ((TextView) getView.findViewById(R.id.pickup_timer)).setText(CommonLib.getFormattedDate(Long.parseLong(fromDate), true));

        if (toDate == null || toDate.length() < 1) {
            getView.findViewById(R.id.drop_date_container).setVisibility(View.GONE);
            getView.findViewById(R.id.drop_date_label).setVisibility(View.GONE);
        } else {
            ((TextView) getView.findViewById(R.id.drop_timer)).setText(CommonLib.getFormattedDate(Long.parseLong(toDate), false));
            getView.findViewById(R.id.drop_date_container).setVisibility(View.VISIBLE);
            getView.findViewById(R.id.drop_date_label).setVisibility(View.VISIBLE);
        }

        TextView display_name_2 = (TextView) getView.findViewById(R.id.display_name2);
        display_name_2.setText(cab.getDisplayName());

        ((TextView)getView.findViewById(R.id.pay)).setText(getResources().getString(R.string.pay_now) + "₹ " + cab.getAdvance());
        String subType = "";
        if (cab.getSubType() == CommonLib.CAB_SEDAN) {
            subType = getResources().getString(R.string.sedan);
        } else if (cab.getSubType() == CommonLib.CAB_COMPACT) {
            subType = getResources().getString(R.string.compact);
        } else if (cab.getSubType() == CommonLib.CAB_LUXURY) {
            subType = getResources().getString(R.string.luxury);
        } else if (cab.getSubType() == CommonLib.SUV) {
            subType = getResources().getString(R.string.suv);
        } else if (cab.getSubType() == CommonLib.TEMPO) {
            subType = getResources().getString(R.string.tempo);
        }
        TextView cab_type_2 = (TextView) getView.findViewById(R.id.cab_type2);
        cab_type_2.setText(subType);


        TextView time_2 = (TextView) getView.findViewById(R.id.time2);
        if (cab.getCapacity() > 1)
            time_2.setText(cab.getCapacity() + " SEATS");
        else
            time_2.setText(cab.getCapacity() + " SEATS");

        final DecimalFormat format = new DecimalFormat("0.#");
        TextView baseFare_2 = (TextView) getView.findViewById(R.id.travel_cost);
        baseFare_2.setText("₹ " + format.format(cab.getFare()));

        TextView rate_per_km_2 = (TextView) getView.findViewById(R.id.rate_per_KM2);
        rate_per_km_2.setText("₹ " + format.format(cab.getCostPerDistance()));

        ((TextView) getView.findViewById(R.id.terms_desc)).setText(cab.getTerms());

        setListeners();
        UploadManager.addCallback(this);
    }

    private void setListeners() {
        bookBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zProgressDialog = ProgressDialog.show(activity, null, getResources().getString(R.string.booking_your_cab));
                UploadManager.intercityBookingRequest(cab.getType(), cab.getCabType(), fromCity.getName(), toCity.getName(), cab.getAdvance(), cab.getFare(), fromDate, toDate, cab.getDisplayName(), cab.getBookingId(), "", "", CommonLib.TYPE_PAYMENT_PENDING);
            }
        });
    }

//    private PostData postData;
//    private PayUChecksum checksum;
    private String var1;
//    PayuHashes payuHashes;

//    public void generateHashFromSDK(PaymentParams mPaymentParams, String Salt) {
//        payuHashes = new PayuHashes();
//        postData = new PostData();
//
//        // payment Hash;
//        checksum = null;
//        checksum = new PayUChecksum();
//        checksum.setAmount(mPaymentParams.getAmount());
//        checksum.setKey(mPaymentParams.getKey());
//        checksum.setTxnid(mPaymentParams.getTxnId());
//        checksum.setEmail(mPaymentParams.getEmail());
//        checksum.setSalt(Salt);
//        checksum.setProductinfo(mPaymentParams.getProductInfo());
//        checksum.setFirstname(mPaymentParams.getFirstName());
//        checksum.setUdf1("");
//        checksum.setUdf2("");
//        checksum.setUdf3("");
//        checksum.setUdf4("");
//        checksum.setUdf5("");
//
//        postData = checksum.getHash();
//        if (postData.getCode() == PayuErrors.NO_ERROR) {
//            payuHashes.setPaymentHash(postData.getResult());
//        }
//
//        mPaymentParams.setHash(payuHashes.getPaymentHash());
//
//        // checksum for payemnt related details
//        // var1 should be either user credentials or default
//        var1 = PayuConstants.DEFAULT;
//
//        if ((postData = calculateHash(key, PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // Assign post data first then check for success
//            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(postData.getResult());
//        //vas
//        if ((postData = calculateHash(key, PayuConstants.VAS_FOR_MOBILE_SDK, PayuConstants.DEFAULT, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
//            payuHashes.setVasForMobileSdkHash(postData.getResult());
//
//        // getIbibocodes
//        if ((postData = calculateHash(key, PayuConstants.GET_MERCHANT_IBIBO_CODES, PayuConstants.DEFAULT, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
//            payuHashes.setMerchantIbiboCodesHash(postData.getResult());
//
//        if (!var1.contentEquals(PayuConstants.DEFAULT)) {
//            // get user card
//            if ((postData = calculateHash(key, PayuConstants.GET_USER_CARDS, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // todo rename storedc ard
//                payuHashes.setStoredCardsHash(postData.getResult());
//            // save user card
//            if ((postData = calculateHash(key, PayuConstants.SAVE_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
//                payuHashes.setSaveCardHash(postData.getResult());
//            // delete user card
//            if ((postData = calculateHash(key, PayuConstants.DELETE_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
//                payuHashes.setDeleteCardHash(postData.getResult());
//            // edit user card
//            if ((postData = calculateHash(key, PayuConstants.EDIT_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
//                payuHashes.setEditCardHash(postData.getResult());
//        }
//
//        if (mPaymentParams.getOfferKey() != null) {
//            postData = calculateHash(key, PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey(), salt);
//            if (postData.getCode() == PayuErrors.NO_ERROR) {
//                payuHashes.setCheckOfferStatusHash(postData.getResult());
//            }
//        }
//
//        if (mPaymentParams.getOfferKey() != null && (postData = calculateHash(key, PayuConstants.CHECK_OFFER_STATUS, mPaymentParams.getOfferKey(), salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) {
//            payuHashes.setCheckOfferStatusHash(postData.getResult());
//        }
//
//        Intent intent = new Intent(activity, PayUBaseActivity.class);
//        intent.putExtra(PayuConstants.SALT, salt);
//        intent.putExtra(PayuConstants.ENV, PayuConstants.PRODUCTION_ENV);
//        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
//        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
//        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);
//        activity.startActivityForResult(intent, CommonLib.PAYMENT_REQUEST_CODE_PAYMENT_COLLECTOR);
//    }

    // deprecated, should be used only for testing.
//    private PostData calculateHash(String key, String command, String var1, String salt) {
//        checksum = null;
//        checksum = new PayUChecksum();
//        checksum.setKey(key);
//        checksum.setCommand(command);
//        checksum.setVar1(var1);
//        checksum.setSalt(salt);
//        return checksum.getHash();
//    }

    @Override
    public void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonLib.PAYMENT_REQUEST_CODE_PAYMENT_COLLECTOR || requestCode == CommonLib.PAYMENT_REQUEST_CODE_THIRD_PARTY_COLLECTOR) {
            if (data != null) {
                if(resultCode == Activity.RESULT_OK) {
                    resultCode = CommonLib.TYPE_PAYMENT_DONE;
                    TaxiBookings bookings = (TaxiBookings) data.getSerializableExtra("booking");
                    //upload manager api call
                    zProgressDialog = ProgressDialog.show(activity, null, getResources().getString(R.string.booking_your_cab));
                    UploadManager.intercityBookingRequest(bookings.getType(), bookings.getCabType(), bookings.getFromCity(), bookings.getToCity(), bookings.getAdvance(), bookings.getAmount(), bookings.getStartDate()+"", bookings.getReturnDate()+"", "", bookings.getBookingId()+"", "", "", resultCode);
                } else if(resultCode == Activity.RESULT_CANCELED) {
                    resultCode = CommonLib.TYPE_PAYMENT_FAILED;
                    Toast.makeText(activity, "Payment failed", Toast.LENGTH_LONG).show();
                    if (data.hasExtra("booking") && data.getSerializableExtra("booking") != null) {
                        TaxiBookings bookings = (TaxiBookings) data.getSerializableExtra("booking");
                        UploadManager.intercityBookingRequest(bookings.getType(), bookings.getCabType(), bookings.getFromCity(), bookings.getToCity(), bookings.getAdvance(), bookings.getAmount(), bookings.getStartDate() + "", bookings.getReturnDate() + "", "", bookings.getBookingId() + "", "", "", resultCode);
                    }
                } else
                    Toast.makeText(activity, "Payment failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "Payment failed", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void uploadFinished(int requestType, int cabId, int action, Object data, int uploadId, boolean status, String stringId) {
        if (requestType == CommonLib.INTERCITY_BOOKING_REQUEST) {
            if (zProgressDialog != null && zProgressDialog.isShowing())
                zProgressDialog.dismiss();

            if(!destroyed) {
                if(action == CommonLib.TYPE_PAYMENT_PENDING && status) {
                    TaxiBookings bookings = (TaxiBookings) data;
                    productInfo = "Intercity";// bundle.getString("productInfo");
                    tranId = bookings.getReferenceId();
                    amount = bookings.getAdvance()+"";

                    Intent intent = new Intent(activity, ZPaymentWebView.class);
                    intent.putExtra("title", "Confirm Payment");
                    intent.putExtra("url", "http://zapplon.com/intercity/results/redirecting.php?url=" + bookings.getPaymentUrl() + "&param=" + bookings.getPaymentParam());
                    intent.putExtra("taxiBookings", bookings);
                    activity.startActivityForResult(intent, CommonLib.PAYMENT_REQUEST_CODE_THIRD_PARTY_COLLECTOR);
                } else if(action == CommonLib.TYPE_PAYMENT_DONE && status) {
                    TaxiBookings bookings = (TaxiBookings) data;

                    Intent intent = new Intent(activity, Home.class);
                    intent.putExtra("booking",bookings);
                    intent.putExtra("intercityBookingStatus", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } else if(action == CommonLib.TYPE_PAYMENT_FAILED && status) {
                    //stay here
                    Toast.makeText(activity, getResources().getString(R.string.payment_failed), Toast.LENGTH_SHORT).show();
                } else {
                    //stay here
                    Toast.makeText(activity, getResources().getString(R.string.err_occurred), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
    }


}
