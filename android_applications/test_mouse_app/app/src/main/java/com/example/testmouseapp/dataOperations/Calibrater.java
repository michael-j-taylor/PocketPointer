package com.example.testmouseapp.dataOperations;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

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

    private ArrayList<Float> x_readings = new ArrayList<>();
    private ArrayList<Float> y_readings = new ArrayList<>();

    public float magnitude_offset = 0;
    public float x_offset = 0;
    public float y_offset = 0;
    public float x_threshold;
    public float y_threshold;
    public float magnitude_threshold;

    private static final String TAG = "Cailbrater";

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

        //calibration complete
        if (num_readings == total_readings) {

            this.x_offset = this.x_sum/this.num_readings;
            this.y_offset =  this.y_sum/this.num_readings;
            calculateThresholds(10);
            this.num_readings = 0;
            this.calibrating = false;
            Log.d(TAG, "calibration set to false");
        }

        else {
            addDataPoint(reading_x, reading_y);
        }
    }


    private void calculateThresholds(int num_edge_readings) {

        /*
        In order to calculate the bets offset, first an average of the top and bottom n readings is calculated.
        Next a fraction of that offset is calculated and added to the original number.
        */

        Collections.sort(this.x_readings);
        Collections.sort(this.y_readings);


        if (this.x_offset > 0) {
            this.x_threshold = averageTopReadings(this.x_readings, 10);
        }
        else {
            this.x_threshold= averageBottomReadings(this.x_readings, 10);
        }

        if (this.y_threshold > 0) {
            this.y_threshold = averageTopReadings(this.y_readings, 10);
        }
        else {
            this.y_threshold = averageBottomReadings(this.y_readings, 10);
        }

        this.x_threshold += 0.2*this.x_threshold;
        this.y_threshold += 0.2*this.y_threshold;


        this.magnitude_threshold = (float) Math.sqrt(Math.pow(this.x_threshold, 2) + Math.pow(this.y_threshold, 2));
        Log.d(TAG, "Threshold at: " + Float.toString(this.magnitude_threshold));
    }


    private float averageTopReadings(ArrayList<Float> readings, int num_readings) {

        //list of last 5 elements of readings List
        List<Float> topReadings = readings.subList(readings.size() - num_readings, readings.size());

        float sum = 0;
        for (int i = 0; i < num_readings; i++) {
            sum += topReadings.indexOf(i);
        }

        return sum/num_readings;
    }


    private float averageBottomReadings(ArrayList<Float> readings, int num_readings) {

        //create list of bottom 5 readings
        List<Float> bottomReadings = readings.subList(0, num_readings);

        float sum = 0;
        for (int i = 0; i < num_readings; i++) {
            sum += bottomReadings.indexOf(i);
        }

        return sum/num_readings;
    }

}
