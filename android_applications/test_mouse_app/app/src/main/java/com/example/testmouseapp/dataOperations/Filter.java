package com.example.testmouseapp.dataOperations;


//calculates thresholds for x, y, and magnitude given offsets from a calibrated accelerometer and a percentage
public class Filter {

    public float y_threshold;
    public float x_threshold;
    public float magnitude_threshold;

    public Filter(float x_offset, float y_offset, double percent) {
        this.x_threshold = (float) calculateThreshold(x_offset, percent);
        this.y_threshold = (float) calculateThreshold(y_offset, percent);
        this.magnitude_threshold = calculateMagnitudeThreshold(this.x_threshold, this.y_threshold);
    }


    //adjust given x or y offset by percent
    private double calculateThreshold(float offset, double percent) {
        if (offset > 0) {
            return offset + (offset*percent);
        }
        else if (offset < 0) {
            return offset - (offset*percent);
        }
        else return offset;
    }


    //calculate threshold for magnitude, using previously calculated x and y thresholds
    private float calculateMagnitudeThreshold(float x_threshold, float y_threshold) {
        return (float) Math.sqrt(Math.pow(x_threshold, 2) + Math.pow(y_threshold, 2));
    }
}
