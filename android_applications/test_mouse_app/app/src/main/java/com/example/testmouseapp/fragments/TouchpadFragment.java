package com.example.testmouseapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.dataOperations.PPOnSwipeListener;
import com.example.testmouseapp.dataOperations.pointerTracker;

public class TouchpadFragment extends Fragment {
    private static final String TAG = "Touchpad Fragment";

    private MainActivity mm_main_activity;

    private pointerTracker PPpointerTracker;
    private GestureDetectorCompat PPGestureDetector;
    private View view;

    private boolean mouseLock = false;  //determines if swipe data is sent or pointer coordinates on touchpad

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //hide action bar for this fragment
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        view = inflater.inflate(R.layout.fragment_touchpad, container, false);

        mm_main_activity = (MainActivity) getActivity();
        assert mm_main_activity != null;

        //button creation
        ToggleButton button_mouse_lock = view.findViewById(R.id.button_mouseLock);
        button_mouse_lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mouseLock = true;
                } else {
                    mouseLock = false;
                }
            }
        });

        //Create GestureDetector for activity (in this case, we use our own PPGestureDetector class
        //instead of an android-provided one
        PPpointerTracker = new pointerTracker();
        PPGestureDetector = new GestureDetectorCompat(getContext(), new PPOnSwipeListener() {

            //onSingleTapConfirmed only triggers once the device is sure that another tap isn't
            //going to occur (i.e., in a doubletap)
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                try {
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.TAP, "SOMEWHERE"));
                } catch (IllegalStateException ignored) { }

                return true;
            }


            @Override
            public boolean onDoubleTap(MotionEvent event) {
                try {
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.DOUBLETAP, "SOMEWHERE"));
                } catch (IllegalStateException ignored) { }

                return true;
            }


            @Override
            public boolean onSwipe(PPOnSwipeListener.Direction direction) {
                if (direction == PPOnSwipeListener.Direction.up) {
                    Log.d(TAG, "swipe up");

                    try {
                        mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "UP"));
                    } catch (IllegalStateException ignored) { }
                }

                if (direction == PPOnSwipeListener.Direction.down) {
                    Log.d(TAG, "swipe down");

                    try {
                        mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "DOWN"));
                    } catch (IllegalStateException ignored) { }
                }

                if (direction == PPOnSwipeListener.Direction.left) {
                    Log.d(TAG, "swipe left");

                    try {
                        mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "LEFT"));
                    } catch (IllegalStateException ignored) { }
                }

                if (direction == PPOnSwipeListener.Direction.right) {
                    Log.d(TAG, "swipe right");

                    try {
                        mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "RIGHT"));
                    } catch (IllegalStateException ignored) { }
                }

                return true;
            }
        });


        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (mouseLock) {  //if mouse lock enabled, send x and y coordinates of pointer
                    PPpointerTracker.setMouseCoordinates(event);
                } else {  //capture gesture from swipe
                    PPGestureDetector.onTouchEvent(event);
                }

                return true;
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        TextView device_view = view.findViewById(R.id.touchpadDeviceText);
        if (mm_main_activity.bt_service != null && mm_main_activity.bt_service.isConnected()) {
            String s = "Connected to " + mm_main_activity.bt_service.device.getName();
            device_view.setText(s);
        } else {
            device_view.setText(R.string.not_connected);
        }
    }

}
