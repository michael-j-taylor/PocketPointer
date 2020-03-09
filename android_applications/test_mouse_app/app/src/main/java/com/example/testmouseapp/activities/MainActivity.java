package com.example.testmouseapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.testmouseapp.R;

import com.example.testmouseapp.dataOperations.MovingAverage;
import com.example.testmouseapp.dataOperations.Filter;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    Sensor accelerometer;

    //maximum and minimum acceleration values measured
    float xmax = 0;
    float xmin = 0;
    float ymax = 0;
    float ymin = 0;

    //accelerometer bounds
    float x_pos_bound;
    float x_neg_bound;
    float y_pos_bound;
    float y_neg_bound;

    //printed accelerometer values
    float val_x;
    float val_y;

    //calibration vars
    boolean calibrating = false;
    int num_readings = 0;
    int readings_max = 10000;  //change this to determine how many readings the accelerometer calibrates on
    float x_total;
    float y_total;
    float x_pad = 0;
    float y_pad = 0;

    //bluetooth vars


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

        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activateCalibrate(v);
            }
        });
    }

    //on sensor value change, display X and Z values
    @Override
    public void onSensorChanged(SensorEvent event) {

        TextView live_acceleration;
        TextView max_acceleration;

        live_acceleration = findViewById(R.id.acceleration);
        max_acceleration = findViewById(R.id.maximums);

        if (calibrating) {
            live_acceleration.setText("Calibrating");
            calibrateAccelerometer(event);
        }

        else {

            //if (event.values[0] > 0.0254 || event.values[0] < -0.0254) {  //attempt to ignore small values
            if (true) {

                if (event.values[0] > xmax) {xmax = event.values[0];}
                if (event.values[0] < xmin) {xmin = event.values[0];}

                if (event.values[1] > ymax) { ymax = event.values[1];}
                if (event.values[1] < ymin) { ymin = event.values[1];}

                val_x = event.values[0] + x_pad;
                val_y = event.values[1] + y_pad;

                //Log.d(TAG, "onSensorChanged: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
                String data_live = "X: " + val_x + "\nY: " + val_y;
                String data_max = "X Maximum: " + xmax + "\nX Minimum: " + xmin + "\n\nY Maximum: " + ymax + "\nY Minimum: " + ymin;

                live_acceleration.setText(data_live);
                max_acceleration.setText(data_max);
            }

            else {

                String data_live = "X: " + 0 + "\nY: " + 0;
                String data_max = "X Maximum: " + xmax + "\nX Minimum: " + xmin + "\n\nY Maximum: " + ymax + "\nY Minimum: " + ymin;

                live_acceleration.setText(data_live);
                max_acceleration.setText(data_max);
            }
        }

    }

    public void calibrateAccelerometer(SensorEvent event) {
        num_readings += 1;
        xmax = 0;
        ymax = 0;
        xmin = 0;
        ymin = 0;

        if (num_readings > readings_max) {
            x_total += event.values[0];
            y_total += event.values[1];
        }

        else {
            x_pad = x_total / readings_max;
            y_pad = y_total / readings_max;

            calibrating = false;
            num_readings = 0;
            Log.d(TAG, "accelerometer calibrated");
        }
    }

    public void activateCalibrate(View view) {
        calibrating = true;
        x_total = 0;
        y_total = 0;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void checkBluetooth(View view) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            String noBtMsg = "Your device does not support Bluetooth. Please connect using a USB cable.";
            Snackbar noBtSnackbar = Snackbar.make(findViewById(R.id.MainActivityCoordinator), noBtMsg, Snackbar.LENGTH_LONG);
            noBtSnackbar.show();
        }
        else {
            String hasBtMsg = "Your device suppports Bluetooth.";
            Snackbar hasBtSnackbar = Snackbar.make(findViewById(R.id.MainActivityCoordinator), hasBtMsg, Snackbar.LENGTH_LONG);
            hasBtSnackbar.show();
        }
    }
}

