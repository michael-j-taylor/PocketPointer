package com.example.testmouseapp.threads;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.PPMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CommunicationThread extends Thread {
    private Object lock;
    private static final String TAG = "CommunicationThread";
    private final BluetoothSocket mmSocket;
    private Handler mmHandler;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private boolean running = true;
    private byte[] mmBuffer; // mmBuffer store for the stream

    CommunicationThread(BluetoothSocket socket, Handler handler, Object lock) {
        mmSocket = socket;
        mmHandler = handler;
        this.lock = lock;

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
        mmBuffer = new byte[PPMessage.MESSAGE_SIZE];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (running) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                if (numBytes < PPMessage.MESSAGE_SIZE) {
                    if (numBytes == -1) {
                        //Connection broke so close socket
                        this.cancel();
                        return;
                    }
                    //Log.d(TAG, "Only read " + numBytes + ", not " + PPMessage.MESSAGE_SIZE);
                }

                //Get message from buffer
                byte what = mmBuffer[0];
                //Got null message. Discard and continue
                if (what == PPMessage.Command.NULL) continue;

                String text = new String(mmBuffer, 1, PPMessage.MESSAGE_SIZE-1, StandardCharsets.UTF_8);

                // Send the obtained bytes to the UI activity.
                Message readMsg = mmHandler.obtainMessage(
                        MainActivity.MessageConstants.MESSAGE_READ, -1, what,
                        text);
                readMsg.sendToTarget();

                sleep(100);
            } catch (Exception e) {
                Log.e(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(PPMessage message) throws IllegalArgumentException{
        try {
            //Send message to client

            byte[] b = new byte[PPMessage.MESSAGE_SIZE];

            //Convert message to byte[]
            //Message type is first byte
            b[0] = message.what;
            //Rest of buffer is message text
            byte[] text = message.text.getBytes(StandardCharsets.UTF_8);
            if (text.length > PPMessage.MESSAGE_SIZE-1) {
                //Throw exception if text is too long
                throw new IllegalArgumentException();
            }
            System.arraycopy(text, 0, b, 1, text.length);

            mmOutStream.write(b);
            sleep(500);
            mmOutStream.flush();

            // Share the sent message with the UI activity.
            Message writtenMsg = mmHandler.obtainMessage(
                    MainActivity.MessageConstants.MESSAGE_WRITE, -1, message.what, message.text);
            writtenMsg.sendToTarget();
        } catch (Exception e) {
            if (e.equals(IOException.class)) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg = mmHandler.obtainMessage(
                        MainActivity.MessageConstants.MESSAGE_TOAST, "Couldn't send data to the other device");
                mmHandler.sendMessage(writeErrorMsg);
            } else if (e.equals(InterruptedException.class)) {
                //Do nothing
            }
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
