package com.application.zapplon.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.application.zapplon.services.TrackingService;
import com.application.zapplon.utils.CommonLib;

/**
 * Created by apoorvarora on 03/07/16.
 */
public class TrackingReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(!CommonLib.isMyServiceRunning(context, TrackingService.class)) {

            Intent service = new Intent(context, TrackingService.class);
            startWakefulService(context, service);

        }
    }

    public void setAlarm(Context context, long syncTime) {
        alarmMgr = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TrackingService.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                syncTime, syncTime, alarmIntent);

    }

    public void cancelAlarm(Context context) {
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}