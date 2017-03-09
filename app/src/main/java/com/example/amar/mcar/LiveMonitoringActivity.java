package com.example.amar.mcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.amar.mcar.OBD.LoadCommand;
import com.example.amar.mcar.OBD.MassAirFlowCommand;
import com.example.amar.mcar.OBD.RPMCommand;
import com.example.amar.mcar.OBD.SpeedCommand;
import com.example.amar.mcar.OBD.ThrottlePositionCommand;
import com.example.amar.mcar.enums.ObdProtocols;
import com.example.amar.mcar.protocol.EchoOffCommand;
import com.example.amar.mcar.protocol.LineFeedOffCommand;
import com.example.amar.mcar.protocol.SelectProtocolCommand;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class LiveMonitoringActivity extends AppCompatActivity {

    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice OBDevice;
    private BluetoothSocket bluetoothSocket;
    LinearLayout llh;
    LinearLayout llv1;
    LinearLayout llv2;
    TextView speed;
    TextView rpm;
    TextView kmpl_i;
    TextView load;
    TextView absoluteLoad;
    TextView throttlePosition;

    Thread rpmThread = null;
    Thread speedAndKmplthread = null;
    Thread loadThread = null;
    Thread absoluteLoadThread = null;
    Thread throttlePositionThread = null;

    ArrayList<Double> mafArrayList = new ArrayList<Double>();
    Float spd = 0f;
    Double maf = 0.0;
    long[] t1 = new long[1];
    long[] t2 = new long[1];
    Double total_distance_Since_LMON = null; //in kilometers
    Double lastLocationLatitude = null;
    Double lastLocationLongitude = null;
    Double litres_of_fuel_consumed_Since_LMON = null;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    String rpmReading = "0";
    String speedReading = "0";
    Double kmpiReading = 0.0;
    String loadReading = "0";
    String absoluteLoadReading = "0";
    String throttlePositionReading = "0";

    Handler speedHandler = new Handler();
    Handler rpmHandler = new Handler();
    Handler kmplIHandler = new Handler();
    Handler loadHandler = new Handler();
    Handler absoluteLoadHandler = new Handler();
    Handler throttlePositionHandler = new Handler();

    private void init() {
        this.llh = (LinearLayout) findViewById(R.id.linearLayoutH);
        this.llv1 = (LinearLayout) llh.findViewById(R.id.linearLayoutV1);
        this.llv2 = (LinearLayout) llh.findViewById(R.id.linearLayoutV2);
        this.speed = (TextView) llv2.findViewById(R.id.textView21);
        this.rpm = (TextView) llv2.findViewById(R.id.textView22);
        this.kmpl_i = (TextView) llv2.findViewById(R.id.textView23);
        this.load = (TextView) llv2.findViewById(R.id.textView24);
        this.absoluteLoad = (TextView) llv2.findViewById(R.id.textView25);
        this.throttlePosition = (TextView) llv2.findViewById(R.id.textView26);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_monitoring);

        init();

        speed.setText("WOW");

        Intent intent = getIntent();
//        if(intent!=null){
//         not possible
//        }

        String device_address = intent.getStringExtra(Constants.DEVICE_ADDRESS);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.OBDevice = bluetoothAdapter.getRemoteDevice(device_address);


        /*                  ESTABLISHING CONNECTION, UUID NEEDED                       */
        try {
            this.bluetoothSocket = this.OBDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            e.printStackTrace();
            setResult(Constants.CONNECTION_FAILED);
            Log.e("lm", "Couldn't create RFCOMM Socket");
            finish();
        }

        if (this.bluetoothSocket == null) {
            setResult(Constants.CONNECTION_FAILED);
            Log.e("lm", "Bluetooth Socket is null");
            finish();
        }

        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            setResult(Constants.CONNECTION_FAILED);
            Log.e("lm", "Couldn't connect bluetooth socket");
//            finish();
        }

        /*                                  DONE                                       */


        /*                       NOW INITIALISING OBD COMMANDS                            */


        try {
            new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*                      DONE                        */


        /*                  NOW USING COMMANDS                  */

        final RPMCommand rpmCommand = new RPMCommand();
        this.rpmThread = new Thread() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        rpmReading = rpmCommand.getFormattedResult();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    rpmHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            rpm.setText(rpmReading);
                        }
                    });
                }
            }
        };
        rpmThread.start();

        final SpeedCommand speedCommand = new SpeedCommand();
        final MassAirFlowCommand massAirFlowCommand = new MassAirFlowCommand();

        this.speedAndKmplthread = new Thread() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        speedCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
//                        massAirFlowCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        speedReading = speedCommand.getFormattedResult();
//                        speed_meter.setText(speed);
//                        Log.i("Speed", speedReading + "\n");
//                        maf = massAirFlowCommand.getMAF();
//                        LiveMonitoringActivity.this.makeChangesToArrayList(mafArrayList, 1, maf);
//                        kmpiReading = (2.98022 * speedCommand.getMetricSpeed()) / maf;
//                        (2.98022 * speedCommand.getMetricSpeed()) / maf
//                        Log.i("Kmpl", (2.98022 * speedCommand.getMetricSpeed()) / maf + "kmpl");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    speedHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            speed.setText(speedReading);
//                            kmpl_i.setText(kmpiReading.toString());
                        }
                    });
                }

            }
        };
        speedAndKmplthread.start();

        final LoadCommand loadCommand = new LoadCommand();

        this.loadThread = new Thread() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        loadCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        loadReading = loadCommand.getFormattedResult();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    loadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            load.setText(loadReading);
                        }
                    });
                }
            }
        };

