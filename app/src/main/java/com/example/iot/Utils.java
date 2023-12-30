package com.example.iot;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

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


public class Utils {

    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "LightsActivity";
    private static Utils instance;

    public String outputvalue;

    private Context context;

    public float lux_value_received;
    public float prox_value_received;
    public float temp_value_received;

    private Utils(Context context) {
        this.context=context;
        connect();


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
                String prox_value = newMessage.split(";")[1];

                Float l_value = Float.parseFloat(lux_value);
                Float p_value = Float.parseFloat(prox_value);

                lux_value_received=l_value;
                prox_value_received=p_value;
                getTempSensor();
                Log.i("Lux control", "Received: "+lux_value_received);
                Log.i("Prox control", "Received: "+prox_value_received);
                Log.i("Temp control", "Received: "+temp_value_received);

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }
    private void getTempSensor() {
        run("tdtool --list-sensors");
        String[] list=outputvalue.split("\t");
        String[] temp=list[4].split("=");
        temp_value_received=(Float.parseFloat(temp[1]));
        Log.i("trial", temp[1]);

    }

    public static synchronized Utils getInstance(Context context) {
        if (instance == null) {
            instance = new Utils(context);
        }
        return instance;
    }
    public void connect(){
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(context, SERVER_URI,
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
