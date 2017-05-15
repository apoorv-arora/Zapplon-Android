package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.CabBooking;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 14-Sep-16.
 */
public class MyIntracityBookingsFragment extends Fragment implements UploadManagerCallback {

    private ZApplication zapp;
    private SharedPreferences prefs;
    private int width, height;

    private AsyncTask mAsyncRunning;
    private Activity mContext;
    private Activity activity;
    private View getView;
    private WishesAdapter mAdapter;
    private LayoutInflater inflater;
    private LayoutInflater vi;
    private boolean destroyed = false;

    // Load more part
    private ListView mListView;
    private ArrayList<CabBooking> wishes;
    private LinearLayout mListViewFooter;
    private int mWishesTotalCount;
    private ProgressDialog zProgressDialog;
    private boolean loading = false;
    private int count = 10;

    AlertDialog dialog;


    /*public static MyIntracityBookingsFragment newInstance(Bundle bundle) {
        MyIntracityBookingsFragment fragment = new MyIntracityBookingsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.intracity_bookings, null);
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
        inflater = LayoutInflater.from(mContext);
        mListView = (ListView) getView.findViewById(R.id.booking_list);
        mListView.setDivider(null);
        mListView.setDividerHeight(width/20);

        setListeners();
        refreshView();
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

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetHistory().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if (requestType == CommonLib.CAB_CANCELLATION_REQUEST && stringId != null && stringId.equalsIgnoreCase("mbf")) {
            if (destroyed)
                return;
            if (zProgressDialog != null && zProgressDialog.isShowing()) {
                zProgressDialog.dismiss();
            }
            if(status && dialog != null && dialog.isShowing())
                dialog.dismiss();
            //reload the list
            refreshView();
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }

    private class GetHistory extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.VISIBLE);

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
                String url = "";
                String preStr, requestWrapper;
                url = CommonLib.SERVER + "booking/bookings/?start=0&count=" + count;
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.MY_BOOKINGS, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
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

            getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);

            if (result != null) {
                getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof Object[]) {
                    Object[] arr = (Object[]) result;
                    mWishesTotalCount = (Integer) arr[0];
                    setWishes((ArrayList<CabBooking>) arr[1]);
                    if (((ArrayList<CabBooking>) arr[1]).size() == 0) {
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
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                            .show();

                    getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

                    getView.findViewById(R.id.content).setVisibility(View.GONE);
                }
            }

        }
    }

    public class WishesAdapter extends ArrayAdapter<CabBooking> {

        private List<CabBooking> wishes;
        private Activity mContext;
        private int width;
        String otherReason = "";

        public WishesAdapter(Activity context, int resourceId, List<CabBooking> wishes) {
            super(context.getApplicationContext(), resourceId, wishes);
            mContext = context;
            this.wishes = wishes;
            width = mContext.getWindowManager().getDefaultDisplay().getWidth();
        }

        @Override
        public int getCount() {
            if (wishes == null) {
                return 0;
            } else {
                return wishes.size();
            }
        }

        protected class ViewHolder {
            ImageView cabType;
            TextView bookingId;
            TextView amount;
            TextView status;
            TextView time;
            TextView fromAddress;
            TextView toAddress;
            TextView cancel;
            TextView share;
            RelativeLayout relativeLayout;
            LinearLayout cancel_tab;
            RelativeLayout toContainer;
            LinearLayout zappsContainer;
            TextView zappsValue;
            TextView zapps;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            final CabBooking wish = wishes.get(position);
            if (v == null || v.findViewById(R.id.booking_history_root) == null) {
                v = LayoutInflater.from(mContext).inflate(R.layout.deal_history_snippet, null);
            }

            ViewHolder viewHolder = (ViewHolder) v.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.status = (TextView) v.findViewById(R.id.booking_status_text);
                viewHolder.time = (TextView) v.findViewById(R.id.trip_time_text);
                viewHolder.toAddress = (TextView) v.findViewById(R.id.to_address);
                viewHolder.fromAddress = (TextView) v.findViewById(R.id.from_address);
                viewHolder.bookingId = (TextView) v.findViewById(R.id.booking_id_text);
                viewHolder.amount = (TextView) v.findViewById(R.id.booking_amount_text);
                viewHolder.cancel = (TextView) v.findViewById(R.id.cancel);
                viewHolder.share = (TextView) v.findViewById(R.id.share);
                viewHolder.cabType = (ImageView) v.findViewById(R.id.cab_type);
                viewHolder.relativeLayout = (RelativeLayout) v.findViewById(R.id.booking_history_root);
                viewHolder.cancel_tab = (LinearLayout) v.findViewById(R.id.cancel_tab);
                viewHolder.toContainer = (RelativeLayout) v.findViewById(R.id.to_container);
                viewHolder.zappsContainer = (LinearLayout)  v.findViewById(R.id.zapps_container);
                viewHolder.zappsValue = (TextView)  v.findViewById(R.id.zapps_redeemed);
                viewHolder.zapps = (TextView)  v.findViewById(R.id.zapps);
                v.setTag(viewHolder);
            }

            viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if((wish.getType() == CommonLib.TYPE_OLA
                            || wish.getType() == CommonLib.TYPE_EASY
                            || wish.getType() == CommonLib.TYPE_MEGA
                            || wish.getType() == CommonLib.TYPE_JUGNOO) &&
                            (wish.getStatus() == CommonLib.TRACK_STAGE_CALL_DRIVER
                                    || wish.getStatus() == CommonLib.TRACK_STAGE_CLIENT_LOCATED
                                    || wish.getStatus() == CommonLib.TRACK_STAGE_TRIP_START)) {

                        Intent bookingIntent = new Intent(mContext, Home.class);
                        bookingIntent.putExtra("bookingStatus", true);
                        bookingIntent.putExtra("booking", wish);
                        bookingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(bookingIntent);
                    }
                }
            });

            if((wish.getType() == CommonLib.TYPE_OLA
                    || wish.getType() == CommonLib.TYPE_EASY
                    || wish.getType() == CommonLib.TYPE_MEGA
                    || wish.getType() == CommonLib.TYPE_JUGNOO) &&
                    (wish.getStatus() == CommonLib.TRACK_STAGE_CALL_DRIVER
                            || wish.getStatus() == CommonLib.TRACK_STAGE_CLIENT_LOCATED
                            || wish.getStatus() == CommonLib.TRACK_STAGE_TRIP_START)) {

                viewHolder.cancel_tab.setVisibility(View.VISIBLE);
            }

            if(wish.getDropAddress() == null || wish.getDropAddress().isEmpty())
                viewHolder.toContainer.setVisibility(View.GONE);

            if(wish.getCashback() < 1)
                viewHolder.zappsContainer.setVisibility(View.GONE);
            else {
                viewHolder.zappsContainer.setVisibility(View.VISIBLE);
                if(wish.getCashback() > 1) {
                    viewHolder.zappsValue.setText(wish.getCashback() + "");
                    viewHolder.zapps.setText(getResources().getString(R.string.zapps));
                }
                else {
                    viewHolder.zappsValue.setText(wish.getCashback() + "");
                    viewHolder.zapps.setText(getResources().getString(R.string.zapp));
                }
            }

            viewHolder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final View customView = inflater.inflate(R.layout.cancel_dialog, null);
                    dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
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
                                zProgressDialog = ProgressDialog.show(mContext, null, "Cancelling Request. Please wait!!!");
                                UploadManager.cabCancellationRequest(wish.getType(), wish.getAccessToken(), wish.getCrn(), wish.getCallId(), wish.getPickupLatitude(), wish.getPickupLongitude(), wish.getBookingId(),cancel_reason, "mbf");
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

            viewHolder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, wish.getShareUrl());
                    startActivity(shareIntent);

                }
            });

            viewHolder.bookingId.setText(wish.getCrn()+"");
            viewHolder.status.setText(CommonLib.getStatusString(wish.getStatus()));
            viewHolder.status.setTextColor(CommonLib.getStatusColor(mContext, wish.getStatus()));
            Double price = wish.getAmount();
            final DecimalFormat format = new DecimalFormat("0.#");
            viewHolder.amount.setText("₹ "+format.format(price));

            viewHolder.time.setText(CommonLib.getFormattedDate(wish.getCreated(), true));

            viewHolder.fromAddress.setText(wish.getPickupAddress());
            viewHolder.toAddress.setText(wish.getDropAddress());

            viewHolder.cabType.getLayoutParams().width = 3 * width / 15;

            if (wish.getType() == CommonLib.TYPE_UBER) {
                viewHolder.cabType.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.uber, width, width));
                viewHolder.cabType.getLayoutParams().height = width / 15;
            } else if (wish.getType() == CommonLib.TYPE_OLA) {
                viewHolder.cabType.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.olaicon, width, width));
                viewHolder.cabType.getLayoutParams().height = width / 15;
            } else if (wish.getType() == CommonLib.TYPE_EASY) {
                viewHolder.cabType.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.easycabs_logo, width, width));
                viewHolder.cabType.getLayoutParams().height = (3/2) * width / 15;
            } else if (wish.getType() == CommonLib.TYPE_JUGNOO) {
                viewHolder.cabType.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.jugnoo, width, width));
                viewHolder.cabType.getLayoutParams().height = width / 15;
            } else if (wish.getType() == CommonLib.TYPE_MEGA) {
                viewHolder.cabType.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.megacabs, width, width));
                viewHolder.cabType.getLayoutParams().height = width / 15;
            } else if (wish.getType() == CommonLib.TYPE_RIDZ) {
                viewHolder.cabType.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.ridz, width, width));
                viewHolder.cabType.getLayoutParams().height = width / 15;
            }
            return v;
        }

    }

    // set all the wishes here
    private void setWishes(ArrayList<CabBooking> wishes) {
        this.wishes = wishes;
        if (wishes != null && wishes.size() > 0 && mWishesTotalCount > wishes.size()
                && mListView.getFooterViewsCount() == 0) {
            mListViewFooter = new LinearLayout(mContext);
            mListViewFooter.setBackgroundResource(R.color.white);
            mListViewFooter.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width / 5));
            mListViewFooter.setGravity(Gravity.CENTER);
            mListViewFooter.setOrientation(LinearLayout.HORIZONTAL);
            ProgressBar pbar = new ProgressBar(mContext, null,
                    android.R.attr.progressBarStyleSmallInverse);
            mListViewFooter.addView(pbar);
            pbar.setTag("progress");
            pbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mListView.addFooterView(mListViewFooter);
        }
        mAdapter = new WishesAdapter(mContext, R.layout.store_item_history_snippet, this.wishes);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();   //previously missing
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount - 1 < mWishesTotalCount
                        && !loading && mListViewFooter != null) {
                    if (mListView.getFooterViewsCount() == 1) {
                        loading = true;
                        new LoadModeWishes().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, totalItemCount - 1);
                    }
                } else if (totalItemCount - 1 == mWishesTotalCount && mListView.getFooterViewsCount() > 0) {
                    mListView.removeFooterView(mListViewFooter);
                }
            }
        });
    }

    private class LoadModeWishes extends AsyncTask<Integer, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Integer... params) {
            int start = params[0];
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                String preStr, requestWrapper;
                url = CommonLib.SERVER + "booking/bookings/?start=" + start + "&count=" + count;
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.MY_BOOKINGS, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
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
            if (result != null && result instanceof Object[]) {
                Object[] arr = (Object[]) result;
                wishes.addAll((ArrayList<CabBooking>) arr[1]);
                mAdapter.notifyDataSetChanged();
            }
            loading = false;
        }
    }


    @Override
    public void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

}
