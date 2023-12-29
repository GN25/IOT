package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Calendar;




public class LightsActivity extends AppCompatActivity {

    private static final String PREF_NAME = "LightsPrefs";
    private static final String KEY_START_HOUR = "startHour";
    private static final String KEY_START_MINUTE = "startMinute";
    private static final String KEY_END_HOUR = "endHour";
    private static final String KEY_END_MINUTE = "endMinute";
    private static final String KEY_SWITCH_STATE = "switchState";

    private Switch switchOnOffLights;

    private SharedPreferences sharedPreferences;


    private Handler handler;
    private final int INTERVAL = 5000;
    private FrameLayout dropdownContainer;



    int automatedLightsMode=0;//0 nada, 1 lux level , 2 time range
    //Mode1
    private Button btnSubmitLuxLevel;
    private TextView tvActualThresholdLuxLevel;
    private TextView tvReceivedLuxValue;
    private EditText editTextNewLuxValue;
    private  int lux_threshold = 3;

    //Mode2
    private Button btnSubmitTimeRange;
    private TimePicker timePickerStart ;
    private TimePicker timePickerEnd ;
    private TextView tvRange;
    private int hourS;
    private int minS;
    private int hourE;
    private int minE;


    private Utils utils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utils=Utils.getInstance(getApplicationContext());

        setContentView(R.layout.lights_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switchOnOffLights = findViewById(R.id.switchLightsOnOff);


        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);


        int lastStartHour = sharedPreferences.getInt(KEY_START_HOUR, -1);
        int lastStartMinute = sharedPreferences.getInt(KEY_START_MINUTE, -1);
        int lastEndHour = sharedPreferences.getInt(KEY_END_HOUR, -1);
        int lastEndMinute = sharedPreferences.getInt(KEY_END_MINUTE, -1);
        boolean lastSwitchState = sharedPreferences.getBoolean(KEY_SWITCH_STATE, false);
       // switchLights.setChecked(lastSwitchState);
        displaySelectedTimeRange(lastStartHour, lastStartMinute, lastEndHour, lastEndMinute);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        dropdownContainer = findViewById(R.id.dropdownContainer);


        MaterialButtonToggleGroup toggleButtonAutomation = findViewById(R.id.toggleButtonAutomation);
        FrameLayout dropdownContainer = findViewById(R.id.dropdownContainer);

        toggleButtonAutomation.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    // Mostrar el dropdownContainer cuando se selecciona un botón
                    LayoutInflater inflater = LayoutInflater.from(LightsActivity.this);
                    View inflatedView = null;


                    if (checkedId == R.id.btLuxLevel) {

                        dropdownContainer.removeAllViews();
                        inflatedView = inflater.inflate(R.layout.luxvalue_dropdown, dropdownContainer, false);
                        automatedLightsMode=1;
                        startRepeatingTask();
                    } else if (checkedId == R.id.btTimeRange) {


                        dropdownContainer.removeAllViews();
                        inflatedView = inflater.inflate(R.layout.timerange_dropdown, dropdownContainer, false);
                        automatedLightsMode=2;
                        startRepeatingTask();
                    }

