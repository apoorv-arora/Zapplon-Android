package com.application.zapplon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.ZTracker;

/**
 * Created by Pratik on 03/21/2016.
 */
public class InstallReferrerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //if invitation is from google app invite
        if (intent != null && intent.getExtras() != null && intent.hasExtra("referrer")) {
            String referrer = String.valueOf(intent.getExtras().get("referrer"));
            if(referrer != null && referrer.startsWith("trackingId_")) {
                String inviteCode = referrer.substring(new String("trackingId_").length(), referrer.length());
                ZTracker.logGAEvent(context, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_INVITE_INSTALLED, inviteCode);
                SharedPreferences prefs = context.getSharedPreferences("application_settings", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("invited_by", inviteCode);
                editor.commit();
            } else {
                ZTracker.logGAEvent(context, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_REFERRED_INSTALLED, referrer);
                try {
                    UploadManager.addCampaign(CommonLib.getIMEI(context), Integer.parseInt(referrer));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } //if invitation is from facebook or whatsapp
        else if (context != null && intent != null && intent.getExtras() != null && intent.hasExtra("invite")) {
            String inviteCode = String.valueOf(intent.getExtras().get("invite"));
            ZTracker.logGAEvent(context, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_INVITE_INSTALLED, inviteCode);
            SharedPreferences prefs = context.getSharedPreferences("application_settings", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("invited_by", inviteCode);
            editor.commit();
        }
    }
}
