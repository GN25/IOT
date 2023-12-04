package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ThermometerActivity extends AppCompatActivity {

    private static final String PREF_NAME = "ThermometerPrefs";
    private static final String KEY_MAX_TEMPERATURE = "maxThermometer";
    private static final String KEY_SWITCH_STATE = "switchState";
    private EditText editTextMaxTemperatureT;
    private TextView textViewTemperatureTDisplay;
    private Switch switchTherm;
    private SharedPreferences sharedPreferences;

    boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thermometer_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editTextMaxTemperatureT = findViewById(R.id.editTextMaxTemperatureT);
        textViewTemperatureTDisplay = findViewById(R.id.textViewTemperatureTDisplay);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        switchTherm = findViewById(R.id.switchThermometer);



        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        float lastMaxTemperature = sharedPreferences.getFloat(KEY_MAX_TEMPERATURE, Float.NaN);
        displayEnteredTemperatures(lastMaxTemperature);
        boolean lastSwitchState = sharedPreferences.getBoolean(KEY_SWITCH_STATE, false);
        switchTherm.setChecked(lastSwitchState);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String maxTemperatureString = editTextMaxTemperatureT.getText().toString();

                if (!maxTemperatureString.isEmpty()) {

                    float maxTemperature = Float.parseFloat(maxTemperatureString);
                    saveEnteredTemperatures(maxTemperature);
                    displayEnteredTemperatures(maxTemperature);
                    processLogic();

                } else {
                    handleVoidError();
                }
            }
        });


        switchTherm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    handleSwitchOn();
                    saveSwitchState(isChecked);
                } else {
                    handleSwitchOff();
                    saveSwitchState(isChecked);
                }
            }
        });
    }

    private void handleSwitchOn() {
        active = true;
        Toast.makeText(this, "Thermometer notifications are ON", Toast.LENGTH_SHORT).show();
    }

    private void handleSwitchOff() {
        active = false;
        Toast.makeText(this, "Thermometer notifications are OFF", Toast.LENGTH_SHORT).show();
    }

    private void processLogic() {

    }

    private void handleVoidError() {
        Toast.makeText(this, "No empty fields allowed", Toast.LENGTH_SHORT).show();
    }

    private void displayEnteredTemperatures(float maxTemperature) {
        String displayText = "Max Temperature: " + maxTemperature;
        textViewTemperatureTDisplay.setText(displayText);
    }

    private void saveEnteredTemperatures(float maxTemperature) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_MAX_TEMPERATURE, maxTemperature);
        editor.apply();
    }

    private void saveSwitchState(boolean isChecked) {
        // Save the switch state in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SWITCH_STATE, isChecked);
        editor.apply();
    }
}
