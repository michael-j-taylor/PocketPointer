package com.example.testmouseapp.dataOperations;



public class Filter {

    private float x_pos_offset;
    private float x_neg_offset;
    private float y_pos_offset;
    private float y_neg_offset;
    private float magnitude_offset;

    public Filter(float x_pos_offset, float x_neg_offset, float y_pos_offset, float y_neg_offset) {
        this.x_pos_offset = x_pos_offset;
        this.x_neg_offset = x_neg_offset;
        this.y_pos_offset = y_pos_offset;
        this.y_neg_offset = y_neg_offset;
    }


    public Filter(float magnitude_offset) {
        this.magnitude_offset = magnitude_offset;
    }


    //return true if data over/under offset values calculated during sensor calibration
    public boolean overThreshold(float xdata, float ydata) {
        if ( (xdata > x_pos_offset || xdata < x_neg_offset) || (ydata > y_pos_offset || ydata < y_neg_offset) ) {
            return true;
        }
        return false;
    }


    //return true if magnitude over offset value calculated during sensor calibration
    public boolean overThreshold(double magnitude) {
        if (magnitude > this.magnitude_offset) {
            return true;
        }
        return false;
    }
}
