package com.example.amar.mcar;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

public class MyCarActivity extends AppCompatActivity {

    CircularImageView connect_button;
    CircularImageView liveMonitoring_button;
    String device_name;
    String device_address;
    LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_car);

        init();

        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(MyCarActivity.this, "Please enable GPS to continue", Toast.LENGTH_SHORT).show();
                    if(!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        Toast.makeText(MyCarActivity.this, "Please connect to internet to continue", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(MyCarActivity.this, BluetoothActivity.class);
                startActivityForResult(intent, Constants.MYCAR_TO_BLUETOOTH);
//                finish();
            }
        });

        liveMonitoring_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(MyCarActivity.this, "Please enable GPS to continue", Toast.LENGTH_SHORT).show();
                    if(!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        Toast.makeText(MyCarActivity.this, "Please connect to internet to continue", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if(device_name == null){
                    Toast.makeText(MyCarActivity.this, "Please connect OBD adpater first, press Connect OBD Adapter", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent();
                    intent.setClass(MyCarActivity.this, LiveMonitoringActivity.class);
                    intent.putExtra(Constants.DEVICE_NAME, device_name);
                    intent.putExtra(Constants.DEVICE_ADDRESS, device_address);
                    startActivityForResult(intent, Constants.MYCAR_TO_LM);
                }
            }
        });


    }

    private void init(){
        this.connect_button = (CircularImageView) findViewById(R.id.connect);
        this.liveMonitoring_button = (CircularImageView) findViewById(R.id.monitor);

        this.lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.MYCAR_TO_BLUETOOTH){
            if(resultCode == Constants.BLUETOOTH_ADPATER_NOT_FOUND){
                Toast.makeText(MyCarActivity.this, "Bluetooth not supported by your device", Toast.LENGTH_SHORT).show();
            }else if(resultCode == Constants.BT_REQUEST_DENIED){
                Toast.makeText(MyCarActivity.this, "Please enable bluetooth", Toast.LENGTH_SHORT).show();
            }else if(resultCode == Constants.NO_DEVICES_PAIRED){
                Toast.makeText(MyCarActivity.this, "Please pair OBD Adapter first.", Toast.LENGTH_SHORT).show();
            }else if(resultCode == Constants.BT_DEVICE_SELECTED){
                this.device_name = data.getStringExtra(Constants.DEVICE_NAME);
                this.device_address = data.getStringExtra(Constants.DEVICE_ADDRESS);
                Toast.makeText(MyCarActivity.this, this.device_name + "(OBD Adapter) was selected. Press 'Live Monitoring' to continue.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == Constants.MYCAR_TO_LM){
            if(resultCode == Constants.CONNECTION_FAILED){
                Toast.makeText(MyCarActivity.this, "There was a wee bit of error establishing connection with adapter", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == Constants.LOCATION_REQUEST){
            if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Log.i("main", "onActivityResult > location not enabled");
            }
        }
    }
}
