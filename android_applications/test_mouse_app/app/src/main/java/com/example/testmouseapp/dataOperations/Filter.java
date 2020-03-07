package com.example.testmouseapp.dataOperations;

public class Filter {

    private float xmax;
    private float xmin;
    private float ymax;
    private float ymin;

    public Filter(float xmax, float xmin, float ymax, float ymin) {
        this.xmax = xmax;
        this.xmin = xmin;
        this.ymax = ymax;
        this.ymin = ymin;
    }

    //return false if data is between specified maximun and minumum x and y values
    public boolean filterData(float xdata, float ydata) {
        if ( (xdata < xmax && xdata > xmin) || (ydata < ymax && ydata > ymin) ) {
            return false;
        }
        return true;
    }

}
