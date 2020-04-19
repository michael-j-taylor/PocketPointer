package com.example.testmouseapp.activities;

import android.os.Bundle;

import com.example.testmouseapp.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.testmouseapp.dataOperations.PPOnSwipeListener;

public class TouchpadActivity extends AppCompatActivity {

    private GestureDetectorCompat PPGestureDetector;

    private static final String TAG = "TouchpadActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);

        PPGestureDetector = new GestureDetectorCompat(this, new PPOnSwipeListener() {

            //onSingleTapConfirmed only triggers once the device is sure that another tap isn't
            //going to occur (i.e., in a doubletap)
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                return true;
                //send bluetooth message here
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                return true;
            }

            @Override
            public boolean onSwipe(PPOnSwipeListener.Direction direction) {
                if (direction == PPOnSwipeListener.Direction.up) {
                    Log.d(TAG, "swipe up");
                    //send bluetooth message here
                }

                if (direction == PPOnSwipeListener.Direction.down) {
                    Log.d(TAG, "swipe down");
                }

                if (direction == PPOnSwipeListener.Direction.left) {
                    Log.d(TAG, "swipe left");
                }

                if (direction == PPOnSwipeListener.Direction.right) {
                    Log.d(TAG, "swipe right");
                }

                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.PPGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
