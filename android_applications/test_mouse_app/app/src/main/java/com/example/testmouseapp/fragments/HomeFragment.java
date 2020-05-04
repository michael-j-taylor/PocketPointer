package com.example.testmouseapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.example.testmouseapp.dataOperations.VectorOperations;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.Objects;
import java.util.Vector;

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
    private float raw_z;
    private long startTime = 0;
    private long currentTime;

    private final int polling_rate = 60; //in Hz
    private int twa = 0; //ticks without acceleration
    private float friction_coefficient = .82f;
    private float time;
    private final int vibrationTime = 40;

    private Calibrater calibrater;

    private double x_pos = 0;
    private double y_pos = 0;
    private double x_vel = 0;
    private double y_vel = 0;

    private TextView live_acceleration;

    private boolean inFocus;
    private boolean isCalibrating = false;

    MenuItem menuItem_item_calibrate;
    MenuItem menuItem_progressbar_calibrating;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        time = 1.f/polling_rate;
        calibrater = new Calibrater(150);
        movingAverage_X = new MovingAverage(40);
        movingAverage_Y = new MovingAverage(40);
    }

    @SuppressLint("ClickableViewAccessibility")
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
        navigationView = mm_main_activity.navigationView;

        //get all quick setting menu items
        MenuItem menuItem_mouse_lock = navigationView.getMenu().findItem(R.id.nav_switch_mousemode);
        MenuItem menuItem_switch_overrideVolumeKeys = navigationView.getMenu().findItem(R.id.nav_switch_override_volume_keys);
        menuItem_item_calibrate = navigationView.getMenu().findItem(R.id.nav_item_calibrate);
        menuItem_progressbar_calibrating = navigationView.getMenu().findItem(R.id.nav_progressbar_calibrate);

        //hide any items not relevant to this fragment
        menuItem_mouse_lock.setVisible(false);
        menuItem_switch_overrideVolumeKeys.setVisible(false);
        menuItem_progressbar_calibrating.setVisible(false);

        // show all items relevant to this fragment
        menuItem_item_calibrate.setVisible(true);


        //set listeners
        Button button_calibrate = menuItem_item_calibrate.getActionView().findViewById(R.id.menu_button_calibrate);
        button_calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                menuItem_item_calibrate.setVisible(false);
                menuItem_progressbar_calibrating.setVisible(true);
                calibrater.calibrating = true;
                isCalibrating = true;
            }
        });
        /*----------STANDARD BUTTON CREATION----------*/
        //TODO: move appropriate buttons to navdrawer

        final Vibrator vibe = (Vibrator) mm_main_activity.getSystemService(Context.VIBRATOR_SERVICE);
        //Register mouse buttons
        Button lmb = view.findViewById(R.id.button_left_mouse);
        lmb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(vibrationTime);
                if (canSendMessage())
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_LEFT));
                Log.d(TAG, "Left mouse button clicked");
            }
        });

        Button rmb = view.findViewById(R.id.button_right_mouse);
        rmb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(vibrationTime);
                if (canSendMessage())
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_RIGHT));
                Log.d(TAG, "Right mouse button clicked");
            }
        });

        //Register scroll wheel and button
        ScrollView scroll_wheel = view.findViewById(R.id.scroll_wheel);
        scroll_wheel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                vibe.vibrate(vibrationTime);
                if (canSendMessage())
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_MIDDLE));
            }
        });
        scroll_wheel.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent me) {
                Log.d(TAG, "Middle mouse button clicked");
                vibe.vibrate(vibrationTime);
                float oldY;
                float newY = me.getY();
                if (me.getHistorySize() > 0)
                    oldY = me.getHistoricalY(0);
                else
                    oldY = newY;

                if (canSendMessage()) {
                    if (Math.abs(newY - oldY) <= 5) {
                        mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.BUTTON, PPMessage.Button.MOUSE_MIDDLE));
                        Log.d(TAG, "Middle mouse button clicked");
                        return true;
                    }
                    else {
                        mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.SCROLL, 0, (newY - oldY)/10));
                        Log.d(TAG, "Middle mouse button scrolled: " + me.getY());
                        return true;
                    }
                }
                return false;
            }
            public void performClick() {}
        });
        scroll_wheel.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        scroll_wheel.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int newX, int newY, int oldX, int oldY) {
                if (canSendMessage())
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.SCROLL, newX - oldX, newY - oldY));
                Log.d(TAG, "Scroll wheel invoked");
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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
        raw_z = event.values[2];

        //float raw_magnitude = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2));
        float calibrated_magnitude = (float) Math.sqrt(Math.pow(raw_x - calibrater.x_offset, 2) +
                Math.pow(raw_y - calibrater.y_offset, 2));
        if (calibrated_magnitude > calibrater.magnitude_threshold && !calibrater.calibrating && raw_z < 10 && raw_z > 9.5) {
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
            if (isCalibrating) {
                isCalibrating = false;
                menuItem_progressbar_calibrating.setVisible(false);
                menuItem_item_calibrate.setVisible(true);
            }

            if (currentTime - startTime > time*1000 && inFocus) {
                startTime = Calendar.getInstance().getTimeInMillis();
                //set maximum x & y acceleration readings
                /*if (event.values[0] > xmax) {xmax = event.values[0];} //Might repurpose this later
                if (event.values[0] < xmin) {xmin = event.values[0];}

                if (event.values[1] > ymax) { ymax = event.values[1];}
                if (event.values[1] < ymin) { ymin = event.values[1];}*/

                //calculate current value via moving average
                accel_x = movingAverage_X.calculateAverage() - calibrater.x_offset;
                accel_y = movingAverage_Y.calculateAverage() - calibrater.y_offset;
                float average_calibrated_magnitude = (float) Math.sqrt(Math.pow(accel_x, 2) + Math.pow(accel_y, 2));
                if (average_calibrated_magnitude < calibrater.magnitude_threshold)
                {
                    accel_x = 0;
                    accel_y = 0;
                    twa++;
                    if (twa >= 6) {
                        //Log.d(TAG, "3 or more ticks since acceleration");
                        x_vel *= friction_coefficient;
                        y_vel *= friction_coefficient;
                        float vel_mag = (float) Math.sqrt(x_vel * x_vel + y_vel * y_vel);
                        if (vel_mag < .00005f) {
                            x_vel = 0;
                            y_vel = 0;
                        }
                    }
                }
                else
                    twa = 0;

                if (VectorOperations.oppositeDirection(accel_x, accel_y, x_vel, y_vel))
                {
                    accel_x *= 5;
                    accel_y *= 5;
                }

                //calculate jerk
                float jerk_x = (accel_x - prev_accel_x)/time;
                float jerk_y = (accel_y - prev_accel_y)/time;
                //calculate velocity
                x_vel = x_vel + accel_x * time + .5 * jerk_x * Math.pow(time, 2);
                y_vel = y_vel + accel_y * time + .5 * jerk_y * Math.pow(time, 2);
                //calculate position. Will jerk help? We'll find out. Delta x and y to send to Windows if that is what is needed.
                double delta_x = x_vel * time + .5 * accel_x * Math.pow(time, 2) + (double)1/6 * jerk_x * Math.pow(time, 3);
                double delta_y = y_vel * time + .5 * accel_y * Math.pow(time, 2) + (double)1/6 * jerk_y * Math.pow(time, 3);
                x_pos += delta_x*5000;
                y_pos += -(delta_y*5000);
                if (x_pos < 0.0)
                    x_pos = 0;
                if (y_pos < 0.0)
                    y_pos = 0;
                delta_x*=5000;
                delta_y*=-5000;
                if (canSendMessage()) {
                    mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.MOUSE_COORDS, delta_x, delta_y));
                }

                prev_accel_x = accel_x;
                prev_accel_y = accel_y;

                /*String data_live = "X: " + String.format("%.3f", x_pos) + "\nY: " + String.format("%.3f", y_pos) + "\nax: " + accel_x +"\nay: " + accel_y +
                        "\ncx: " + calibrater.x_offset + "\ncy: " + calibrater.y_offset + "\nrx: " + String.format("%.5f", raw_x) +
                        "\nry: " + String.format("%.5f", raw_y) + "\nvx: " + String.format("%.5f", x_vel) +
                        "\nvy: " + String.format("%.5f", y_vel);

                live_acceleration.setText(data_live);*/
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

    private boolean canSendMessage() {
        return (mm_main_activity.bt_service != null && mm_main_activity.bt_service.isConnected() && inFocus);
    }
}
