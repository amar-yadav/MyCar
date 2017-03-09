package com.example.amar.mcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(BluetoothActivity.this, MyCarActivity.class);
//                startActivity(intent);
                finish();
            }
        });

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //check if bluetooth supported on device
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            setResult(Constants.BLUETOOTH_ADPATER_NOT_FOUND);
            finish();
        }

        //Ask user to enable bluetooth
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }else{
            setup();
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                setup();
            }
            else{
                setResult(Constants.BT_REQUEST_DENIED);
                finish();
            }
        }
    }

    private void setup(){
        Set<BluetoothDevice> pairedDevices = this.mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            final ArrayList<ListItem> listItems = new ArrayList<>();
            for(BluetoothDevice bluetoothDevice : pairedDevices){
                listItems.add(new ListItem(bluetoothDevice.getName(), bluetoothDevice.getAddress()));
            }

            ListAdapter listAdapter = new ListAdapter(this, listItems);
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ListItem device = listItems.get(i);
                    Intent intent = new Intent();
                    intent.setClass(BluetoothActivity.this, MyCarActivity.class);
                    intent.putExtra(Constants.DEVICE_NAME, device.name);
                    intent.putExtra(Constants.DEVICE_ADDRESS, device.address);
                    setResult(Constants.BT_DEVICE_SELECTED, intent);
//                    startActivity(intent);
                    finish();
                }
            });
        }else{
            setResult(Constants.NO_DEVICES_PAIRED);
            finish();
        }

    }
}
