package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ThermometerActivity extends AppCompatActivity {

    private static final String PREF_NAME = "ThermometerPrefs";
    private static final String KEY_MAX_TEMPERATURE = "maxThermometer";
    private static final String KEY_SWITCH_STATE = "switchState";
    private EditText editTextMaxTemperatureT;
    private TextView textViewTemperatureTDisplay;
    private Switch switchTherm;
    private SharedPreferences sharedPreferences;
     private TextView tvTermometro;
    boolean active = false;
    String outputvalue;
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
        tvTermometro = findViewById(R.id.tvTemp);

        getValorSensor();

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

    private void getValorSensor() {
        run("tdtool --list-sensors");
        /*
        String[] lines = outputvalue.split("\n");
        String[] vals = lines[0].split(" ");


         */
        tvTermometro.setText(outputvalue);
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



    public void run (String command) {
        String hostname = "192.168.1.8";
        String username = "pi";
        String password = "iot";
        StringBuilder str = new StringBuilder();
        try         {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Connection conn = new Connection(hostname); //init connection
            conn.connect();
            //start connection to the hostname
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");
            Session sess = conn.openSession();
            sess.execCommand(command);
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout)); //reads text
            while (true){
                String line = br.readLine(); // read line
                if (line == null)
                    break;
                str.append(line);
                System.out.println(line);
            }
            outputvalue = str.toString();
            /* Show exit status, if available (otherwise "null") */
            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close(); // Close this session
            conn.close();

        } catch (IOException e)         {
            e.printStackTrace(System.err);
            System.exit(2);
        }
    }
}
