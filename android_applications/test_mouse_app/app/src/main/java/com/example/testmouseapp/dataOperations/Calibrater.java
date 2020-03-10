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

        /*
        In order to calculate the bets offset, first an average of the top and bottom n readings is calculated.
        Next a fraction of that offset is calculated and added to the original number.
        */

        Collections.sort(this.x_readings);
        Collections.sort(this.y_readings);

        float x_avg = this.x_sum/this.num_readings;
        float y_avg = this.y_sum/this.num_readings;

        if (x_avg > 0) {
            this.x_offset = averageTopReadings(this.x_readings, 10);
        }
        else {
            this.x_offset= averageBottomReadings(this.x_readings, 10);
        }

        if (y_avg > 0) {
            this.y_offset = averageTopReadings(this.y_readings, 10);
        }
        else {
            this.y_offset = averageBottomReadings(this.y_readings, 10);
        }

        //this.x_offset = (float) adjustOffset(x_avg, 0.1);
        //this.y_offset = (float) adjustOffset(y_avg, 0.1);
        //this.magnitude_offset = calculateMagnitudeOffset(x_offset, y_offset);
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
