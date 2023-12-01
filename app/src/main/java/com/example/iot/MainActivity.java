package com.example.iot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLights = findViewById(R.id.btnLights);
        Button btnAlarm = findViewById(R.id.btnAlarm);
        Button btnHeating = findViewById(R.id.btnHeating);
        Button btnThermometer = findViewById(R.id.btnThermometer);

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