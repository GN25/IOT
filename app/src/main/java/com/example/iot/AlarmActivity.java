package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

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
    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "AlarmActivity";

    private boolean notified = false;


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

                String prox_value = newMessage.split(";")[1];

                tx_prox.setText("Proximity detected: " + prox_value);

                Float value = Float.parseFloat(prox_value);
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

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        ////////////////////////////////////////////////////////////
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

    //CONNECTION METHODS
    private void connect() {
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
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}




