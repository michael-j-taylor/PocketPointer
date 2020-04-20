package com.example.testmouseapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
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
import com.example.testmouseapp.dataOperations.Calibrater;
import com.example.testmouseapp.dataOperations.MovingAverage;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.services.BluetoothService;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

public class HomeFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private View view;

    //maximum and minimum acceleration values measured
    private float xmax = 0;
    private float xmin = 0;
    private float ymax = 0;
    private float ymin = 0;

    private MovingAverage movingAverage_X = new MovingAverage(100);
    private MovingAverage movingAverage_Y = new MovingAverage(100);

    //printed accelerometer values
    private float accel_x, prev_accel_x, raw_x;
    private float accel_y, prev_accel_y, raw_y;
    private long startTime = 0;
    private long currentTime;

    private final int polling_rate = 60; //in Hz
    private float time;

    private Calibrater calibrater;

    private double x_pos = 0;
    private double y_pos = 0;
    private double x_vel = 0;
    private double y_vel = 0;
    double x_jerk = 0;
    double y_jerk = 0;

    TextView live_acceleration, max_acceleration, position, threshold_text;

    //bluetooth vars
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final int REQUEST_ENABLE_BT = 3;
    private final int SHOW_DEVICES = 9;
    private final int REQUEST_COARSE_LOCATION = 12;
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
//        setContentView(R.layout.activity_main);
        time = 1.f/polling_rate;
        calibrater = new Calibrater(100);
        /*Button calibrate = view.findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                calibrater.calibrating = true;
            }
        });*/

        // Bind to BluetoothService
        Intent intent = new Intent(getContext(), BluetoothService.class);
        Objects.requireNonNull(getActivity()).bindService(intent, mm_connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "onCreate: Initializing accelerometer");

        //get sensor manager services
        sensorManager = (SensorManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SENSOR_SERVICE);

        //get sensor (accelerometer in this case)
        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //setup listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);  //can be changed to different delays //could use 1000000/polling_rate

        Log.d(TAG, "onCreate: Registered accelerometer listener");

        //Register testmessages button listener
        Button button_testmessages = view.findViewById(R.id.testmessages);
        button_testmessages.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testMessages();
            }
        });

        //Register bluetooth button listener
        Button button_bluetooth = view.findViewById(R.id.bluetooth);
        button_bluetooth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectDevice();
            }
        });
        Button button_calibrate = view.findViewById(R.id.calibrate);
        button_calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                calibrater.calibrating = true;
            }
        });
        return view;
    }

    //on sensor value change, display X and Z values
    @Override
    public void onSensorChanged(SensorEvent event) {
        live_acceleration = view.findViewById(R.id.acceleration);
        currentTime = Calendar.getInstance().getTimeInMillis();

        raw_x = event.values[0];
        raw_y = event.values[1];

        //float raw_magnitude = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2));
        float calibrated_magnitude = (float) Math.sqrt(Math.pow(raw_x - calibrater.x_offset, 2) +
                Math.pow(raw_y - calibrater.y_offset, 2));
        if (calibrated_magnitude > calibrater.magnitude_threshold) {
            //Log.d(TAG, "THRESHOLD EXCEEDED");
            movingAverage_X.addToWindow(raw_x);
            movingAverage_Y.addToWindow(raw_y);
        }
        else {
            movingAverage_X.addToWindow(0.0);
            movingAverage_Y.addToWindow(0.0);
        }
        if (calibrater.calibrating) {
            live_acceleration.setText("Calibrating");
            calibrater.calibrate(raw_x, raw_y);
            if (!calibrater.calibrating) {
                x_vel = 0;
                x_pos = 0;
                y_vel = 0;
                y_pos = 0;
                prev_accel_x = 0;
                prev_accel_y = 0;
                movingAverage_X.clearWindow();
                movingAverage_Y.clearWindow();
            }
        }
        else {  //calibrated, using live data
            //threshold_text.setText("Acceleration threshold: " + Float.toString(calibrater.magnitude_threshold));
            //intermittently calculate position
            if (currentTime - startTime > time*1000) {

                //set maximum x & y acceleration readings
                if (event.values[0] > xmax) {xmax = event.values[0];}
                if (event.values[0] < xmin) {xmin = event.values[0];}

                if (event.values[1] > ymax) { ymax = event.values[1];}
                if (event.values[1] < ymin) { ymin = event.values[1];}

                //calculate current value via moving average
                accel_x = movingAverage_X.calculateAverage() - calibrater.x_offset;
                accel_y = movingAverage_Y.calculateAverage() - calibrater.y_offset;
                if (calibrated_magnitude < calibrater.magnitude_threshold)
                {
                    accel_x = 0;
                    accel_y = 0;
                }
                //Log.d(TAG, event.values[0] + " " + event.values[1]);//Log.d(TAG, val_x + " " + val_y);

                //Log.d(TAG, "raw magnitude: " + raw_magnitude + " vs adjusted " + magnitude + "vs thresh " + calibrater.magnitude_threshold);

                //calculate jerk
                float jerk_x = (accel_x - prev_accel_x)/time;
                float jerk_y = (accel_y - prev_accel_y)/time;
                //calculate velocity
                x_vel = x_vel + accel_x * time + .5 * jerk_x * Math.pow(time, 2);
                y_vel = y_vel + accel_y * time + .5 * jerk_y * Math.pow(time, 2);
                //calculate position. Will jerk help? We'll find out. Delta x and y and send to Windows if that is what is needed.
                double delta_x = x_vel * time + .5 * accel_x * Math.pow(time, 2) + 1/6 * jerk_x * Math.pow(time, 3);
                double delta_y = y_vel * time + .5 * accel_y * Math.pow(time, 2) + 1/6 * jerk_y * Math.pow(time, 3);
                x_pos += delta_x;
                y_pos += delta_y;
                prev_accel_x = accel_x;
                prev_accel_y = accel_y;

                String data_live = "X: " + String.format("%.3f", x_pos) + "\nY: " + String.format("%.3f", y_pos) + "\nax: " + accel_x +"\nay: " + accel_y +
                        "\ncx: " + calibrater.x_offset + "\ncy: " + calibrater.y_offset + "\nrx: " + String.format("%.5f", raw_x) +
                        "\nry: " + String.format("%.5f", raw_y);
                String data_max = "X Maximum: " +
                        String.format("%.3f", xmax) + "\nX Minimum: " +
                        String.format("%.3f", xmin) + "\n\nY Maximum: " +
                        String.format("%.3f", ymax) + "\nY Minimum: " +
                        String.format("%.3f", ymin);

                live_acceleration.setText(data_live);
                //max_acceleration.setText(data_max);
                //position.setText("Position: " + String.format("%.3f",x_pos) + ", " + String.format("%.3f",y_pos));
                startTime = Calendar.getInstance().getTimeInMillis();
            }
            else {
                //String data_live = "X: " + 0 + "\nY: " + 0;
                String data_max = "X Maximum: " +
                        String.format("%.3f", xmax) + "\nX Minimum: " +
                        String.format("%.3f", xmin) + "\n\nY Maximum: " +
                        String.format("%.3f", ymax) + "\nY Minimum: " +
                        String.format("%.3f", ymin);

                String data_live = "X: " + accel_x + "\nY: " + accel_y;
                //live_acceleration.setText(data_live);
            }
        }
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
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
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

    private void testMessages() {
        //Send messages to server here
        try {
            mm_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 1 from client"));
            mm_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 2 from client\n"));
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
                BluetoothDevice d = data.getParcelableExtra("device");
                assert d != null;
                try {
                    mm_service.openConnection(d);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to connect to " + d.getName(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to connect to " + d.getName());
                    Intent showDevices = new Intent(getContext(), DevicesActivity.class);
                    startActivityForResult(showDevices, SHOW_DEVICES);
                }
            }
        }
    }

    public void onDestroy() {
        if (mm_bound) {
            Objects.requireNonNull(getActivity()).unbindService(mm_connection);
            mm_bound = false;
        }
        super.onDestroy();
    }
}
