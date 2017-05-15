package com.application.zapplon.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.application.zapplon.receivers.GcmBroadcastReceiver;
import com.application.zapplon.utils.CommonLib;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	public static boolean notificationDismissed = false;

	public Context context;
	private SharedPreferences prefs;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	public static final int NOTIFICATION_ID = 1;

	public GcmIntentService() {
		super("GcmIntentService");
		context = this;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = null;
		if(intent != null)
			extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		prefs = getSharedPreferences("application_settings", 0);

		if (extras != null && !extras.isEmpty()) {
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */

			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				CommonLib.ZLog("Send error:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				CommonLib.ZLog("Deleted messages on server:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				if (extras.containsKey("Notification"))
					com.application.zapplon.utils.NotificationManager.getInstance(getApplicationContext()).sendNotification(extras);
			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

}