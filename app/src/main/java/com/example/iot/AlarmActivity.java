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

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmActivity extends AppCompatActivity {

    private static final String PREF_NAME = "AlarmPrefs";
    private static final String KEY_SWITCH_STATE = "switchState";

    private Switch switchAlarm;
    private SharedPreferences sharedPreferences;
    private TextView tx_prox;

    private TextView tv_proxThreshold;
    private Button bt_saveProx;
    private EditText editText_proxThreshold;
    private int actual_proxThreshold;


    private boolean notified = false;
    private Utils utils;

    private Handler handler;
    private final int INTERVAL = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);

        utils=Utils.getInstance(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        switchAlarm = findViewById(R.id.switchAlarm);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        tx_prox = findViewById(R.id.tx_prox);
        tv_proxThreshold=findViewById(R.id.tv_actualThresholdValue);
        editText_proxThreshold=findViewById(R.id.editTextProximityThreshold);
        bt_saveProx=findViewById(R.id.bt_saveProximityThreshold);
        actual_proxThreshold=3000;
        bt_saveProx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue=String.valueOf(editText_proxThreshold.getText());
               actual_proxThreshold= Integer.parseInt(newValue);
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

            checkProximity();
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
        tx_prox.setText("Proximity detected: " + utils.prox_value_received);

        Float value = utils.prox_value_received;
        if (switchAlarm.isChecked()) {
            if (value > actual_proxThreshold) {
                if (!notified) {
                    System.out.println("ALARMA: " + value);
                    handleAlarmDetected();
                    notified = true;
                }

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




