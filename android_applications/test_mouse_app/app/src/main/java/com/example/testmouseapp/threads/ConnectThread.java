package com.example.testmouseapp.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.example.testmouseapp.activities.DevicesActivity;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final Object lock;
    private final String TAG = "ConnectThread";
    private final BluetoothSocket mmSocket;
    private final Handler mmHandler;
    private boolean running = true;
    private boolean connected = false;
    private CommunicationThread mmCommunicationThread = null;

    public ConnectThread(BluetoothDevice device, Handler handler, Object lock) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        mmHandler = handler;
        this.lock = lock;
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
                mmCommunicationThread = new CommunicationThread(mmSocket, mmHandler, lock);
                synchronized (lock) {
                    //Log.d(TAG, "In sync");
                    mmCommunicationThread.start();
                    connected = true;
                    //Log.d(TAG, "Update");
                    lock.notify();
                    //Log.d(TAG, "Notify");
                }
                //Log.d(TAG, "Out of sync");
                sleep(100);
                //Log.d(TAG, "End of iteration");
            }
        } catch (Exception connectException) {
            // Unable to connect; close the socket and return
            cancel();
        }
    }

    public CommunicationThread getCommunicationThread() {
        return mmCommunicationThread;
    }

    public boolean isRunning() {
        return running;
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
