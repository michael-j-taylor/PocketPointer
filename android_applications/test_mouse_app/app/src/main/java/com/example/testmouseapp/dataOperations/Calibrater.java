package com.example.testmouseapp.dataOperations;

import android.hardware.SensorEventListener;

import androidx.appcompat.app.AppCompatActivity;

//used to calibrate acclerometer
public class Calibrater  {

    private int num_readings;
    private float x_offset;
    private float y_offset;

    public boolean calibrating = false;

    public Calibrater (int num_readings) {
        this.num_readings = num_readings;
    }


    public void calibrate(float xval, float yval) {
        if (!this.calibrating) {
            this.calibrating = true;
        }

    }


}
