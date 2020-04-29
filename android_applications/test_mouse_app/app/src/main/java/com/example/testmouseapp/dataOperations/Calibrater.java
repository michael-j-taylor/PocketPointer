package com.example.testmouseapp.dataOperations;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//used to calibrate accelerometer
public class Calibrater {

    private int total_readings;
    private int num_readings = 0;
    private float x_sum = 0;
    private float y_sum = 0;

    private ArrayList<Float> x_readings = new ArrayList<>();
    private ArrayList<Float> y_readings = new ArrayList<>();

    //public float magnitude_offset = 0;
    public float x_offset = 0;
    public float y_offset = 0;
    public float x_threshold;
    public float y_threshold;
    public float magnitude_threshold;

    private static final String TAG = "Cailbrater";
    public boolean calibrating;

    public Calibrater(int total_readings) {
        this.total_readings = total_readings;
        calibrating = true;
    }

    public void addDataPoint(float reading_x, float reading_y) {
        num_readings++;
        x_sum += reading_x;
        y_sum += reading_y;

        x_readings.add(reading_x);
        y_readings.add(reading_y);
    }

    public void calibrate(float reading_x, float reading_y) {
        //calibration complete
        if (num_readings >= total_readings) {
            x_offset = x_sum/num_readings;
            y_offset =  y_sum/num_readings;
            calculateThresholds(10);
            num_readings = 0;
            calibrating = false;
            x_sum = 0;
            y_sum = 0;
            Log.d(TAG, "calibrating set to false");
            Log.d(TAG, "offsets set to " + x_offset + " " + y_offset);
        }
        else {
            addDataPoint(reading_x, reading_y);
        }
    }
    /* In order to calculate the bets offset, first an average of the top and bottom n readings is calculated.
    Next a fraction of that offset is calculated and added to the original number.*/
    private void calculateThresholds(int num_edge_readings) {
        Collections.sort(x_readings);
        Collections.sort(y_readings);
        if (x_offset > 0) {
            x_threshold = averageTopReadings(x_readings, num_edge_readings);
        }
        else {
            x_threshold= averageBottomReadings(x_readings, num_edge_readings);
        }

        if (y_threshold > 0) {
            y_threshold = averageTopReadings(y_readings, num_edge_readings);
        }
        else {
            y_threshold = averageBottomReadings(y_readings, num_edge_readings);
        }
        x_readings.removeAll(x_readings);
        y_readings.removeAll(y_readings);
        magnitude_threshold = (float) Math.sqrt(Math.pow(x_threshold, 2) + Math.pow(y_threshold, 2)) * 1.2f;
        Log.d(TAG, "Threshold at: " + magnitude_threshold + " " + x_threshold + " " + y_threshold);
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
