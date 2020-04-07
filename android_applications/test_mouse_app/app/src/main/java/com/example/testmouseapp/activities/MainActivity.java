package com.example.testmouseapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.testmouseapp.R;

import com.example.testmouseapp.dataOperations.MovingAverage;

import com.example.testmouseapp.threads.CommunicationThread;
import com.example.testmouseapp.threads.ConnectThread;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testmouseapp.dataOperations.*;

import java.util.Calendar;

@TargetApi(Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    Sensor accelerometer;

    //maximum and minimum acceleration values measured
    float xmax = 0;
    float xmin = 0;
    float ymax = 0;
    float ymin = 0;

    MovingAverage movingAverage_X = new MovingAverage(50);
    MovingAverage movingAverage_Y = new MovingAverage(50);

    //printed accelerometer values
    float val_x, val_x_ave, val_x_pre, raw_x;
    float val_y, val_y_ave, val_y_pre, raw_y;
    float magnitude;
    int measurementCount = 0;
    long startTime = 0;
    long currentTime;

    //final float threshold = 0.2f;
    final int polling_rate = 60; //in Hz
    float time;
    //calibration vars

    Calibrater calibrater = new Calibrater(1000);

    //boolean calibrating = false;
    //int num_readings = 0;
    //int readings_max = 100000;  //change this to determine how many readings the accelerometer calibrates on
    //float x_total;
    //float y_total;
    //float x_pad = 0;
    //float y_pad = 0;
    double x_pos = 0;
    double y_pos = 0;
    double x_vel = 0;
    double y_vel = 0;
    double x_jerk = 0;
    double y_jerk = 0;

    //Used to interpret Bluetooth messages
    public interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;
    }

    //bluetooth vars
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private CommunicationThread mm_coms = null;
    private ConnectThread mm_connection = null;
    final int REQUEST_ENABLE_BT = 3;
    final int OPEN_BT_SETTINGS = 6;
    final int SHOW_DEVICES = 9;
    final int REQUEST_COARSE_LOCATION = 12;
    @SuppressLint("HandlerLeak")
    public Handler mm_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MessageConstants.MESSAGE_READ) {
                //Get message parts for log
                String type = PPMessage.toString((byte) msg.arg2);
                String text = (String) msg.obj;

                //Log message
                Toast.makeText(getApplicationContext(), "Got: " + type + text, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Got: " + type + text);


                PPMessage m = new PPMessage((byte) msg.arg2, text);
                //TODO Do something with the message here.

                //If message is notification to terminate, do so
                if (m.what == PPMessage.Command.END) {
                    Toast.makeText(getApplicationContext(), "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
                    //Shut down communicationsThread and connectThread
                    mm_coms = null;
                    if (mm_connection != null)
                        mm_connection.cancel();
                }

            } else if (msg.what == MessageConstants.MESSAGE_WRITE) {
                //Get message parts for log
                String type = PPMessage.toString((byte) msg.arg2);
                String text = (String) msg.obj;

                //Log message
                Toast.makeText(getApplicationContext(), "Sent: " + type + text, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Sent: " + type + text);

            } else if (msg.what == MessageConstants.MESSAGE_TOAST) {
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();

            } else {
                Log.e(TAG, "Received bad message code from handler: " + msg.what);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time = 1.f/polling_rate;

        Log.d(TAG, "onCreate: Initializing accelerometer");

        //get sensor manager services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //get sensor (accelerometer in this case)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //setup listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);  //can be changed to different delays //could use 1000000/polling_rate

        Log.d(TAG, "onCreate: Registered accelerometer listener");

        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activateCalibrate(v);
            }
        });

        //Set up Action Bar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
    }

    //on sensor value change, display X and Z values
    @Override
    public void onSensorChanged(SensorEvent event) {

        //TODO: move the below objects out of this function - they don't need to be initialized every time the sensor updates
        TextView live_acceleration;
        TextView max_acceleration;
        TextView position;
        live_acceleration = findViewById(R.id.acceleration);
        max_acceleration = findViewById(R.id.maximums);
        position = findViewById(R.id.position);
        TextView threshold_text = findViewById(R.id.threshold);


        currentTime = Calendar.getInstance().getTimeInMillis();

        raw_x = event.values[0];
        raw_y = event.values[1];

        float raw_magnitude = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2));
        if (raw_magnitude > calibrater.magnitude_threshold) {
            Log.d(TAG, "THRESHOLD EXCEEDED");
            movingAverage_X.addToWindow(raw_x);
            movingAverage_Y.addToWindow(raw_y);
        } else {
            raw_x = 0;
            raw_y = 0;
        }


        if (calibrater.calibrating) {
            live_acceleration.setText("Calibrating");
            calibrater.calibrate(raw_x, raw_y);
            x_vel = 0;
            x_pos = 0;
            y_vel = 0;
            y_pos = 0;
            //calibrateAccelerometer(event);

        } else {  //calibrated, using live data

            threshold_text.setText("Acceleration threshold: " + Float.toString(calibrater.magnitude_threshold));
            //val_x = event.values[0] + x_pad;
            //val_y = event.values[1] + y_pad;

            //intermittently calculate position
            if (currentTime - startTime > time*1000) {

                //set maximum x & y acceleration readings
                if (event.values[0] > xmax) {xmax = event.values[0];}
                if (event.values[0] < xmin) {xmin = event.values[0];}

                if (event.values[1] > ymax) { ymax = event.values[1];}
                if (event.values[1] < ymin) { ymin = event.values[1];}

                //calculate current value via moving average
                val_x = movingAverage_X.calculateAverage();
                val_y = movingAverage_Y.calculateAverage();
                //Log.d(TAG, event.values[0] + " " + event.values[1]);
                //Log.d(TAG, val_x + " " + val_y);



                magnitude = (float) Math.sqrt(Math.pow(val_x, 2) + Math.pow(val_y, 2));
                Log.d(TAG, "raw magnitude: " + raw_magnitude + " vs adjusted " + magnitude + "vs thresh " + calibrater.magnitude_threshold);

                //if (magnitude < calibrater.magnitude_threshold) {
                //    val_x_ave = 0;
                //    val_y_ave = 0;
                //}

                //calculate velocity
                x_vel = x_vel + val_x * time;
                y_vel = y_vel + val_y * time;

                //calculate position
                x_pos = x_pos + x_vel * time + .5 * val_x * time * time;
                y_pos = y_pos + y_vel * time + .5 * val_y * time * time;

                String data_live = "X: " + val_x + "\nY: " + val_y;
                String data_max = "X Maximum: " +
                        String.format("%.3f", xmax) + "\nX Minimum: " +
                        String.format("%.3f", xmin) + "\n\nY Maximum: " +
                        String.format("%.3f", ymax) + "\nY Minimum: " +
                        String.format("%.3f", ymin);

                live_acceleration.setText(data_live);
                max_acceleration.setText(data_max);
                position.setText("Position: " + String.format("%.3f",x_pos) + ", " + String.format("%.3f",y_pos));
                startTime = Calendar.getInstance().getTimeInMillis();
                measurementCount = 0;
            }
            else {
                //String data_live = "X: " + 0 + "\nY: " + 0;
                String data_max = "X Maximum: " +
                        String.format("%.3f", xmax) + "\nX Minimum: " +
                        String.format("%.3f", xmin) + "\n\nY Maximum: " +
                        String.format("%.3f", ymax) + "\nY Minimum: " +
                        String.format("%.3f", ymin);

                String data_live = "X: " + val_x + "\nY: " + val_y;

                live_acceleration.setText(data_live);
                max_acceleration.setText(data_max);

                /*
                magnitude = (float) Math.sqrt(Math.pow(val_x, 2) + Math.pow(val_y, 2));

                if (magnitude > calibrater.magnitude_threshold) {
                    //val_x_ave += val_x;
                    movingAverage_X.addToWindow(val_x);
                    //val_y_ave += val_y;
                    movingAverage_Y.addToWindow(val_y);
                    //x_jerk = (val_x - val_x_pre)*time;
                    //y_jerk = (val_y - val_y_pre)*time;
                }*/
                measurementCount++;
            }
        }
       /* try
        {
            Thread.sleep(0,1000000/polling_rate);
        }
        catch (Exception e)
        {
            System.out.print(e);
        }*/
    }

    /*
    public void calibrateAccelerometer(SensorEvent event) {
        num_readings += 1;
        xmax = 0;
        ymax = 0;
        xmin = 0;
        ymin = 0;
        x_vel = 0;
        y_vel = 0;
        x_pos = 0;
        y_pos = 0;

        if (num_readings > readings_max) {
            x_total += event.values[0];
            y_total += event.values[1];
        }

        else {
            x_pad = x_total / readings_max;
            y_pad = y_total / readings_max;

            calibrating = false;
            num_readings = 0;
            Log.d(TAG, "accelerometer calibrated");
        }
    }
    */


    public void activateCalibrate(View view) {
        calibrater.calibrating = true;
        //x_total = 0;
        //y_total = 0;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void connectDevice(View view) {
        if (bluetoothAdapter == null) {
            String noBtMsg = "Your device does not support Bluetooth. Please connect using a USB cable.";

            Toast noBtToast = Toast.makeText(getApplicationContext(), noBtMsg, Toast.LENGTH_LONG);
            noBtToast.show();
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
            }
            else {
                enableBluetooth();
            }
        }
    }

    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Intent showDevices = new Intent(this, DevicesActivity.class);
            startActivityForResult(showDevices, SHOW_DEVICES);
        }
    }

    public void execute() {
        //TODO Write to coms here. See example in testMessages(View)

    }

    public void testMessages(View view) {
        //Send messages to server here
        mm_coms.write(new PPMessage(PPMessage.Command.STRING, "Test message 1 from client"));
        mm_coms.write(new PPMessage(PPMessage.Command.STRING, "Test message 2 from client\n"));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //String btEnabledMsg = "Thank you for activating Bluetooth.";
                //Toast noBtToast = Toast.makeText(getApplicationContext(), btEnabledMsg, Toast.LENGTH_LONG);
                //noBtToast.show();
                Intent showDevices = new Intent(this, DevicesActivity.class);
                startActivityForResult(showDevices, SHOW_DEVICES);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "You must enable Bluetooth for wireless connection.", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_COARSE_LOCATION) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            }
            else Toast.makeText(this, "You must enable location permissions to discover devices", Toast.LENGTH_LONG).show();

        }
        if (requestCode == OPEN_BT_SETTINGS) {
            if (!bluetoothAdapter.isEnabled()) {
                String btDisabledMsg = "You must enable Bluetooth for wireless connection.";

                Toast noBtToast = Toast.makeText(getApplicationContext(), btDisabledMsg, Toast.LENGTH_LONG);
                noBtToast.show();
            }
        }
        if (requestCode == SHOW_DEVICES) {
            if (resultCode == RESULT_OK) {
                BluetoothDevice d = data.getParcelableExtra("device");

                mm_connection = new ConnectThread(d, mm_handler);
                mm_connection.start();

                //Ensure comm channel has been established before moving on
                while (mm_connection.isRunning() && !mm_connection.isConnected());

                //If the connection was successful, move on
                if (mm_connection.isConnected()) {
                    mm_coms = mm_connection.getCommunicationThread();
                    execute();
                } else {
                    //Otherwise, return to devices activity and throw error toast
                    mm_connection.cancel();
                    Toast.makeText(this, "Failed to connect to " + d.getName(), Toast.LENGTH_SHORT).show();
                    Intent showDevices = new Intent(this, DevicesActivity.class);
                    startActivityForResult(showDevices, SHOW_DEVICES);
                }
            }
        }
    }

    public void onDestroy() {
        Toast.makeText(this, "Shutting down", Toast.LENGTH_SHORT).show();
        if (mm_coms != null) {
            mm_coms.write(new PPMessage(PPMessage.Command.END, "Client ending activity"));
        }
        //Shut down communicationsThread and connectThread
        mm_coms = null;
        if (mm_connection != null)
            mm_connection.cancel();
        super.onDestroy();
    }
}

