package com.example.testmouseapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.DevicesActivity;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.Calibrater;
import com.example.testmouseapp.dataOperations.MovingAverage;
import com.example.testmouseapp.dataOperations.PPMessage;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

public class HomeFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "Home Fragment";
    private MainActivity mm_main_activity;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private View view;

    //maximum and minimum acceleration values measured
    private float xmax = 0;
    private float xmin = 0;
    private float ymax = 0;
    private float ymin = 0;

    private MovingAverage movingAverage_X = new MovingAverage(50);
    private MovingAverage movingAverage_Y = new MovingAverage(50);

    //printed accelerometer values
    private float val_x, val_x_ave, val_x_pre, raw_x;
    private float val_y;
    private float val_y_ave;
    private float val_y_pre;
    private float magnitude;
    private int measurementCount = 0;
    private long startTime = 0;
    private long currentTime;

    private final int polling_rate = 60; //in Hz
    private float time;

    private Calibrater calibrater = new Calibrater(1000);

    private double x_pos = 0;
    private double y_pos = 0;
    private double x_vel = 0;
    private double y_vel = 0;
    double x_jerk = 0;
    double y_jerk = 0;

    //bluetooth vars
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final int REQUEST_ENABLE_BT = 3;
    private final int SHOW_DEVICES = 9;
    private final int REQUEST_COARSE_LOCATION = 12;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        time = 1.f/polling_rate;

        //HIDDEN FOR DEMO PURPOSES
        /*
        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activateCalibrate(v);
            }
        });
         */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        mm_main_activity = (MainActivity) getActivity();
        assert mm_main_activity != null;

        Log.d(TAG, "onCreate: Initializing accelerometer");

        //get sensor manager services
        sensorManager = (SensorManager) mm_main_activity.getSystemService(Context.SENSOR_SERVICE);

        //get sensor (accelerometer in this case)
        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //setup listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);  //can be changed to different delays //could use 1000000/polling_rate

        Log.d(TAG, "onCreate: Registered accelerometer listener");

        //Register testmessages button listener
        Button button_testmessages = view.findViewById(R.id.button_testmessages);
        button_testmessages.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testMessages();
            }
        });

        //Register bluetooth button listener
        Button button_connect = view.findViewById(R.id.button_connectDevice);
        button_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectDevice();
            }
        });

        //Register bluetooth button listener
        Button button_disconnect = view.findViewById(R.id.button_disconnectDevice);
        button_disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnectDevice();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        TextView device_view = view.findViewById(R.id.homeDeviceText);
        Button button_connect = view.findViewById(R.id.button_connectDevice);
        Button button_disconnect = view.findViewById(R.id.button_disconnectDevice);
        if (mm_main_activity.bt_service != null && mm_main_activity.bt_service.isConnected()) {
            String s = "Connected to " + mm_main_activity.bt_service.device.getName();
            device_view.setText(s);
            button_connect.setVisibility(View.INVISIBLE);
            button_disconnect.setVisibility(View.VISIBLE);
        } else {
            device_view.setText(R.string.not_connected);
            button_connect.setVisibility(View.VISIBLE);
            button_disconnect.setVisibility(View.INVISIBLE);
        }
    }

    //on sensor value change, display X and Z values
    @Override
    public void onSensorChanged(SensorEvent event) {

        //TODO: move the below objects out of this function - they don't need to be initialized every time the sensor updates
        // HIDDEN FOR DEMO PURPOSES
        TextView live_acceleration;
        //TextView max_acceleration;
        //TextView position;
        live_acceleration = view.findViewById(R.id.acceleration);
        //max_acceleration = findViewById(R.id.maximums);
        //position = findViewById(R.id.position);
        //TextView threshold_text = findViewById(R.id.threshold);


        currentTime = Calendar.getInstance().getTimeInMillis();

        raw_x = event.values[0];
        float raw_y = event.values[1];

        float raw_magnitude = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2));
        if (raw_magnitude > calibrater.magnitude_threshold) {
            //Log.d(TAG, "THRESHOLD EXCEEDED");
            movingAverage_X.addToWindow(raw_x);
            movingAverage_Y.addToWindow(raw_y);
        } else {
            raw_x = 0;
            raw_y = 0;
        }


        if (calibrater.calibrating) {
            //live_acceleration.setText("Calibrating");
            calibrater.calibrate(raw_x, raw_y);
            x_vel = 0;
            x_pos = 0;
            y_vel = 0;
            y_pos = 0;
            //calibrateAccelerometer(event);

        } else {  //calibrated, using live data

            //threshold_text.setText("Acceleration threshold: " + Float.toString(calibrater.magnitude_threshold));

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
                //Log.d(TAG, "raw magnitude: " + raw_magnitude + " vs adjusted " + magnitude + "vs thresh " + calibrater.magnitude_threshold);

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

                String data_live = "X: " + x_pos + "\nY: " + y_pos;
                String data_max = "X Maximum: " +
                        String.format("%.3f", xmax) + "\nX Minimum: " +
                        String.format("%.3f", xmin) + "\n\nY Maximum: " +
                        String.format("%.3f", ymax) + "\nY Minimum: " +
                        String.format("%.3f", ymin);

                live_acceleration.setText(data_live);
                //max_acceleration.setText(data_max);
                //position.setText("Position: " + String.format("%.3f",x_pos) + ", " + String.format("%.3f",y_pos));
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

                //live_acceleration.setText(data_live);
                //max_acceleration.setText(data_max);

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

    public void activateCalibrate() {
        calibrater.calibrating = true;
        //x_total = 0;
        //y_total = 0;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void connectDevice() {
        if (bluetoothAdapter == null) {
            String noBtMsg = "Your device does not support Bluetooth. Please connect using a USB cable.";

            Toast noBtToast = Toast.makeText(getContext(), noBtMsg, Toast.LENGTH_LONG);
            noBtToast.show();
        }
        else {
            if (ContextCompat.checkSelfPermission(mm_main_activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mm_main_activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
            }
            else {
                enableBluetooth();
            }
        }
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Intent showDevices = new Intent(getContext(), DevicesActivity.class);
            startActivityForResult(showDevices, SHOW_DEVICES);
        }
    }

    private void disconnectDevice() {
        mm_main_activity.bt_service.closeConnection();

        TextView device_view = view.findViewById(R.id.homeDeviceText);
        Button button_connect = view.findViewById(R.id.button_connectDevice);
        Button button_disconnect = view.findViewById(R.id.button_disconnectDevice);

        device_view.setText(R.string.not_connected);
        button_connect.setVisibility(View.VISIBLE);
        button_disconnect.setVisibility(View.INVISIBLE);
    }

    private void testMessages() {
        //Send messages to server here
        try {
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 1 from client"));
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 2 from client\n"));
        } catch (IllegalStateException ignored) { }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                //String btEnabledMsg = "Thank you for activating Bluetooth.";
                //Toast noBtToast = Toast.makeText(getApplicationContext(), btEnabledMsg, Toast.LENGTH_LONG);
                //noBtToast.show();
                Intent showDevices = new Intent(getContext(), DevicesActivity.class);
                startActivityForResult(showDevices, SHOW_DEVICES);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "You must enable Bluetooth for wireless connection.", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_COARSE_LOCATION) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else
                Toast.makeText(getContext(), "You must enable location permissions to discover devices", Toast.LENGTH_LONG).show();

        }
        if (requestCode == SHOW_DEVICES) {
            if (resultCode == Activity.RESULT_OK) {
                mm_main_activity.bt_service.device = data.getParcelableExtra("device");
                assert mm_main_activity.bt_service.device != null;
                try {
                    mm_main_activity.bt_service.openConnection(mm_main_activity.bt_service.device);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to connect to " + mm_main_activity.bt_service.device.getName(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to connect to " + mm_main_activity.bt_service.device.getName());
                    Intent showDevices = new Intent(getContext(), DevicesActivity.class);
                    startActivityForResult(showDevices, SHOW_DEVICES);
                }
            }
        }
    }

}
