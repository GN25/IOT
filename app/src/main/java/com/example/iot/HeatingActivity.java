package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;



public class HeatingActivity extends AppCompatActivity {



    private static final String STATE_SENSING = "Sensing";
    private static final String STATE_SIMULATING = "Simulating";

    private Handler handler;
    private final int INTERVAL = 5000;

    private Slider sliderSimulation;
    private Button btApplySimulation;
    private TextView tvState;
    private TextView tvReceivedTemperature;

    private EditText editTextTempLimit;
    private Button btSaveTempThreshold;
    private TextView tvActualtempThreshold;
    private RadioButton rbHeatersState;

    private float temp_threshold;
    private float tempValue;
    private boolean isSensing;
    private boolean isSimulating;
    private Utils utils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // setContentView(R.layout.heating_activity);
        setContentView(R.layout.heaters_activity);

        utils=Utils.getInstance(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tvState=findViewById(R.id.tvStateValue);
        sliderSimulation=findViewById(R.id.sliderTemperature);
        btApplySimulation=findViewById(R.id.btApplySimulation);
        editTextTempLimit=findViewById(R.id.editTextTemperatureLimit);
        btSaveTempThreshold=findViewById(R.id.btSaveTempThreshold);
        tvActualtempThreshold=findViewById(R.id.tvActualTemperatureThresholdValue);
        tvReceivedTemperature=findViewById(R.id.tvReceivedTemperatureValue);
        rbHeatersState=findViewById(R.id.rbHeatersState);
        rbHeatersState.setClickable(false);
        btApplySimulation.setEnabled(false);
        sliderSimulation.setEnabled(false);
        temp_threshold= 20;
        tempValue=15;
        tvState.setText("None");
        tvReceivedTemperature.setText("-");



        MaterialButtonToggleGroup toggleButtonTempMode = findViewById(R.id.toggleButtonTempAutomation);

        toggleButtonTempMode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.btSensor) {
                        tvReceivedTemperature.setText(utils.temp_value_received+"");
                        tvState.setText(STATE_SENSING);
                        btApplySimulation.setEnabled(false);
                        sliderSimulation.setEnabled(false);
                        isSensing=true;
                        isSimulating=false;
                        startRepeatingTask();

                    } else if (checkedId == R.id.btSimulation) {
                        tvReceivedTemperature.setText("-");
                        tvState.setText(STATE_SIMULATING);
                        btApplySimulation.setEnabled(true);
                        sliderSimulation.setEnabled(true);
                        isSensing=false;
                        isSimulating=true;
                        startRepeatingTask();
                    }


                } else {
                    tvState.setText("None");
                    btApplySimulation.setEnabled(false);
                    sliderSimulation.setEnabled(false);
                    isSensing=false;
                    isSimulating=false;
                    tvReceivedTemperature.setText("-");
                    stopRepeatingTask();
                }
            }
        });
        btApplySimulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempValue= sliderSimulation.getValue();
                tvReceivedTemperature.setText(tempValue+"");
            }
        });
        btSaveTempThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue= String.valueOf(editTextTempLimit.getText());
                tvActualtempThreshold.setText(newValue);
                temp_threshold=Float.parseFloat(newValue);
                Log.i("Temp control", "SavedLimit: "+temp_threshold);
            }
        });

        handler = new Handler(Looper.getMainLooper());


    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if(isSensing){
                tempValue = utils.temp_value_received;
                tvReceivedTemperature.setText(tempValue+"");

                checkLimits();

            }else if(isSimulating){
                //Log.i("A11","Is simulating");
               checkLimits();
            }else{
                tvReceivedTemperature.setText("-");
            }

            handler.postDelayed(this, INTERVAL);
        }
    };

    private void checkLimits() {
        if(tempValue<=temp_threshold){
            rbHeatersState.setChecked(true);//Simulation of an actuator behaviour
        }else{
            rbHeatersState.setChecked(false);//Simulation of an actuator behaviour
        }
    }

    void startRepeatingTask() {
        runnable.run();
    }
    void stopRepeatingTask() {
        handler.removeCallbacks(runnable);
    }





}
