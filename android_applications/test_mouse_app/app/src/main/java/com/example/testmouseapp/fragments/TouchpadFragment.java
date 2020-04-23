package com.example.testmouseapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.dataOperations.PPOnSwipeListener;

public class TouchpadFragment extends Fragment {
    private static final String TAG = "Touchpad Fragment";

    private MainActivity mm_main_activity;

    private GestureDetectorCompat PPGestureDetector;
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_touchpad, container, false);

        mm_main_activity = (MainActivity) getActivity();
        assert mm_main_activity != null;

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
                PPGestureDetector.onTouchEvent(event);
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
