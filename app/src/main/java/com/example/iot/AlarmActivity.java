package com.example.iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class AlarmActivity extends AppCompatActivity {



    private static final String STATE_SENSING = "Sensing";
    private static final String STATE_SIMULATING = "Simulating";

    private Handler handler;
    private final int INTERVAL = 5000;

    private Slider sliderSimulation;
    private Button btApplySimulation;
    private TextView tvState;
    private TextView tvReceivedTemperature;

    private Button btNewAlarm;
    private float temp_threshold;
    private float tempValue;
    private boolean isSensing;
    private boolean isSimulating;
    private AlertDialog alertDialog;
    private Utils utils;

    private LinearLayout linearContainer;
    private List<View> alarmViews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // setContentView(R.layout.heating_activity);
        setContentView(R.layout.alarm_activity);

        utils=Utils.getInstance(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Alarm system");
        }

        tvState=findViewById(R.id.tvStateValue);
        sliderSimulation=findViewById(R.id.sliderTemperature);
        btApplySimulation=findViewById(R.id.btApplySimulation);
        tvReceivedTemperature=findViewById(R.id.tvReceivedTemperatureValue);
        btNewAlarm=findViewById(R.id.btNewAlarm);

        linearContainer = findViewById(R.id.linearContainer);


        btApplySimulation.setEnabled(false);
        sliderSimulation.setEnabled(false);
        temp_threshold= 20;
        tempValue=15;
        tvState.setText("None");
        tvReceivedTemperature.setText("-");

        alarmViews = new ArrayList<>();

        MaterialButtonToggleGroup toggleButtonTempMode = findViewById(R.id.toggleButtonTempAutomation);

        toggleButtonTempMode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.btSensor) {
                        tvReceivedTemperature.setText(utils.temp_value_received+"");
                        tvState.setText(STATE_SENSING);
                        btApplySimulation.setEnabled(false);
                        sliderSimulation.setEnabled(false);
                        isSensing=true;
                        isSimulating=false;
                        startRepeatingTask();

                    } else if (checkedId == R.id.btSimulation) {
                        tvReceivedTemperature.setText("-");
                        tvState.setText(STATE_SIMULATING);
                        btApplySimulation.setEnabled(true);
                        sliderSimulation.setEnabled(true);
                        isSensing=false;
                        isSimulating=true;
                        startRepeatingTask();
                    }


                } else {
                    tvState.setText("None");
                    btApplySimulation.setEnabled(false);
                    sliderSimulation.setEnabled(false);
                    isSensing=false;
                    isSimulating=false;
                    tvReceivedTemperature.setText("-");
                    stopRepeatingTask();
                }
            }
        });
        btApplySimulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempValue= sliderSimulation.getValue();
                tvReceivedTemperature.setText(tempValue+"");
            }
        });
        final AlertDialog[] alertDialog = {null};
        btNewAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createModal();

            }
        });



        handler = new Handler(Looper.getMainLooper());


    }

    private void createModal() {
        View modalView = getLayoutInflater().inflate(R.layout.modal_new_alarm, null);

        //creation
        AlertDialog.Builder builder = new AlertDialog.Builder(AlarmActivity.this);
        builder.setView(modalView);

        EditText editTextName = modalView.findViewById(R.id.editTextName);
        EditText editTextThreshold = modalView.findViewById(R.id.editTextThreshold);
        Button btAddAlarmInModal = modalView.findViewById(R.id.btAddAlarm);
        Spinner spComparer=modalView.findViewById(R.id.spComparer);

        // Configurar OnClickListener para el botón dentro del modal
        btAddAlarmInModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = editTextName.getText().toString();
                String threshold = editTextThreshold.getText().toString();
                String comparer = spComparer.getSelectedItem().toString();
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();

                    createNewAlarm(name, threshold, comparer);
                }
            }
        });

        alertDialog = builder.create();
        alertDialog.show();

    }

    private void createNewAlarm(String name, String threshold, String comparer) {
       // Log.i("Trial2", name+" - "+threshold);

        View newLayout = getLayoutInflater().inflate(R.layout.fragment_alarm, null);
        linearContainer.addView(newLayout);
        TextView tvAlarmName = newLayout.findViewById(R.id.tvAlarmName);
        TextView tvAlarmThreshold = newLayout.findViewById(R.id.tvAlarmThresholdValue);

        TextView tvAlarmComparer = newLayout.findViewById(R.id.tvAlarmComparer);
        tvAlarmComparer.setText(comparer);

        RadioButton rbAlarm=newLayout.findViewById(R.id.rbAlarm1);
        rbAlarm.setClickable(false);

        tvAlarmName.setText(name);
        tvAlarmThreshold.setText(threshold);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        newLayout.setLayoutParams(layoutParams);


        alarmViews.add(newLayout);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if(isSensing){
                tempValue = utils.temp_value_received;
                tvReceivedTemperature.setText(tempValue+"");

                checkLimits();

            }else if(isSimulating){

                checkLimits();

            }else{
                tvReceivedTemperature.setText("-");
            }

            handler.postDelayed(this, INTERVAL);
        }
    };

    private void checkLimits() {
        for (View alarmView : alarmViews) {
            TextView tvName = alarmView.findViewById(R.id.tvAlarmName);
            String name= (String) tvName.getText();
            TextView tvAlarmThreshold = alarmView.findViewById(R.id.tvAlarmThresholdValue);
            String threshold = tvAlarmThreshold.getText().toString();
            TextView tvComparer=alarmView.findViewById(R.id.tvAlarmComparer);
            String comparer= (String) tvComparer.getText();
            Float value = Float.valueOf(threshold);
            RadioButton rbAlarm=alarmView.findViewById(R.id.rbAlarm1);
            //HACER QUE COMPARE CON LA RECIBIDA Y ACTIVE EL RB
            if(comparer.equals("<")){
                if(tempValue<=value){
                    rbAlarm.setChecked(true);
                }else{
                    rbAlarm.setChecked(false);
                }
            }else if(comparer.equals(">")){
                if(tempValue>=value){
                    rbAlarm.setChecked(true);
                }else{
                    rbAlarm.setChecked(false);
                }
            }else{
                Log.e("Testing", "Esta fallando");
            }

            //Log.i("Testing",name+" - "+tempValue+ " <=? "+value);
        }
    }

    void startRepeatingTask() {
        runnable.run();
    }
    void stopRepeatingTask() {
        handler.removeCallbacks(runnable);
    }




}
