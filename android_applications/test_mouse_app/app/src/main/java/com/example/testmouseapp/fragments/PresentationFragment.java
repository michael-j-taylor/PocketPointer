package com.example.testmouseapp.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.services.BluetoothService;

import java.util.Objects;

public class PresentationFragment extends Fragment {

    private static final String TAG = "Presentation Activity";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind to BluetoothService
        Intent intent = new Intent(getContext(), BluetoothService.class);
        Objects.requireNonNull(getActivity()).bindService(intent, mm_connection, Context.BIND_AUTO_CREATE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_presentation, container, false);

        //Register nextslide button listener
        Button button_nextslide = view.findViewById(R.id.button_nextslide);
        button_nextslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextSlide();
            }
        });

        //Register prevslide button listener
        Button button_prevslide = view.findViewById(R.id.button_prevslide);
        button_prevslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousSlide();
            }
        });

        return view;
    }

    private void nextSlide() {
        try {
            mm_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "RIGHT"));
        } catch (IllegalStateException ignored) { }
    }

    private void previousSlide() {
        try {
            mm_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "LEFT"));
        } catch (IllegalStateException ignored) { }
    }

    public void onDestroy() {
        if (mm_bound) {
            Objects.requireNonNull(getActivity()).unbindService(mm_connection);
            mm_bound = false;
        }
        super.onDestroy();
    }
}
