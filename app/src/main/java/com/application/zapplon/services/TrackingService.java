package com.application.zapplon.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.application.zapplon.data.CabBooking;
import com.application.zapplon.receivers.TrackingReceiver;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.PostWrapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harsh on 6/21/2016.
 */
public class TrackingService extends IntentService{

    private AsyncTask mAsyncRunning = null;
    String hash;

    public TrackingService() {
        super("TrackingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        hash = intent.getStringExtra("hash");

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
        nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
        nameValuePairs.add(new BasicNameValuePair("ref_id",hash));
        Object result[] = null;
        try {
            result = PostWrapper.postRequest(CommonLib.SERVER + "booking/tracking?", nameValuePairs,
                    PostWrapper.CAB_BOOKING_TRACKING,  this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(result != null && result.length > 0 && result[1] instanceof CabBooking) {

            CabBooking cabBooking = (CabBooking) result[1];

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(CommonLib.LOCAL_CAB_TRACKING_BROADCAST);
            broadcastIntent.putExtra("booking", cabBooking);
            broadcastIntent.putExtra("booking_status", cabBooking.getStatus());

            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

            // if the ride is finished, then cancel the alarm
            if((cabBooking.getType() == CommonLib.TYPE_OLA
                    || cabBooking.getType() == CommonLib.TYPE_EASY
                    || cabBooking.getType() == CommonLib.TYPE_MEGA
                    || cabBooking.getType() == CommonLib.TYPE_JUGNOO) &&
                    (cabBooking.getStatus() == CommonLib.TRACK_STAGE_CALL_DRIVER
                            || cabBooking.getStatus() == CommonLib.TRACK_STAGE_CLIENT_LOCATED
                            || cabBooking.getStatus() == CommonLib.TRACK_STAGE_TRIP_START)) {
                TrackingReceiver alarm4 = new TrackingReceiver();
                alarm4.cancelAlarm(this);
                alarm4.setAlarm(this, CommonLib.POLLING_TIMER);
            }
        }
        stopForeground(true);
    }
}