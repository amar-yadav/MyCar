package com.example.amar.mcar;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

public class RemoteActivity extends AppCompatActivity {

    private CircularImageView mSwitch;
    private SmsManager mSmsManager;
    String smsBody = "";
    String SMS_SENT = "SMS_SENT";
    String SMS_DELIVERED = "SMS_DELIVERED";
    String SMS_RECEIVED = "SMS_RECEIVED";
    BroadcastReceiver sms_sent_broadcast_receiver;
    BroadcastReceiver sms_delivered_broadcast_receiver;
    BroadcastReceiver sms_received_broadcast_receiver;
    PendingIntent sentPendingIntent;
    PendingIntent deliveredPendingIntent;
    PendingIntent receivedPendingIntent;
    private boolean b = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings_menu_item:
                Toast.makeText(this, "settings was tapped", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivityForResult(intent, Constants.REMOTE_TO_SETTINGS);
                return true;

//            case R.id.help:
//                showHelp();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        // Get the default instance of SmsManager
        mSmsManager = SmsManager.getDefault();
        initSMS();
        init();
    }

    private void initSMS() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            String permission[] = {android.Manifest.permission.SEND_SMS, android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS};
            ActivityCompat.requestPermissions(this, permission, 1);
            Log.i("RemoteActivity", "initSMS > permission was not granted previously");
            return;
        }

        sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

//      For when the SMS has been sent
        sms_sent_broadcast_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(sms_sent_broadcast_receiver, new IntentFilter(SMS_SENT));

//      For when the SMS has been delivered
        sms_delivered_broadcast_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(sms_delivered_broadcast_receiver, new IntentFilter(SMS_DELIVERED));

        receivedPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_RECEIVED), 0);
    }

    private void init() {
//        this.mSmsManager = SmsManager.getDefault();
        this.mSwitch = (CircularImageView) findViewById(R.id.immobilize);
        mSwitch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!b) {
                    new AlertDialog.Builder(RemoteActivity.this)
                            .setTitle("CAUTION!")
                            .setMessage("Are you sure you want to immobilise you car?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
//                                    Toast.makeText(RemoteActivity.this, "fukin hell", Toast.LENGTH_SHORT).show();
                                    smsBody = "imon";
                                    mSmsManager.sendTextMessage(Constants.I_20_PHONE_NO, null, smsBody, sentPendingIntent, deliveredPendingIntent);
                                    b = true;
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
                else{
                    new AlertDialog.Builder(RemoteActivity.this)
                            .setTitle("CAUTION!")
                            .setMessage("Are you sure you want to turn off immobiliser on your car?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(RemoteActivity.this, "fukin hell bruh", Toast.LENGTH_SHORT).show();
                                    smsBody = "imoff";
                                    mSmsManager.sendTextMessage(Constants.I_20_PHONE_NO, null, smsBody, sentPendingIntent, deliveredPendingIntent);
                                    b = false;
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                return b;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REMOTE_TO_SETTINGS: {
                if (resultCode == Constants.SETTINGS_TO_REMOTE_SAVED_SETTINGS) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    String lat = sharedPreferences.getString(Constants.SAVED_LATITUDE, null);
                    Toast.makeText(this, "Settings were saved successfully " + lat, Toast.LENGTH_SHORT).show();
                }
                break;
            }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        try{
            unregisterReceiver(sms_sent_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("RemoteActivity", "onDestroy: ", e);
        }
        try{
            unregisterReceiver(sms_delivered_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("RemoteActivity", "onDestroy: ", e);
        }
        try{
            unregisterReceiver(sms_received_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("RemoteActivity", "onDestroy: ", e);
        }
        super.onDestroy();
    }
}
