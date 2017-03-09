package com.example.amar.mcar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by Amar on 11/28/16.
 */

public abstract class SmsReceiverAbstract extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strMessageSource = "";
        String strMessageBody = "";

        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");

            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = myBundle.getString("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                strMessageSource += messages[i].getOriginatingAddress();
                strMessageBody += messages[i].getMessageBody();
//                strMessage += "\n";
            }
            onNewPosition(strMessageSource, strMessageBody);
        }
    }

    protected abstract void onNewPosition(String strMessageSource, String strMessageBody);

}
