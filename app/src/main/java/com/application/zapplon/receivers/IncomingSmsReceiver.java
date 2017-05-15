package com.application.zapplon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.application.zapplon.utils.CommonLib;

/**
 * Created by apoorvarora on 15/02/16.
 */
public class IncomingSmsReceiver extends BroadcastReceiver {

    private static final String FROM_NUMBER = "zaplon";

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();

    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {


            final Bundle bundle = intent.getExtras();

            try {

                if (bundle != null) {

                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    String finalMsg = "";

                    String senderNum = "";

                    for (int i = 0; i < pdusObj.length; i++) {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                        senderNum = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();

                        finalMsg += message;
                    }

                    if(senderNum!=null && !"".equals(senderNum)){
                        if(senderNum.toLowerCase().contains(FROM_NUMBER)){

                            Intent smsIntent = new Intent(CommonLib.LOCAL_SMS_BROADCAST);
                            smsIntent.putExtra("verification_message", finalMsg);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(smsIntent);
                        }
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

}
