package com.example.amar.mcar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Amar on 11/28/16.
 */

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage [] messages = null;
        String strMessage = "";
        Intent newIntent = new Intent();

        if (myBundle != null)
        {
            Object [] pdus = (Object[]) myBundle.get("pdus");

            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = myBundle.getString("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
//                strMessage += "SMS From: " + messages[i].getOriginatingAddress();
//                if(messages[i].getOriginatingAddress().contains(Constants.I_20_PHONE_NO)){
//                    strMessage += "wooo";
//                }
//                strMessage += " : ";
                strMessage += messages[i].getMessageBody();
//                if(messages[i].getOriginatingAddress().contains(Constants.I_20_PHONE_NO)){
                    if(messages[i].getMessageBody().contains("DayAlert")&&messages[i].getOriginatingAddress().equals(Constants.IN_CAR_PHONE_NO_AMAR)){
                        newIntent.setClass(context, MapTraceActivity.class);
                        newIntent.putExtra(Constants.MESSAGE_HEAD, "DayAlert");
                        newIntent.putExtra(Constants.MESSAGE_BODY, strMessage);
                    }
//                }
                strMessage += "\n";
            }

            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
            Log.e("SMS", strMessage);
            Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
        }
    }
}
