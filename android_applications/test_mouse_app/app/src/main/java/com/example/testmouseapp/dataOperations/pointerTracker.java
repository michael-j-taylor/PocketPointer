package com.example.testmouseapp.dataOperations;


import android.util.Log;
import android.view.MotionEvent;

//intended to track x and y coordinates of pointer on device touchscreen
public class pointerTracker {

    private static final String TAG = "pointerTracker";

    private double x1;
    private double y1;
    private double dx;
    private double dy;

    public pointerTracker() {
        x1 = 0;
        y1 = 0;
        dx = 0;
        dy = 0;
    }

    public void setMouseCoordinates(MotionEvent event) {
        double x2 = x1;
        double y2 = y1;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                this.x1 = event.getX();
                this.y1 = event.getY();
                x2 = x1;
                y2 = y1;
                Log.d(TAG, "initialized with x: " + this.x1 + " | y: " + this.y1);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                this.x1 = event.getX();
                this.y1 = event.getY();
                Log.d(TAG, "x: " + this.x1 + " | y: " + this.y1);
                break;
            }

        }

        dx = x1 - x2;
        dy = y1 - y2;
    }

    public double getDeltaX() {
        return dx;
    }

    public double getDeltaY() {
        return dy;
    }
}
