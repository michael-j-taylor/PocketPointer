package com.example.testmouseapp.dataOperations;

import java.util.ArrayList;
import java.util.List;


//used to calibrate accelerometer
public class Calibrater {

    private int total_readings;
    private int num_readings = 0;
    private float x_sum = 0;
    private float y_sum = 0;


    private ArrayList<Float> x_readings = new ArrayList<>();
    private ArrayList<Float> y_readings = new ArrayList<>();

    private float magnitude_offset = 0;
    private float x_offset = 0;
    private float y_offset = 0;

    public boolean calibrating = false;
    public double x_threshold;
    public double y_threshold;
    public double magnitude_threshold;


    public Calibrater(int total_readings) {
        this.total_readings = total_readings;
    }


    public void addDataPoint(float reading_x, float reading_y) {
        this.num_readings++;
        this.x_sum += reading_x;
        this.y_sum += reading_y;

        this.x_readings.add(reading_x);
        this.y_readings.add(reading_y);
    }


    public void calibrate(float reading_x, float reading_y) {

        if (num_readings == total_readings) {

            calculateOffsets();
            calculateThresholds(10);  //calculate threshold w/ top outliers
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


    public void calculateThresholds(int num_edge_readings) {

        //x threshold
        if (this.x_offset >= 0) {  //avg x reading is positive value
            float x_topreadings = this.averageTopReadings(this.x_readings, num_edge_readings);

            this.x_threshold = x_topreadings + 0.2 * x_topreadings;
        } else {
            float x_bottomreadings = this.averageBottomReadings(this.x_readings, num_edge_readings);

            this.x_threshold = x_bottomreadings + 0.2 * x_bottomreadings;
        }


        //y threshold
        if (this.y_offset >= 0) {  //avg y reading is positive value
            float y_topreadings = this.averageTopReadings(this.y_readings, num_edge_readings);

            this.y_threshold = y_topreadings + 0.2 * y_topreadings;
        } else {
            float y_bottomreadings = this.averageBottomReadings(this.y_readings, num_edge_readings);

            this.y_threshold = y_bottomreadings + 0.2 * y_bottomreadings;
        }


        //magnitude threshold
        this.magnitude_threshold = (float) Math.sqrt(Math.pow(this.x_threshold, 2) + Math.pow(this.y_threshold, 2));

    }


    public float averageTopReadings(ArrayList<Float> readings, int num_readings) {

        //list of last 5 elements of readings List
        List<Float> topReadings = readings.subList(readings.size() - num_readings, readings.size());

        float sum = 0;
        for (int i = 0; i < num_readings; i++) {
            sum += topReadings.indexOf(i);
        }

        return sum/num_readings;
    }


    public float averageBottomReadings(ArrayList<Float> readings, int num_readings) {

        //create list of bottom 5 readings
        List<Float> bottomReadings = readings.subList(0, num_readings);

        float sum = 0;
        for (int i = 0; i < num_readings; i++) {
            sum += bottomReadings.indexOf(i);
        }

        return sum/num_readings;
    }



}
