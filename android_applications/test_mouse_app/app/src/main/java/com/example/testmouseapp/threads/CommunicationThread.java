package com.example.testmouseapp.threads;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.testmouseapp.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommunicationThread extends Thread {
    private static final String TAG = "CommunicationThread";
    private final BluetoothSocket mmSocket;
    private final Handler mmHandler;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private boolean running = true;
    private byte[] mmBuffer; // mmBuffer store for the stream

    CommunicationThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        mmHandler = handler;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (running) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                // Send the obtained bytes to the UI activity.
                Message readMsg = mmHandler.obtainMessage(
                        MainActivity.MessageConstants.MESSAGE_READ, numBytes, -1,
                        mmBuffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {
            //Send message to client
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
            Message writtenMsg = mmHandler.obtainMessage(
                    MainActivity.MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg = mmHandler.obtainMessage(
                    MainActivity.MessageConstants.MESSAGE_TOAST, "Couldn't send data to the other device");
            mmHandler.sendMessage(writeErrorMsg);
        }
    }

    boolean isRunning() {
        return running;
    }

    // Call this method from the main activity to shut down the connection.
    void cancel() {
        running = false;
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }


}
