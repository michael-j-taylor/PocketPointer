package com.example.testmouseapp.dataOperations;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//calculates thresholds for x, y, and magnitude given offsets from a calibrated accelerometer and a percentage
public class Filter {

    public float y_threshold;
    public float x_threshold;
    public float magnitude_threshold;

    private Calibrater calibrater;

    public Filter(Calibrater calibrater, double percent) {

        this.calibrater = calibrater;
        this.magnitude_threshold = calculateMagnitudeThreshold(this.x_threshold, this.y_threshold);
    }


    //adjust given x or y offset by percent
    /*
    private double calculateThreshold(ArrayList<Float> readings, double percent) {

        Collections.sort(readings);
        float x_avg = this.calibrater.x_offset / readings.size();
        float y_avg = this.calibrater.y_offset / readings.size();

        if (x_avg > 0) {

        }
        return avg_threshold + (avg_threshold*percent);
    }
     */


    //calculate threshold for magnitude, using previously calculated x and y thresholds
    private float calculateMagnitudeThreshold(float x_threshold, float y_threshold) {
        return (float) Math.sqrt(Math.pow(x_threshold, 2) + Math.pow(y_threshold, 2));
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