                    dropdownContainer.addView(inflatedView);
                    dropdownContainer.setVisibility(View.VISIBLE);
                } else {
                    automatedLightsMode=0;
                    dropdownContainer.setVisibility(View.GONE);
                }
            }
        });

        switchOnOffLights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    activateLamp();


                } else {
                    desactivateLamp();


                }
            }
        });

        handler = new Handler(Looper.getMainLooper());

    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(automatedLightsMode==1){
                checkLuxLevel();
            }
            if(automatedLightsMode==2){
                checkTimeRange();
            }

            handler.postDelayed(this, INTERVAL);
        }
    };

    private void checkLuxLevel() {
        btnSubmitLuxLevel=findViewById(R.id.buttonSaveLuxLevel);
        tvActualThresholdLuxLevel =findViewById(R.id.tvActualThresholdValue);
        tvReceivedLuxValue =findViewById(R.id.tvReceivedLuxValue);

        editTextNewLuxValue=findViewById(R.id.editTextLuxThreshold);
        if(btnSubmitLuxLevel!=null && tvActualThresholdLuxLevel !=null && editTextNewLuxValue!=null){

            btnSubmitLuxLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newValue= String.valueOf(editTextNewLuxValue.getText());
                    tvActualThresholdLuxLevel.setText(newValue);
                    lux_threshold=Integer.parseInt(newValue);
                    Log.i("Lux control", "Saved: "+lux_threshold);
                }
            });
            tvReceivedLuxValue.setText( utils.lux_value_received+"");
            if(utils.lux_value_received<=lux_threshold){
                //Prueba para valores,
                activateLamp();
                Log.i("Lux control", "Activated ("+utils.lux_value_received+" < "+lux_threshold);
            }else{
                //THE IDEA IS TO PUT THE SENSOR IN AN OUTSIDE PLACE
                //OTHERWISE IT WILL BE A LOOP:
                //  no light -> on
                //  on -> light
                //  light ->off
                //  off -> no light
                desactivateLamp();
                Log.i("Lux control", "Desactivated ( NO-> "+utils.lux_value_received+" < "+lux_threshold);
            }
        }

    }

    void startRepeatingTask() {
        runnable.run();
    }
    void stopRepeatingTask() {
        handler.removeCallbacks(runnable);
    }
    private void activateLamp() {
        utils.run("tdtool --on 1");
    }
    private void desactivateLamp() {
        utils.run("tdtool --off 1");
    }



    private void checkTimeRange() {

        timePickerStart = findViewById(R.id.timePickerStart);
        timePickerEnd = findViewById(R.id.timePickerEnd);
        btnSubmitTimeRange = findViewById(R.id.btnSubmitLights);
        tvRange = findViewById(R.id.tvCurrentHourRange);
        if (tvRange!=null && btnSubmitTimeRange !=null && timePickerStart != null && timePickerEnd != null) {
            btnSubmitTimeRange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        hourS = timePickerStart.getHour();
                        minS = timePickerStart.getMinute();
                        hourE = timePickerEnd.getHour();
                        minE = timePickerEnd.getMinute();
                        Log.e("Hora guardada",hourS+":"+minS+" - "+hourE+":"+minE);
                        tvRange.setText(hourS+":"+minS+" - "+hourE+":"+minE);

                }
            });
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            if ((currentHour > hourS || (currentHour == hourS && currentMinute >= minS))
                    && (currentHour < hourE || (currentHour == hourE && currentMinute <= minE))) {
                Toast.makeText(this, "Hour in range", Toast.LENGTH_SHORT).show();
                activateLamp();
            } else {
                Toast.makeText(this, "Hour not in range", Toast.LENGTH_SHORT).show();
                desactivateLamp();
            }
        } else {
            Log.e("ERROR NULL P", "Fallo al cargar layout");
        }

    }


    private void saveSelectedTimeRange(int startHour, int startMinute, int endHour, int endMinute) {
        // Save the selected time range in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_START_HOUR, startHour);
        editor.putInt(KEY_START_MINUTE, startMinute);
        editor.putInt(KEY_END_HOUR, endHour);
        editor.putInt(KEY_END_MINUTE, endMinute);
        editor.apply();
    }

    private void handleHourError() {
        Toast.makeText(this, "Invalid hour range", Toast.LENGTH_SHORT).show();
    }

    private void displaySelectedTimeRange(int startHour, int startMinute, int endHour, int endMinute) {

        String displayText = "Time Range: " + formatTime(startHour, startMinute)
                + " - " + formatTime(endHour, endMinute);
        //textViewLightsTimeRange.setText(displayText);
    }

    private String formatTime(int hour, int minute) {
        // HH:mm (24-h)
        return String.format("%02d:%02d", hour, minute);
    }

    private void saveSwitchState(boolean isChecked) {
        // Save the switch state in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SWITCH_STATE, isChecked);
        editor.apply();
    }










}