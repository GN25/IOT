package com.example.iot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private Utils utils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton btnLights = findViewById(R.id.btnLights);
        ImageButton btnAlarm = findViewById(R.id.btnAlarm);
        ImageButton btnHeating = findViewById(R.id.btnHeating);
        ImageButton btnThermometer = findViewById(R.id.btnThermometer);

        this.utils=Utils.getInstance(getApplicationContext());

        btnLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LightsActivity.class));
            }
        });

        btnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AlarmActivity.class));
            }
        });

        btnHeating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HeatingActivity.class));
            }
        });

        btnThermometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ThermometerActivity.class));
            }
        });
    }
}