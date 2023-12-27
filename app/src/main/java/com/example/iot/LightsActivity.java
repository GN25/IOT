package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
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
import java.util.Calendar;

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





    private Switch switchOnOffLights;

    private SharedPreferences sharedPreferences;
    boolean automaticActive = false;
    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "LightsActivity";
    private Handler handler;
    private final int INTERVAL = 5000;
    private FrameLayout dropdownContainer;

    String outputvalue;



    int automatedLightsMode=0;//0 nada, 1 lux level , 2 time range

    //Mode1
    private Button btnSubmitLuxLevel;
    private TextView tvActualThresholdLuxLevel;
    private TextView tvReceivedLuxValue;
    private EditText editTextNewLuxValue;
    private  int lux_threshold = 3;
    private float lux_value_received;

    //Mode2
    private Button btnSubmitTimeRange;
    private TimePicker timePickerStart ;
    private TimePicker timePickerEnd ;
    private TextView tvRange;
    private int hourS;
    private int minS;
    private int hourE;
    private int minE;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lights_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //switchLights = findViewById(R.id.switchLights);

        switchOnOffLights = findViewById(R.id.switchLightsOnOff);
        //lux_prueba=findViewById(R.id.luxPrueba);
        //tx_light=findViewById(R.id.luxLevelTextView);

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

                //tx_light.setText("Detected lux level: "+lux_value);
                Float value = Float.parseFloat(lux_value);
                Log.i("Lux control", "Test: "+value);
                lux_value_received=value;
                Log.i("Lux control", "Received: "+lux_value_received);
                /*
                if(value<1200){
                    //Prueba para valores,
                    activateLamp();
                }else{
                    desactivateLamp();
                }*/

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        ////////////////////////////////////////////////////////////




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
            tvReceivedLuxValue.setText( lux_value_received+"");
            if(lux_value_received<=lux_threshold){
                //Prueba para valores,
                activateLamp();
                Log.i("Lux control", "Activated ("+lux_value_received+" < "+lux_threshold);
            }else{
                //THE IDEA IS TO PUT THE SENSOR IN AN OUTSIDE PLACE
                //OTHERWISE IT WILL BE A LOOP:
                //  no light -> on
                //  on -> light
                //  light ->off
                //  off -> no light
                desactivateLamp();
                Log.i("Lux control", "Desactivated ( NO-> "+lux_value_received+" < "+lux_threshold);
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
        run("tdtool --on 1");
    }
    private void desactivateLamp() {
        run("tdtool --off 1");
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