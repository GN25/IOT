package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;

public class ProximityActivity extends AppCompatActivity {

    private static final String PREF_NAME = "AlarmPrefs";
    private static final String KEY_SWITCH_STATE = "switchState";

    private static final String STATE_SENSING = "Sensing";
    private static final String STATE_SIMULATING = "Simulating";

    private Switch switchAlarm;
    private SharedPreferences sharedPreferences;


    private TextView tv_proxThreshold;
    private Button bt_saveProx;
    private EditText editText_proxThreshold;



    private boolean notified = false;
    private Utils utils;

    private Handler handler;
    private final int INTERVAL = 5000;

    private Slider sliderSimulation;
    private Button btApplySimulation;
    private TextView tvState;
    private TextView tvReceivedProximity;
    private float prox_threshold;
    private float proxValue;
    private boolean isSensing;
    private boolean isSimulating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proximity_activity);

        utils=Utils.getInstance(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        tvState=findViewById(R.id.tvStateValue);
        sliderSimulation=findViewById(R.id.sliderProx);
        btApplySimulation=findViewById(R.id.btApplySimulation);
        tvReceivedProximity=findViewById(R.id.tvReceivedProxValue);

        btApplySimulation.setEnabled(false);
        sliderSimulation.setEnabled(false);
        prox_threshold= 3000;
        proxValue=2999;
        tvState.setText("None");
        tvReceivedProximity.setText("-");

        switchAlarm = findViewById(R.id.switchAlarm);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        tv_proxThreshold=findViewById(R.id.tv_actualThresholdValue);
        editText_proxThreshold=findViewById(R.id.editTextProximityThreshold);
        bt_saveProx=findViewById(R.id.bt_saveProximityThreshold);


        MaterialButtonToggleGroup toggleButtonTempMode = findViewById(R.id.toggleButtonProxAutomation);

        toggleButtonTempMode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.btSensor) {
                        tvReceivedProximity.setText(utils.temp_value_received+"");
                        tvState.setText(STATE_SENSING);
                        btApplySimulation.setEnabled(false);
                        sliderSimulation.setEnabled(false);
                        isSensing=true;
                        isSimulating=false;
                        startRepeatingTask();

                    } else if (checkedId == R.id.btSimulation) {
                        tvReceivedProximity.setText("-");
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
                    tvReceivedProximity.setText("-");
                    stopRepeatingTask();
                }
            }
        });
        btApplySimulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proxValue= sliderSimulation.getValue();
                tvReceivedProximity.setText(proxValue+"");
            }
        });


        bt_saveProx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue=String.valueOf(editText_proxThreshold.getText());
               prox_threshold= Integer.parseInt(newValue);
               tv_proxThreshold.setText(newValue);
            }
        });

        boolean lastSwitchState = sharedPreferences.getBoolean(KEY_SWITCH_STATE, false);
        switchAlarm.setChecked(lastSwitchState);
        notified = false;
        handler = new Handler(Looper.getMainLooper());
        startRepeatingTask();

        switchAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveSwitchState(isChecked);
                    handleSwitchOn();
                } else {
                    saveSwitchState(isChecked);
                    handleSwitchOff();
                }
            }
        });
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(isSensing){
               proxValue = utils.prox_value_received;
                tvReceivedProximity.setText(proxValue+"");
                checkProximity();
            }else if(isSimulating){
                checkProximity();

            }else{
                tvReceivedProximity.setText("-");
            }
            handler.postDelayed(this, INTERVAL);
        }
    };

    void startRepeatingTask() {
        runnable.run();
    }
    void stopRepeatingTask() {
        handler.removeCallbacks(runnable);
    }
    private void checkProximity() {

        if (switchAlarm.isChecked()) {
            if (proxValue > prox_threshold) {
                Log.i("Prox control",proxValue +">"+ prox_threshold);
                //if (!notified) {
                    //System.out.println("ALARMA: " + proxValue);
                    handleAlarmDetected();
                    notified = true;
                //}

            }
        }
    }

    private void handleSwitchOn() {
        Toast.makeText(this, "Alarm is ON", Toast.LENGTH_SHORT).show();
    }

    private void handleAlarmDetected() {
        Toast.makeText(this, "MOVEMENT DETECTED", Toast.LENGTH_SHORT).show();
        createNotification("PROXIMITY ALERT", "A near-range movement has been detected");

    }

    private void handleSwitchOff() {
        Toast.makeText(this, "Alarm is OFF", Toast.LENGTH_SHORT).show();
    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SWITCH_STATE, isChecked);
        editor.apply();
    }



    void createNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("mi_canal_id", "Mi Canal", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "mi_canal_id")
                .setSmallIcon(R.drawable.alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(1, builder.build());
    }
}




