package com.example.testmouseapp.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.dataOperations.PPOnSwipeListener;
import com.example.testmouseapp.services.BluetoothService;

import java.util.Objects;

public class TouchpadFragment extends Fragment {
    private static final String TAG = "Touchpad Fragment";

    private GestureDetectorCompat PPGestureDetector;
    private View view;

    //Bluetooth vars
    private BluetoothService mm_service;
    private boolean mm_bound;
    private ServiceConnection mm_connection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mm_service = binder.getService();
            mm_bound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "onServiceDisconnected");
            mm_bound = false;
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind to BluetoothService
        Intent intent = new Intent(getContext(), BluetoothService.class);
        Objects.requireNonNull(getActivity()).bindService(intent, mm_connection, Context.BIND_AUTO_CREATE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_touchpad, container, false);

        PPGestureDetector = new GestureDetectorCompat(getContext(), new PPOnSwipeListener() {

            //onSingleTapConfirmed only triggers once the device is sure that another tap isn't
            //going to occur (i.e., in a doubletap)
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                try {
                    mm_service.writeMessage(new PPMessage(PPMessage.Command.TAP, "SOMEWHERE"));
                } catch (IllegalStateException ignored) { }

                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                try {
                    mm_service.writeMessage(new PPMessage(PPMessage.Command.DOUBLETAP, "SOMEWHERE"));
                } catch (IllegalStateException ignored) { }

                return true;
            }

            @Override
            public boolean onSwipe(PPOnSwipeListener.Direction direction) {
                if (direction == PPOnSwipeListener.Direction.up) {
                    Log.d(TAG, "swipe up");

                    try {
                        mm_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "UP"));
                    } catch (IllegalStateException ignored) { }
                }

                if (direction == PPOnSwipeListener.Direction.down) {
                    Log.d(TAG, "swipe down");

                    try {
                        mm_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "DOWN"));
                    } catch (IllegalStateException ignored) { }
                }

                if (direction == PPOnSwipeListener.Direction.left) {
                    Log.d(TAG, "swipe left");

                    try {
                        mm_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "LEFT"));
                    } catch (IllegalStateException ignored) { }
                }

                if (direction == PPOnSwipeListener.Direction.right) {
                    Log.d(TAG, "swipe right");

                    try {
                        mm_service.writeMessage(new PPMessage(PPMessage.Command.SWIPE, "RIGHT"));
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

    public void onDestroy() {
        if (mm_bound) {
            Objects.requireNonNull(getActivity()).unbindService(mm_connection);
            mm_bound = false;
        }
        super.onDestroy();
    }
}
