package com.example.testmouseapp.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.threads.CommunicationThread;
import com.example.testmouseapp.threads.ConnectThread;

public class BluetoothService extends Service {
    private static final String TAG = "Bluetooth Service";
    // Binder given to clients
    private final IBinder mm_binder = new LocalBinder();

    public BluetoothDevice device;
    private CommunicationThread mm_coms = null;
    private ConnectThread mm_connection = null;
    private boolean mm_remote_killed = false;
    private ServiceCallback mm_callback;

    @SuppressLint("HandlerLeak")
    public Handler mm_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MessageConstants.MESSAGE_READ) {
                //Get message parts for log
                String type = PPMessage.toString((byte) msg.arg2);
                String text = (String) msg.obj;

                text = text.trim();

                //Log message
                //Toast.makeText(getApplicationContext(), "Got: " + type + text, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Got: " + type + text);


                PPMessage m = new PPMessage((byte) msg.arg2, text);
                //TODO Do something with the message here.

                //If message is notification to terminate, do so
                if (m.what == PPMessage.Command.END) {
                    //Shut down BluetoothService
                    Toast.makeText(getApplicationContext(), "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
                    mm_remote_killed = true;
                    closeConnection();
                }

            } else if (msg.what == MessageConstants.MESSAGE_WRITE) {
                //Get message parts for log
                String type = PPMessage.toString((byte) msg.arg2);
                String text = (String) msg.obj;

                //Log message
                //Toast.makeText(getApplicationContext(), "Sent: " + type + text, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Sent: " + type + text);

            } else if (msg.what == MessageConstants.MESSAGE_TOAST) {
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();

            } else if (msg.what == MessageConstants.CONNECT_SUCCEEDED) {
                Toast.makeText(getApplicationContext(), "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
                mm_coms = mm_connection.getCommunicationThread();
                mm_callback.updateConnection();

            } else {
                Log.e(TAG, "Received bad message code from handler: " + msg.what);
            }
        }
    };

    //Used to interpret Bluetooth messages
    public interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;
        int CONNECT_SUCCEEDED = 3;
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothService.this;
        }
    }

    public interface ServiceCallback {
        void updateConnection();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mm_binder;
    }

    public void setCallbacks(ServiceCallback callbacks) {
        mm_callback = callbacks;
    }

    public void openConnection(BluetoothDevice d) {
        //Ensure comm channel has been established before moving on
        device = d;

        mm_connection = new ConnectThread(device, mm_handler, this);
        mm_connection.start();
        Log.d(TAG, "Started connectThread");
    }

    public void closeConnection() {
        if (mm_coms != null && !mm_remote_killed) {
            writeMessage(new PPMessage(PPMessage.Command.END, "Client ending activity"));
            mm_coms = null;
        }
        //Shut down communicationsThread and connectThread
        if (mm_connection != null && mm_connection.isConnected())
            mm_connection.cancel();

        device = null;
    }

    public void writeMessage(PPMessage m) {
        if (mm_coms == null) {
            Log.e(TAG, "Tried to write to null mm_coms");
            Toast.makeText(getApplicationContext(), "Not connected to any device", Toast.LENGTH_SHORT).show();
            throw new IllegalStateException();
        }
        mm_coms.write(m);
    }

    public boolean isConnected() {
        if (mm_connection == null) return false;
        return mm_connection.isConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Allow processes to rebind to service
        return true;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Shutting down", Toast.LENGTH_SHORT).show();
        closeConnection();
        super.onDestroy();
    }
}