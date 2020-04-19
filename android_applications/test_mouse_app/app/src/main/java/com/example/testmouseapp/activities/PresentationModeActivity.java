package com.example.testmouseapp.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testmouseapp.R;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.services.BluetoothService;

public class PresentationModeActivity extends AppCompatActivity {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation_mode);

        // Bind to BluetoothService
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mm_connection, Context.BIND_AUTO_CREATE);
    }


    public void nextSlide(View view) {
        try {
            mm_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "RIGHT"));
        } catch (IllegalStateException ignored) { }
    }

    public void previousSlide(View view) {
        try {
            mm_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "LEFT"));
        } catch (IllegalStateException ignored) { }
    }

    public void onDestroy() {
        if (mm_bound) {
            unbindService(mm_connection);
            mm_bound = false;
        }
        super.onDestroy();
    }
}
