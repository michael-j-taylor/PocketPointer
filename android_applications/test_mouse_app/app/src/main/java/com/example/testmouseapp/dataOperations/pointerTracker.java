package com.example.testmouseapp.dataOperations;


import android.util.Log;
import android.view.MotionEvent;

//intended to track x and y coordinates of pointer on device touchscreen
public class pointerTracker {

    private static final String TAG = "pointerTracker";

    private float x, y;

    public pointerTracker() {
        this.x = 0;
        this.y = 0;
    }

    public void setMouseCoordinates(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                this.x = event.getX();
                this.y = event.getY();
                Log.d(TAG, "initialized with x: " + this.x + " | y: " + this.y);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                this.x = event.getX();
                this.y = event.getY();
                Log.d(TAG, "x: " + this.x + " | y: " + this.y);
                break;
            }
        }
    }


}
