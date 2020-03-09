package com.example.testmouseapp.dataOperations;

//used to calibrate acclerometer
public class Calibrater {

    private int num_readings;

    public boolean calibrating = false;

    public Calibrater(int num_readings) {
        this.num_readings = num_readings;
    }


}
