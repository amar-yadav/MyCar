package com.example.amar.mcar;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapTraceActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SmsReceiverAbstract smsReceiver;
    private LatLng nuLatLng;
    private double latitude = Double.parseDouble("28.6651787");
    private double longitude = Double.parseDouble("77.3780441");
    private boolean newPositionAvailable = false;
    public boolean flag = false;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

//        mediaPlayer = MediaPlayer.create(this, notification);
//        mediaPlayer.start();


        Log.i("MapTraceActivity", "onCreate: ");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        initSMSTrack(Constants.IN_CAR_PHONE_NO_AMAR, Constants.START_TRACKING);

        smsReceiver = new SmsReceiverAbstract() {
            @Override
            protected void onNewPosition(String strMessageSource, String strMessageBody) {

                Toast.makeText(MapTraceActivity.this, ":"+strMessageSource+":"+strMessageBody+":", Toast.LENGTH_LONG).show();
                String[] words = strMessageBody.split("\\s");
                if(words[0].equals("DayAlert")){
                    Log.i("MapTraceActivity", "onNewPosition: "+words[1]+words[2]);
//                    nuLatLng = new LatLng(Double.parseDouble(words[1]), Double.parseDouble(words[2]));
                    latitude = Double.parseDouble(words[1]);
                    longitude = Double.parseDouble(words[2]);
                    LatLng sydney = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f));
//                    newPositionAvailable = true;
//                    Toast.makeText(MapTraceActivity.this, latitude+", "+longitude+", "+newPositionAvailable, Toast.LENGTH_LONG).show();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(999);
        this.registerReceiver(smsReceiver, intentFilter);
    }

    private void initSMSTrack(String phoneNumber, String smsBody) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            String permission[] = {android.Manifest.permission.SEND_SMS, android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS};
            ActivityCompat.requestPermissions(this, permission, 1);
            Log.i("main", "onCreate > permission was not granted previously");
            return;
        }

//        String phoneNumber = "+918527940008";
//        String smsBody = "This is an SMS!";

        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        // For when the SMS has been sent
        registerReceiver(new BroadcastReceiver() {
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
        }, new IntentFilter(SMS_SENT));

        // For when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
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
        }, new IntentFilter(SMS_DELIVERED));

        // Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();
        // Send a text based SMS
        if(!flag){
            smsManager.sendTextMessage(Constants.IN_CAR_PHONE_NO_AMAR, null, smsBody, sentPendingIntent, deliveredPendingIntent);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        28.6651787, 77.3780441
//        LatLng sydney = new LatLng(latitude, longitude);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Add a marker in Sydney and move the camera
//        while(newPositionAvailable){
//
//            if(addnew!=null){
//                Toast.makeText(this, "new Position found", Toast.LENGTH_SHORT).show();
//                mMap.addMarker(new MarkerOptions().position(addnew).title("gg"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(addnew));
//                newPositionAvailable = false;
//            }else{
//                Toast.makeText(this, "waiting for postition update", Toast.LENGTH_SHORT).show();
//            }
//        }
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            this.unregisterReceiver(this.smsReceiver);
        }catch (IllegalArgumentException e){
            Log.e("MapTraceActivity", "onDestroy: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
        }
        Intent intent = new Intent();
        intent.setClass(MapTraceActivity.this, MainActivity.class);
        startActivity(intent);
        super.onDestroy();
    }
}
