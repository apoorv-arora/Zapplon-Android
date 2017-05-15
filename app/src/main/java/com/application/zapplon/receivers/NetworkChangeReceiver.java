package com.application.zapplon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.application.zapplon.utils.CommonLib;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (intent != null && intent.getAction() != null && (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") || intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED"))) {
            if (CommonLib.isNetworkAvailable(context)) {
                Intent smsIntent = new Intent(CommonLib.LOCAL_INTERNET_CONNECTIVITY_BROADCAST);
                LocalBroadcastManager.getInstance(context).sendBroadcast(smsIntent);
            }
        }
    }
}