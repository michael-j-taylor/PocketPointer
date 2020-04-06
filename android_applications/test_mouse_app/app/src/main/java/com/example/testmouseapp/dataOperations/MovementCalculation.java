package com.example.testmouseapp.dataOperations;

import android.hardware.SensorEvent;

import java.util.Calendar;


public class MovementCalculation {
    public static float[] averageAccel(SensorEvent event, int pollRate)
    {
        int count = 0;
        double pollTime = 1000/pollRate;
        long startTime = Calendar.getInstance().getTimeInMillis();
        long currentTime = 0;
        double sumX = 0, sumY = 0;
        do {
            currentTime = Calendar.getInstance().getTimeInMillis();
            sumX += event.values[0];
            sumY += event.values[1];
            count++;
        } while(currentTime - startTime < pollTime);
        float averages[] = {(float)sumX/count, (float)sumY/count};
        return averages;
    }
}