//        loadThread.start();
        {
//        final AbsoluteLoadCommand absoluteLoadCommand = new AbsoluteLoadCommand();
//
//        this.absoluteLoadThread = new Thread() {
//            @Override
//            public void run() {
//                while (!Thread.currentThread().isInterrupted()) {
//                    try {
//                        absoluteLoadCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
//                        absoluteLoadReading = loadCommand.getFormattedResult();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    absoluteLoadHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            absoluteLoad.setText(loadReading);
//                        }
//                    });
//                }
//            }
//        };
//
//        absoluteLoadThread.start();
        }

        final ThrottlePositionCommand throttlePositionCommand = new ThrottlePositionCommand();

        this.throttlePositionThread = new Thread() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        throttlePositionCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        throttlePositionReading = loadCommand.getFormattedResult();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    throttlePositionHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            throttlePosition.setText(loadReading);
                        }
                    });
                }
            }
        };

//        throttlePositionThread.start();

//        getLastLocation();
//        setGoogleClient();

    }


    public synchronized void makeChangesToArrayList(ArrayList<Double> mafArrayList, int req, double mafVal) {
        if (req == 1) { //add
            Log.i("lm", "makeChangesToArrayList > adding to list : " + mafVal);
            mafArrayList.add(mafVal);
        } else if (req == -1) {
            Log.i("lm", "makeChangesToArrayList > clearing list ");
            mafArrayList.clear();
        } else {
            Log.i("liveMonitoring", "sync make changes to array list, wtf");
        }
    }


    @Override
    public void finish() {
        if(!this.rpmThread.isInterrupted() && this.rpmThread!=null){
            this.rpmThread.interrupt();
        }
        if(!this.speedAndKmplthread.isInterrupted() && this.speedAndKmplthread!=null){
            this.speedAndKmplthread.interrupt();
        }
        super.finish();
    }

//    {
        //        if(lastLocationLatitude == null || lastLocationLongitude == null){
//            Log.i("lm", "onLocationChanged>first Start");
//            lastLocationLatitude = location.getLatitude();
//            lastLocationLongitude = location.getLongitude();
//            litres_of_fuel_consumed_Since_LMON = 0.0;
//            total_distance_Since_LMON = 0.0;
//            t1[0] = System.currentTimeMillis();
//            Log.i("lm", "onLocationChanged>firstStart>t1 initialised : " + t1[0]);
//            return;
//        }
//
//        t2[0] = System.currentTimeMillis();
//
//
//        Double[] maf_tempArray = new Double[mafArrayList.size()];
//        for(int i = 0; i < mafArrayList.size(); i++)//(Double []) mafArrayList.toArray();
//        {
//            maf_tempArray[i] = mafArrayList.get(i);
//        }
//        LiveMonitoringActivity.this.makeChangesToArrayList(mafArrayList, -1, 0.0);
//
//        long time_lapsed = 0;
//        if(t2[0] > t1[0]){
//            time_lapsed = t2[0] - t1[0];
//        }else{
//            Log.i("Long KMPL", "returned as time_lapsed = 0");
//            return;
//        }
//        t1[0] = t2[0];
//        long avg_t = (time_lapsed/maf_tempArray.length)/1000; //to convert into seconds per maf reading in the time elapsed
//        Log.i("lm", "onLocationChanged > avg_t : " + avg_t);
//        Double grams_of_total_air_consumed_sinceLMON = 0.0;
//        for(Double maf_element : maf_tempArray ){
//            grams_of_total_air_consumed_sinceLMON +=maf_element;
//        }
//        Log.i("lm", "onLocationChanged > grams_of_total_air_consumed_sinceLMON : "+grams_of_total_air_consumed_sinceLMON);
//        maf_tempArray = null;
//        grams_of_total_air_consumed_sinceLMON = grams_of_total_air_consumed_sinceLMON *avg_t;
//
//        total_distance_Since_LMON +=getDistanceFromLatLonInKm(lastLocationLatitude, lastLocationLongitude, location.getLatitude(), location.getLongitude());
//
//        Log.i("lm", "onLocationChanged > total_distance_Since_LMON :" + total_distance_Since_LMON);
//        lastLocationLatitude = location.getLatitude();
//        lastLocationLongitude = location.getLongitude();
//        litres_of_fuel_consumed_Since_LMON += grams_of_total_air_consumed_sinceLMON /(832*14.5);
//
//        kmplReading = (total_distance_Since_LMON / litres_of_fuel_consumed_Since_LMON);
//        kmpl_l.setText(kmplReading.toString());
////        Log.i("Long KMPL", (total_distance_Since_LMON / litres_of_fuel_consumed_Since_LMON)+"");

//    }

}
