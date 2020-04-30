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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.DevicesActivity;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.Calibrater;
import com.example.testmouseapp.dataOperations.MovingAverage;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.services.BluetoothService;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

public class HomeFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "Home Fragment";
    private MainActivity mm_main_activity;
    private NavigationView navigationView;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private View view;

    //maximum and minimum acceleration values measured
    private float xmax = 0;
    private float xmin = 0;
    private float ymax = 0;
    private float ymin = 0;

    private MovingAverage movingAverage_X;
    private MovingAverage movingAverage_Y;

    //printed accelerometer values
    private float accel_x, prev_accel_x, raw_x;
    private float accel_y, prev_accel_y, raw_y;
    private long startTime = 0;
    private long currentTime;

    private final int polling_rate = 60; //in Hz
    private int twa = 0; //ticks without acceleration
    private float friction_coefficient = .82f;
    private float time;

    private Calibrater calibrater;

    private double x_pos = 0;
    private double y_pos = 0;
    private double x_vel = 0;
    private double y_vel = 0;

    TextView live_acceleration;

    private boolean inFocus;

    //bluetooth vars
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final int REQUEST_ENABLE_BT = 3;
    private final int SHOW_DEVICES = 9;
    private final int REQUEST_FINE_LOCATION = 6;
    private final int REQUEST_COARSE_LOCATION = 12;


    private MenuItem menuItem_button_connect;
    private MenuItem menuItem_button_disconnect;
    private Button button_connect;
    private Button button_disconnect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        time = 1.f/polling_rate;
        calibrater = new Calibrater(50);
        movingAverage_X = new MovingAverage(50);
        movingAverage_Y = new MovingAverage(50);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //show action bar for this fragment
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

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
        //mm_main_activity.bt_service.writeMessage(new PPMessage.Command.KEY_PRESS, )
        Log.d(TAG, "onCreate: Registered accelerometer listener");

        /*----------VOLATILE NAVIGATION DRAWER BUTTON CREATION----------*/
        //using the public NavigationView in our MainActivity, we can access navigation drawer elements
        //and interact with them. This allows the setup of quick settings for each mode of the application
        NavigationView navigationView = mm_main_activity.navigationView;

        //get all quick setting menu items
        MenuItem menuItem_mouse_lock = navigationView.getMenu().findItem(R.id.nav_switch_mousemode);
        MenuItem menuItem_switch_overrideVolumeKeys = navigationView.getMenu().findItem(R.id.nav_switch_override_volume_keys);

        //hide any items not relevant to this fragment
        menuItem_mouse_lock.setVisible(false);
        menuItem_switch_overrideVolumeKeys.setVisible(false);

        // show all items relevant to this fragment

        /*----------STANDARD BUTTON CREATION----------*/
        //TODO: move appropriate buttons to navdrawer

        //Register testmessages button listener
        Button button_testmessages = view.findViewById(R.id.button_testmessages);
        button_testmessages.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testMessages();
            }
        });

        menuItem_button_connect = navigationView.getMenu().findItem(R.id.nav_button_connect_device);
        menuItem_button_disconnect = navigationView.getMenu().findItem(R.id.nav_button_disconnect_device);

        button_connect = menuItem_button_connect.getActionView().findViewById(R.id.menu_button_connect_device);
        button_disconnect = menuItem_button_disconnect.getActionView().findViewById(R.id.menu_button_disconnect_device);

        //Register bluetooth button listener
        button_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectDevice();
            }
        });

        //Register mouse buttons
        Button lmb = view.findViewById(R.id.button_left_mouse);
        lmb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_LEFT));
            }
        });
        Button rmb = view.findViewById(R.id.button_right_mouse);
        rmb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_RIGHT));
            }
        });

        //Register scroll wheel and button
        ScrollView mmb = view.findViewById(R.id.scroll_wheel);
        rmb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_MIDDLE));
            }
        });

        //Register calibrate button
        Button button_calibrate = view.findViewById(R.id.button_calibrate);
        button_calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                calibrater.calibrating = true;
            }
        });

        //Register bluetooth button listener
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
        inFocus = true;
        TextView device_view = view.findViewById(R.id.homeDeviceText);

        if (mm_main_activity.bt_service != null && mm_main_activity.bt_service.isConnected()) {
            String s = "Connected to " + mm_main_activity.bt_service.device.getName();
            device_view.setText(s);
            menuItem_button_connect.setVisible(false);
            menuItem_button_disconnect.setVisible(true);
        } else {
            device_view.setText(R.string.not_connected);
            menuItem_button_connect.setVisible(true);
            menuItem_button_disconnect.setVisible(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        inFocus = true;
        calibrater.calibrating = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        inFocus = false;
        calibrater.calibrating = false;
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
            if (!calibrater.calibrating)
                resetPointer();
        }
        else {  //calibrated using live data
            //intermittently calculate position
            if (currentTime - startTime > time*1000 && inFocus) {

                //set maximum x & y acceleration readings
                if (event.values[0] > xmax) {xmax = event.values[0];}
                if (event.values[0] < xmin) {xmin = event.values[0];}

                if (event.values[1] > ymax) { ymax = event.values[1];}
                if (event.values[1] < ymin) { ymin = event.values[1];}

                //calculate current value via moving average
                accel_x = movingAverage_X.calculateAverage() - calibrater.x_offset;
                accel_y = movingAverage_Y.calculateAverage() - calibrater.y_offset;
                float average_calibrated_magnitude = (float) Math.sqrt(Math.pow(accel_x, 2) + Math.pow(accel_y, 2));
                if (average_calibrated_magnitude < calibrater.magnitude_threshold)
                {
                    accel_x = 0;
                    accel_y = 0;
                    twa++;
                    if (twa >= 3) {
                        x_vel *= friction_coefficient;
                        y_vel *= friction_coefficient;
                        float vel_mag = (float) Math.sqrt(x_vel * x_vel + y_vel * y_vel);
                        if (vel_mag < .0001f) {
                            x_vel = 0;
                            y_vel = 0;
                        }
                    }
                }
                else
                    twa = 0;
                //Log.d(TAG, event.values[0] + " " + event.values[1]);//Log.d(TAG, val_x + " " + val_y);
                //Log.d(TAG, "raw magnitude: " + raw_magnitude + " vs adjusted " + magnitude + "vs thresh " + calibrater.magnitude_threshold);

                //calculate jerk
                float jerk_x = (accel_x - prev_accel_x)/time;
                float jerk_y = (accel_y - prev_accel_y)/time;
                //calculate velocity
                x_vel = x_vel + accel_x * time + .5 * jerk_x * Math.pow(time, 2);
                y_vel = y_vel + accel_y * time + .5 * jerk_y * Math.pow(time, 2);
                //calculate position. Will jerk help? We'll find out. Delta x and y to send to Windows if that is what is needed.
                double delta_x = x_vel * time + .5 * accel_x * Math.pow(time, 2) + 1/6 * jerk_x * Math.pow(time, 3);
                double delta_y = y_vel * time + .5 * accel_y * Math.pow(time, 2) + 1/6 * jerk_y * Math.pow(time, 3);
                if (mm_main_activity.bt_service != null && mm_main_activity.bt_service.isConnected() && inFocus) {
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.MOUSE_COORDS, delta_x, delta_y));
                }
                x_pos += delta_x;
                y_pos += delta_y;
                prev_accel_x = accel_x;
                prev_accel_y = accel_y;

                String data_live = "X: " + String.format("%.3f", x_pos) + "\nY: " + String.format("%.3f", y_pos) + "\nax: " + accel_x +"\nay: " + accel_y +
                        "\ncx: " + calibrater.x_offset + "\ncy: " + calibrater.y_offset + "\nrx: " + String.format("%.5f", raw_x) +
                        "\nry: " + String.format("%.5f", raw_y) + "\nvx: " + String.format("%.5f", x_vel) +
                        "\nvy: " + String.format("%.5f", y_vel);

                live_acceleration.setText(data_live);
                startTime = Calendar.getInstance().getTimeInMillis();
            }
        //there was an else statement here, but it was basically empty
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void resetPointer() {
        x_vel = 0;
        x_pos = 0;
        y_vel = 0;
        y_pos = 0;
        prev_accel_x = 0;
        prev_accel_y = 0;
        movingAverage_X.clearWindow();
        movingAverage_Y.clearWindow();
    }

    private void connectDevice() {
        if (bluetoothAdapter == null) {
            String noBtMsg = "Your device does not support Bluetooth. Please connect using a USB cable.";

            Toast noBtToast = Toast.makeText(getContext(), noBtMsg, Toast.LENGTH_LONG);
            noBtToast.show();
        }
        else {
            Log.d(TAG, "Build version is " + Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                if (ContextCompat.checkSelfPermission(mm_main_activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mm_main_activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                } else {
                    enableBluetooth();
                }
            } else {
                if (ContextCompat.checkSelfPermission(mm_main_activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mm_main_activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
                } else {
                    enableBluetooth();
                }
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

        device_view.setText(R.string.not_connected);
        menuItem_button_connect.setVisible(true);
        menuItem_button_disconnect.setVisible(false);
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
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else
                Toast.makeText(getContext(), "You must enable location permissions to discover devices", Toast.LENGTH_LONG).show();

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
