package com.example.testmouseapp.dataOperations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

//used to calibrate accelerometer
public class Calibrater {

    private int total_readings;
    private int num_readings = 0;
    private float x_sum = 0;
    private float y_sum = 0;
    private float x_max_reading = 0;
    private float y_max_reading = 0;

    private ArrayList<Float> x_readings = new ArrayList<>();
    private ArrayList<Float> y_readings = new ArrayList<>();

    public float magnitude_offset = 0;
    public float x_offset = 0;
    public float y_offset = 0;

    public boolean calibrating = false;

    public Calibrater(int total_readings) {
        this.total_readings = total_readings;
    }


    public void addDataPoint(float reading_x, float reading_y) {
        num_readings++;
        this.x_sum += reading_x;
        this.y_sum += reading_y;

        this.x_readings.add(reading_x);
        this.y_readings.add(reading_y);
    }


    public void calibrate(float reading_x, float reading_y) {

        if (!calibrating) {
            this.calibrating = true;
        }

        if (num_readings == total_readings) {

            calculateOffsets();
            this.calibrating = false;
        }

        else {
            addDataPoint(reading_x, reading_y);
        }
    }


    private void calculateOffsets() {

        this.x_offset = this.x_sum/this.num_readings;
        this.y_offset = this.y_sum/this.num_readings;
        this.magnitude_offset = (float) Math.sqrt(Math.pow(this.x_offset, 2) + Math.pow(this.y_offset, 2));
    }
}
