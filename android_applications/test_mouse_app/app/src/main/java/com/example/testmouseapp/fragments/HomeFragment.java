package com.example.testmouseapp.fragments;

import android.content.Context;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.Calibrater;
import com.example.testmouseapp.dataOperations.MovingAverage;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.google.android.material.navigation.NavigationView;

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

    private TextView live_acceleration;

    private boolean inFocus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        time = 1.f/polling_rate;
        calibrater = new Calibrater(100);
        movingAverage_X = new MovingAverage(50);
        movingAverage_Y = new MovingAverage(50);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //show action bar for this fragment
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();

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
        ScrollView scroll_wheel = view.findViewById(R.id.scroll_wheel);
        scroll_wheel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_MIDDLE));
            }
        });
        scroll_wheel.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        scroll_wheel.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            public void onScrollChange(View v, int newX, int newY, int oldX, int oldY) {
                mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.SCROLL, newX - oldX, newY - oldY));
            }
        });

        //Register calibrate button
        Button button_calibrate = view.findViewById(R.id.button_calibrate);
        button_calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                calibrater.calibrating = true;
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
            mm_main_activity.button_connect.setVisibility(View.INVISIBLE);
            mm_main_activity.button_disconnect.setVisibility(View.VISIBLE);
        } else {
            device_view.setText(R.string.not_connected);
            mm_main_activity.button_connect.setVisibility(View.VISIBLE);
            mm_main_activity.button_disconnect.setVisibility(View.INVISIBLE);
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
                        //Log.d(TAG, "3 or more ticks since acceleration");
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

    private void testMessages() {
        //Send messages to server here
        try {
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 1 from client"));
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 2 from client\n"));
        } catch (IllegalStateException ignored) { }
    }
}
