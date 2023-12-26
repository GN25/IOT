package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

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

    private TextView tx_light;

    //private TextView lux_prueba;
    private Switch switchLights;

    private Switch switchOnOffLights;

    private SharedPreferences sharedPreferences;

    boolean active = false;

    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "LightsActivity";

    String outputvalue;
    @SuppressLint("MissingInflatedId")
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

        switchOnOffLights = findViewById(R.id.switchLightsOnOff);
        //lux_prueba=findViewById(R.id.luxPrueba);
        tx_light=findViewById(R.id.luxLevelTextView);

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

        connect();
        ////////////API CONNECTION///////////////////////////////////////////
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    subscribe("iot/sensors");
                } else {
                    System.out.println("Connected to: " + serverURI);
                    subscribe("iot/sensors");
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws
                    Exception {
                String newMessage = new String(message.getPayload());
                System.out.println("Incoming message: " + newMessage);

                String lux_value=newMessage.split(";")[0];

                tx_light.setText("Detected lux level: "+lux_value);
                Float value = Float.parseFloat(lux_value);


                if(value<3){
                    //Prueba para valores,
                    //activateLamp();
                }else{
                    //desactivateLamp();
                }

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        ////////////////////////////////////////////////////////////




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

        switchOnOffLights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                   // active = true;
                    //saveSwitchState(isChecked);
                    //processLogic();
                    run("tdtool --on 1");
                } else {
                    //active = false;
                    //saveSwitchState(isChecked);
                    //processLogic();
                    run("tdtool --off 1");
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

    private void activateLamp() {
        run("python3 turnOn.py");
    }
    private void desactivateLamp() {
        run("python3 turnOff.py");
    }

    private void readTemperature() {
        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                // Add code to fetch data via SSH
                run("python3 listsensors.py");
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                // Add code to preform actions after doInBackground
                //lux_prueba.setText(outputvalue);
            }
        }.execute(1);
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




    //CONNECTION METHODS
    private void connect(){
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), SERVER_URI,
                        clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " +
                            SERVER_URI);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void subscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscription successful to topic: " + topic);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    System.out.println("Failed to subscribe to topic: " + topic);
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    ///////////////////////SSH LIBRARY

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