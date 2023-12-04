package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class AlarmActivity extends AppCompatActivity {

    private static final String PREF_NAME = "AlarmPrefs";
    private static final String KEY_SWITCH_STATE = "switchState";

    private Switch switchAlarm;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        switchAlarm = findViewById(R.id.switchAlarm);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        boolean lastSwitchState = sharedPreferences.getBoolean(KEY_SWITCH_STATE, false);
        switchAlarm.setChecked(lastSwitchState);

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

    private void handleSwitchOn() {
        Toast.makeText(this, "Alarm is ON", Toast.LENGTH_SHORT).show();
    }

    private void handleSwitchOff() {
        Toast.makeText(this, "Alarm is OFF", Toast.LENGTH_SHORT).show();
    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SWITCH_STATE, isChecked);
        editor.apply();
    }


}


