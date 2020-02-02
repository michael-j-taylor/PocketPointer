package com.example.testmouseapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    Sensor accelerometer;

    //maximum and minimum acceleration values measured
    float xmax = 0;
    float xmin = 0;
    float ymax = 0;
    float ymin = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing accelerometer");

        //get sensor manager services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //get sensor (accelerometer in this case)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //setup listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);  //can be changed to different delays

        Log.d(TAG, "onCreate: Registered accelerometer listener");


    }

    //on sensor value change, display X and Z values
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0.0254 || event.values[0] < -0.0254) {  //attempt to ignore small values

            if (event.values[0] > xmax) {xmax = event.values[0];}
            if (event.values[0] < xmin) {xmin = event.values[0];}

            if (event.values[1] > ymax) { ymax = event.values[1];}
            if (event.values[1] < ymin) { ymin = event.values[1];}


            TextView live_acceleration;
            TextView max_acceleration;

            live_acceleration = (TextView)findViewById(R.id.acceleration);
            max_acceleration = (TextView)findViewById(R.id.maximums);

            //Log.d(TAG, "onSensorChanged: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
            String data_live = "X: " + event.values[0] + "\nY: " + event.values[1];
            String data_max = "X Maximum: " + xmax + "\nX Minimum: " + xmin + "\n\nY Maximum: " + ymax + "\nY Minimum: " + ymin;

            live_acceleration.setText(data_live);
            max_acceleration.setText(data_max);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
