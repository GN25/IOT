package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class LightsActivity extends AppCompatActivity {

    private static final String PREF_NAME = "LightsPrefs";
    private static final String KEY_START_HOUR = "startHour";
    private static final String KEY_START_MINUTE = "startMinute";
    private static final String KEY_END_HOUR = "endHour";
    private static final String KEY_END_MINUTE = "endMinute";
    private static final String KEY_SWITCH_STATE = "switchState";

    private TimePicker timePickerStart;
    private TimePicker timePickerEnd;
    private TextView textViewLightsTimeRange;
    private Switch switchLights;

    private SharedPreferences sharedPreferences;

    boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lights_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timePickerStart = findViewById(R.id.timePickerStart);
        timePickerEnd = findViewById(R.id.timePickerEnd);
        textViewLightsTimeRange = findViewById(R.id.textViewLightsTimeRange);
        Button btnSubmitLights = findViewById(R.id.btnSubmitLights);
        switchLights = findViewById(R.id.switchLights);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int lastStartHour = sharedPreferences.getInt(KEY_START_HOUR, -1);
        int lastStartMinute = sharedPreferences.getInt(KEY_START_MINUTE, -1);
        int lastEndHour = sharedPreferences.getInt(KEY_END_HOUR, -1);
        int lastEndMinute = sharedPreferences.getInt(KEY_END_MINUTE, -1);
        boolean lastSwitchState = sharedPreferences.getBoolean(KEY_SWITCH_STATE, false);
        switchLights.setChecked(lastSwitchState);
        displaySelectedTimeRange(lastStartHour, lastStartMinute, lastEndHour, lastEndMinute);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        btnSubmitLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int startHour = timePickerStart.getHour();
                int startMinute = timePickerStart.getMinute();
                int endHour = timePickerEnd.getHour();
                int endMinute = timePickerEnd.getMinute();


                if(startHour <= endHour && startMinute <= endMinute){
                    saveSelectedTimeRange(startHour, startMinute, endHour, endMinute);

                    displaySelectedTimeRange(startHour, startMinute, endHour, endMinute);
                }else{
                    handleHourError();
                }
            }
        });

        switchLights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    active = true;
                    saveSwitchState(isChecked);
                    processLogic();
                } else {
                    active = false;
                    saveSwitchState(isChecked);
                    processLogic();
                }
            }
        });
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
        textViewLightsTimeRange.setText(displayText);
    }

    private String formatTime(int hour, int minute) {
        // HH:mm (24-h)
        return String.format("%02d:%02d", hour, minute);
    }

    private void processLogic() {

    }

    private void saveSwitchState(boolean isChecked) {
        // Save the switch state in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SWITCH_STATE, isChecked);
        editor.apply();
    }
}