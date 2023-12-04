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

public class HeatingActivity extends AppCompatActivity {

    private static final String PREF_NAME = "TemperaturePrefs";
    private static final String KEY_MIN_TEMPERATURE = "minTemperature";
    private static final String KEY_MAX_TEMPERATURE = "maxTemperature";

    private static final String KEY_SWITCH_STATE = "switchState";

    private EditText editTextMinTemperature;
    private EditText editTextMaxTemperature;
    private TextView textViewTemperatureDisplay;

    private Switch switchHeating;

    private SharedPreferences sharedPreferences;

    boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.heating_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editTextMinTemperature = findViewById(R.id.editTextMinTemperature);
        editTextMaxTemperature = findViewById(R.id.editTextMaxTemperature);
        textViewTemperatureDisplay = findViewById(R.id.textViewTemperatureDisplay);
        switchHeating = findViewById(R.id.switchHeating);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        float lastMinTemperature = sharedPreferences.getFloat(KEY_MIN_TEMPERATURE, Float.NaN);
        float lastMaxTemperature = sharedPreferences.getFloat(KEY_MAX_TEMPERATURE, Float.NaN);
        displayEnteredTemperatures(lastMinTemperature, lastMaxTemperature);

        boolean lastSwitchState = sharedPreferences.getBoolean(KEY_SWITCH_STATE, false);
        switchHeating.setChecked(lastSwitchState);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String minTemperatureString = editTextMinTemperature.getText().toString();
                String maxTemperatureString = editTextMaxTemperature.getText().toString();

                if (!minTemperatureString.isEmpty() && !maxTemperatureString.isEmpty()) {

                    float minTemperature = Float.parseFloat(minTemperatureString);
                    float maxTemperature = Float.parseFloat(maxTemperatureString);
                    saveEnteredTemperatures(minTemperature, maxTemperature);
                    displayEnteredTemperatures(minTemperature, maxTemperature);
                    processLogic();

                } else {
                    handleVoidError();
                }
            }
        });


        switchHeating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

    private void processLogic() {

    }

    private void handleVoidError() {
        Toast.makeText(this, "No empty fields allowed", Toast.LENGTH_SHORT).show();
    }

    private void displayEnteredTemperatures(float minTemperature, float maxTemperature) {
        String displayText = "Min Temperature: " + minTemperature + "\nMax Temperature: " + maxTemperature;
        textViewTemperatureDisplay.setText(displayText);
    }

    private void saveEnteredTemperatures(float minTemperature, float maxTemperature) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_MIN_TEMPERATURE, minTemperature);
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
