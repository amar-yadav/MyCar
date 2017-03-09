package com.example.amar.mcar;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    LocationManager lm;
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;

    private int dayhourSelected;
    private int dayminuteSelected;
    private int nighthourSelected;
    private int nightminuteSelected;
    private double latitudeSelected;
    private double longitudeSelected;
    private double newLatitude;
    private double newLongitude;
    private int radiusSelected;
    private Date mDate;
    MediaPlayer mediaPlayer;
    BroadcastReceiver sms_sent_broadcast_receiver;
    BroadcastReceiver sms_delivered_broadcast_receiver;
    SmsManager smsManager;
    PendingIntent sentPendingIntent;
    PendingIntent deliveredPendingIntent;
    int dayAlertSMSCount = 0;

    String SMS_SENT = "SMS_SENT";
    String SMS_DELIVERED = "SMS_DELIVERED";

    private SmsReceiverAbstract smsReceiver;
    private static boolean startTracking = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askLocationPermissions();
        init();

        this.lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, Constants.LOCATION_REQUEST);
        }

        smsReceiver = new SmsReceiverAbstract() {
            @Override
            protected void onNewPosition(String strMessageSource, String strMessageBody) {
                Toast.makeText(MainActivity.this, ":"+strMessageSource+":"+strMessageBody+":", Toast.LENGTH_LONG).show();
                String[] words = strMessageBody.split("\\s");
                if(words[0].equals("DayAlert") && strMessageSource.equals(Constants.IN_CAR_PHONE_NO_AMAR)){
                    Intent intent = new Intent(MainActivity.this, MapTraceActivity.class);
                    startActivity(intent);
                    finish();
                }else if(words[0].equals("StartTracking") && strMessageSource.equals(Constants.REMOTE_PHONE_NO)){
                    startTracking = true;
                }

            }
        };

        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(999);
        this.registerReceiver(smsReceiver, intentFilter);

        Intent intent = getIntent();
        String message;
        if (intent != null && (message = intent.getStringExtra(Constants.MESSAGE_BODY))!=null) {

            Log.i("MainActivity", "onCreate: " + message);
            String[] data = message.split("\\s");
            for(String s : data){
                Log.i("MainActivity", "onCreate: ." + s + ".");
            }
        }

        setGoogleClient();
    }

    private void init() {
        ((CircularImageView) findViewById(R.id.my_car)).setOnClickListener(this);
        ((CircularImageView) findViewById(R.id.remote)).setOnClickListener(this);

        mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mySharedPreferences.edit();
        editor.clear();
//        editor.putInt(Constants.SETTINGS_UPDATED, 0);
        editor.commit();

        mySharedPreferences.registerOnSharedPreferenceChangeListener(this);

        String phoneNumber = "8527940008";
        String smsBody = "This is an SMS!";

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            String permission[] = {Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS};
            ActivityCompat.requestPermissions(this, permission, 1);
            Log.i("main", "onCreate > permission was not granted previously");
            return;
        }

        this.sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        this.deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

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

        // Get the default instance of SmsManager
        this.smsManager = SmsManager.getDefault();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.my_car) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MyCarActivity.class);
            startActivity(intent);
        } else if (id == R.id.remote) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, RemoteActivity.class);
            startActivityForResult(intent, Constants.MAIN_TO_REMOTE);
        }
    }

    public void askLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.MAIN_TO_REMOTE:
                Toast.makeText(this, "Returned from Remote Activity to Bluetooth", Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    setResult(Constants.ACCESS_COARSE_LOCATION_REQUEST_DENIED);
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    setResult(Constants.ACCESS_FINE_LOCATION_REQUEST_DENIED);
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    //-------------------LOCATION------------------------//

    private void setGoogleClient() {
        Log.i("lm", "setGoogleClient");
        if (mGoogleApiClient == null) {
            Log.i("main", "setGoogleClient > mGoogleApiClient was null");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    public void getLastLocation() {
        setGoogleClient();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String permission[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permission, 1);
            Log.i("main", "getLastLocation > permission was not granted previously");
            return;
        }

        Log.i("main", "getLastLocation > outside request permissions");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Log.i("lm", "Latitude : " + mLastLocation.getLatitude());
            Log.i("lm", "Longitude : " + mLastLocation.getLongitude());
        } else {
            Toast.makeText(this, "Please Provide Location Access", Toast.LENGTH_SHORT).show();
        }
        startLocationUpdates();
    }

    public void startLocationUpdates() { //using fusedAPI, mGoogleApiClient to provide location updates according to the mlocationRequest's parameters
        Log.i("lm", "startLocationUpdates");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("lm", "startLocationUpdates > fine_location or coarse_location were denied in manifest");
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i("lm", "onConnected");
        getLastLocation(); //also registers for location updates
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("lm", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("lm", "onConnectionFailed, trying to reconnect");
    }

    @Override
    protected void onStart() {
        Log.i("lm", "onStart");
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        Log.i("lm", "onStop");
        super.onStop();
        mGoogleApiClient.disconnect();

        try{
            unregisterReceiver(sms_sent_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("MainActivity", "onDestroy: ", e);
        }
        try{
            unregisterReceiver(sms_delivered_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("MainActivity", "onDestroy: ", e);
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }

    @Override
    protected void onResume() {
        Log.i("lm", "onResume");
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            Log.i("lm", "onResume > google client connected");
            startLocationUpdates();
        } else {
            Log.i("lm", "onResume > google client was disconnected");
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        Log.i("lm", "onPause");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            Log.i("lm", "onPause > google client connected");
            stopLocationUpdates();
        } else {
            Log.i("lm", "onPause > google client was disconnected");
            mGoogleApiClient.connect();
        }

        try{
            unregisterReceiver(sms_sent_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("MainActivity", "onDestroy: ", e);
        }
        try{
            unregisterReceiver(sms_delivered_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("MainActivity", "onDestroy: ", e);
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }

    }

    private void stopLocationUpdates() {
        Log.i("lm", "stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) { //LocationListener.onLocationChanged is invoked by fused location provider
        Log.i("lm", "onLocationChanged");
        newLatitude = location.getLatitude();
        newLongitude = location.getLongitude();
        Log.i("lm", "Latitude : " + newLatitude);
        Log.i("lm", "Longitude : " + newLongitude);
//        Toast.makeText(this, "Latitude : " + location.getLatitude() + " Longitude : " + location.getLongitude(), Toast.LENGTH_LONG).show();

        Log.i("lm", "onLocationChanged: " + mySharedPreferences.getString(Constants.SETTINGS_UPDATED, "1"));
        if (mySharedPreferences.getString(Constants.SETTINGS_UPDATED, "0").equals("1")) {
            Log.i("lm", "savedLatitude : " + latitudeSelected);
            Log.i("lm", "savedLongitude : " + longitudeSelected);
            Log.i("MainActivity", "onLocationChanged: preferences update recognised");

            mDate = new Date();
            int hours = mDate.getHours();
            int minutes = mDate.getMinutes();

            Log.i("MainActivity", "onLocationChanged: currenthours " + hours + ", currentMinutes " + minutes);
            if ((hours > dayhourSelected || (hours == dayhourSelected && minutes >= dayminuteSelected)) && (hours < nighthourSelected || ( hours == nighthourSelected && minutes < nightminuteSelected))) {
                //daytime alarm
                Log.i("MainActivity", "onLocationChanged: DayAlert!");

                if (meterDistanceBetweenPoints(latitudeSelected, longitudeSelected, newLatitude, newLongitude) > radiusSelected*1000) {
                    // Send a text based SMS
                    Log.i("MainActivity", "onLocationChanged: out of bound " + meterDistanceBetweenPoints(latitudeSelected, longitudeSelected, newLatitude, newLongitude));
                    Log.i("MainActivity", "onLocationChanged: " + dayAlertSMSCount);

                    if(dayAlertSMSCount%5 == 0){
                        Log.i("MainActivity", "onLocationChanged: ");

                        Toast.makeText(this, "Sending message :DayAlert " + newLatitude + " " + newLongitude, Toast.LENGTH_SHORT).show();
                        smsManager.sendTextMessage(Constants.REMOTE_PHONE_NO, null, "DayAlert " + newLatitude + " " + newLongitude, sentPendingIntent, deliveredPendingIntent);
                        dayAlertSMSCount++;
                    }
                    dayAlertSMSCount++;
                }else{
                    Log.i("MainActivity", "onLocationChanged: Noooo "+meterDistanceBetweenPoints(latitudeSelected, longitudeSelected, newLatitude, newLongitude) +", "+ radiusSelected);
                }
            }
            else if(startTracking){
                Toast.makeText(this, "Sending message :DayAlert " + newLatitude + " " + newLongitude, Toast.LENGTH_SHORT).show();
                smsManager.sendTextMessage(Constants.REMOTE_PHONE_NO, null, "DayAlert " + newLatitude + " " + newLongitude, sentPendingIntent, deliveredPendingIntent);
                dayAlertSMSCount++;
            }
            else{
                Log.i("MainActivity", "onLocationChanged: " + "selectedHour " + dayhourSelected + ", selectedMinute " + dayminuteSelected + ", currentHour " + hours + ", currentMinutes " + minutes);
                Toast.makeText(this, "selectedHour " + dayhourSelected + ", selectedMinute " + dayminuteSelected + ", currentHour " + hours + ", currentMinutes " + minutes, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.i("lm", "onLocationChanged: wthell");
        }

    }

    private double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (double) (180.0/Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
        double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
        double t3 = Math.sin(a1)*Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000*tt;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//        Toast.makeText(this, "onSharedPreferenceChanged", Toast.LENGTH_SHORT).show();
        if (s.equals(Constants.SETTINGS_UPDATED) && (sharedPreferences.getString(Constants.SETTINGS_UPDATED, "0").equals("1"))) {
            this.dayhourSelected = sharedPreferences.getInt(Constants.DAY_START_SAVED_HOUR, 6);
            this.dayminuteSelected = sharedPreferences.getInt(Constants.DAY_START_SAVED_MINUTE, 0);
            this.nighthourSelected = sharedPreferences.getInt(Constants.NIGHT_START_SAVED_HOUR, 24);
            this.nightminuteSelected = sharedPreferences.getInt(Constants.NIGHT_START_SAVED_MINUTE, 0);
            this.latitudeSelected = Double.parseDouble(sharedPreferences.getString(Constants.SAVED_LATITUDE, mLastLocation.getLatitude() + ""));
            this.longitudeSelected = Double.parseDouble(sharedPreferences.getString(Constants.SAVED_LONGITUDE, mLastLocation.getLongitude() + ""));
            this.radiusSelected = sharedPreferences.getInt(Constants.SAVED_RADIUS, 0);
            Toast.makeText(this, dayhourSelected + ":1 " + dayminuteSelected + ":2 " + nighthourSelected + ":3 " + nightminuteSelected + ":4 " + latitudeSelected + ":5 " + longitudeSelected + ":6 " + radiusSelected + ":7", Toast.LENGTH_SHORT).show();
            Log.i("MainActivity", "onSharedPreferenceChanged: "+dayhourSelected + ":1 " + dayminuteSelected + ":2 " + nighthourSelected + ":3 " + nightminuteSelected + ":4 " + latitudeSelected + ":5 " + longitudeSelected + ":6 " + radiusSelected + ":7");
//            editor.putInt(Constants.SETTINGS_UPDATED, 0);
//            editor.commit();
        }else{
//            Toast.makeText(this, "onSharedPreferenceChanged wtf", Toast.LENGTH_SHORT).show();
        }
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
//        smsManager.sendTextMessage(Constants.REMOTE_PHONE_NO, null, smsBody, sentPendingIntent, deliveredPendingIntent);
    }

    @Override
    protected void onDestroy() {
        try{
            unregisterReceiver(sms_sent_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("MainActivity", "onDestroy: ", e);
        }
        try{
            unregisterReceiver(sms_delivered_broadcast_receiver);
        }catch (IllegalArgumentException e){
            Log.e("MainActivity", "onDestroy: ", e);
        }
        try{
            unregisterReceiver(smsReceiver);
        }catch (IllegalArgumentException e){
            Log.e("MainActivity", "onDestroy: ", e);
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
        super.onDestroy();
    }
}
