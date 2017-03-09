package com.example.amar.mcar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import static com.example.amar.mcar.Constants.PLACE_PICKER_REQUEST;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Spinner day_hour_spinner;
    private Spinner day_minute_spinner;
    private Spinner night_hour_spinner;
    private Spinner night_minute_spinner;
    private Button button;
    private Button saveButton;
    private EditText radiusEntered;

    private int dayStartHourSelected;
    private int dayStartMinuteSelected;
    private int nightStartHourSelected;
    private int nightStartMinuteSelected;
    private double latitudeSelected;
    private double longitudeSelected;
    private int radiusSelected;

    Place placeSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
    }

    private void init() {
//        this.timepicker = (TimePicker) findViewById(R.id.timePicker);
//        timepicker.setIs24HourView(true);
//        timepicker.set
        final PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        this.day_hour_spinner = (Spinner) findViewById(R.id.dayHourSpinner);
        ArrayAdapter<CharSequence> adapterDH = ArrayAdapter.createFromResource(this,
                R.array.hours_in_a_day, android.R.layout.simple_spinner_item);
        adapterDH.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.day_hour_spinner.setAdapter(adapterDH);
        this.day_hour_spinner.setOnItemSelectedListener(this);

        this.day_minute_spinner = (Spinner) findViewById(R.id.dayMinuteSpinner);
        ArrayAdapter<CharSequence> adapterDM = ArrayAdapter.createFromResource(this,
                R.array.minutes_in_a_day, android.R.layout.simple_spinner_item);
        adapterDM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.day_minute_spinner.setAdapter(adapterDM);
        this.day_minute_spinner.setOnItemSelectedListener(this);

        this.night_hour_spinner = (Spinner) findViewById(R.id.nightHourSpinner);
        ArrayAdapter<CharSequence> adapterHN = ArrayAdapter.createFromResource(this,
                R.array.hours_in_a_day, android.R.layout.simple_spinner_item);
        adapterHN.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.night_hour_spinner.setAdapter(adapterHN);
        this.night_hour_spinner.setOnItemSelectedListener(this);

        this.night_minute_spinner = (Spinner) findViewById(R.id.nightMinuteSpinner);
        ArrayAdapter<CharSequence> adapterMN = ArrayAdapter.createFromResource(this,
                R.array.minutes_in_a_day, android.R.layout.simple_spinner_item);
        adapterMN.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.night_minute_spinner.setAdapter(adapterMN);
        this.night_minute_spinner.setOnItemSelectedListener(this);

        this.button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivityForResult(builder.build(SettingsActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        this.saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radiusSelected = Integer.parseInt(radiusEntered.getText()+"");
                SharedPreferences saved_settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor editor = saved_settings.edit();
                editor.putInt(Constants.DAY_START_SAVED_HOUR, dayStartHourSelected);
                editor.putInt(Constants.DAY_START_SAVED_MINUTE, dayStartMinuteSelected);
                editor.putInt(Constants.NIGHT_START_SAVED_HOUR, nightStartHourSelected);
                editor.putInt(Constants.NIGHT_START_SAVED_MINUTE, nightStartMinuteSelected);
                editor.putString(Constants.SAVED_LATITUDE, latitudeSelected+"");
                editor.putString(Constants.SAVED_LONGITUDE, longitudeSelected+"");
                editor.putInt(Constants.SAVED_RADIUS, radiusSelected);
                editor.putString(Constants.SETTINGS_UPDATED, "1");
                editor.commit();

                Log.i("SettingsActivity", "onClick: " + saved_settings.getString(Constants.SETTINGS_UPDATED, "0"));

                setResult(Constants.SETTINGS_TO_REMOTE_SAVED_SETTINGS);
                finish();
            }
        });

        this.radiusEntered = (EditText) findViewById(R.id.radius);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                placeSelected = PlacePicker.getPlace(SettingsActivity.this, data);
                LatLng latLng = placeSelected.getLatLng();
                this.latitudeSelected = latLng.latitude;
                this.longitudeSelected = latLng.longitude;

                String toastMsg = String.format("Place: %s", placeSelected.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int id = adapterView.getId();
        switch (id) {
            case R.id.dayHourSpinner :
                Log.i("SettingsActivity ", " > onItemSelected: " + id);
                this.dayStartHourSelected = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                Toast.makeText(this, "dayStartHourSelected : "+dayStartHourSelected, Toast.LENGTH_SHORT).show();
                break;
            case R.id.dayMinuteSpinner :
                Log.i("SettingsActivity ", " > onItemSelected: " + id);
                this.dayStartMinuteSelected = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                Toast.makeText(this, "dayStartMinuteSelected : "+dayStartMinuteSelected, Toast.LENGTH_SHORT).show();
                break;
            case R.id.nightHourSpinner :
                Log.i("SettingsActivity ", " > onItemSelected: " + id);
                this.nightStartHourSelected = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                Toast.makeText(this, "nightStartHourSelected : "+nightStartHourSelected, Toast.LENGTH_SHORT).show();
                break;
            case R.id.nightMinuteSpinner :
                Log.i("SettingsActivity ", " > onItemSelected: " + id);
                this.nightStartMinuteSelected = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                Toast.makeText(this, "nightStartMinuteSelected : "+nightStartMinuteSelected, Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.i("SettingsActivity ", " > onItemSelected: wtf ");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Toast.makeText(this, "onNothinSelected : " + adapterView.getId() , Toast.LENGTH_SHORT).show();
    }
}
