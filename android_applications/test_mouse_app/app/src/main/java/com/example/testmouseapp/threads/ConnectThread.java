package com.example.testmouseapp.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.testmouseapp.activities.DevicesActivity;
import com.example.testmouseapp.services.BluetoothService;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final BluetoothService service;
    private final String TAG = "ConnectThread";
    private final BluetoothSocket mmSocket;
    private final Handler mmHandler;
    private boolean running = true;
    private boolean connected = false;
    private CommunicationThread mmCommunicationThread = null;

    public ConnectThread(BluetoothDevice device, Handler handler, BluetoothService service) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        mmHandler = handler;
        this.service = service;
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice based on the program's UUID
            UUID mm_uuid = DevicesActivity.getUuid();
            tmp = device.createRfcommSocketToServiceRecord(mm_uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {

            while (running) {
                while (mmCommunicationThread != null && mmCommunicationThread.isRunning()) {
                    sleep(1000);
                }
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                Log.d(TAG, "attempting to connect");
                mmSocket.connect();
                Log.d(TAG, "Connected");
                // The connection attempt succeeded. Perform work associated with the connection in a separate thread.
                mmCommunicationThread = new CommunicationThread(mmSocket, mmHandler);
                mmCommunicationThread.start();

                successfulConnection();
                sleep(100);
                //Log.d(TAG, "End of iteration");
            }
        } catch (Exception connectException) {
            // Unable to connect; close the socket and return
            failedConnection();
            cancel();
        }
    }

    private void successfulConnection() {
        connected = true;
        Log.d(TAG, "Update");
        service.successfulConnection();

        // Share the sent message with the UI activity.
        Message toastMsg = mmHandler.obtainMessage(
                BluetoothService.MessageConstants.MESSAGE_TOAST, -1, -1, "Connected to " + service.device.getName());
        toastMsg.sendToTarget();
    }

    private void failedConnection() {
        Log.e(TAG, "Failed to connect to " + service.device.getName());

        // Share the sent message with the UI activity.
        Message toastMsg = mmHandler.obtainMessage(
                BluetoothService.MessageConstants.MESSAGE_TOAST, -1, -1, "Failed to connect to " + service.device.getName());
        toastMsg.sendToTarget();
    }

    public CommunicationThread getCommunicationThread() {
        return mmCommunicationThread;
    }

    public boolean isConnected() {
        return connected;
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            running = false;
            connected = false;
            if (mmCommunicationThread != null) {
                mmCommunicationThread.cancel();
            }
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
