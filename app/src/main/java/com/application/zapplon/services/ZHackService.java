package com.application.zapplon.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

/**
 * Created by apoorvarora on 01/02/16.
 */
public class ZHackService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences("application_settings", 0);

        if (sharedPreferences.getBoolean("ZOMATO_LOGOUT", false)) {
            //log out zomato cause it's fun :)
            Intent logoutIntent = new Intent("com.application.zomato.LOGOUT_INTENT");
            sendBroadcast(logoutIntent);
        }

        if (sharedPreferences.getBoolean("ZOMATO_ORDER_LOGOUT", false)) {
            //log out order cause it's more fun :)
            Intent orderingIntent = new Intent("com.application.zomato.ordering.LOGOUT_INTENT");
            sendBroadcast(orderingIntent);
        }

        stopSelf();
        return START_STICKY;
    }

}


