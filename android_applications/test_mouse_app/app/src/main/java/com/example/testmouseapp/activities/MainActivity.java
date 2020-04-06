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
import com.example.testmouseapp.dataOperations.Filter;
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

    //accelerometer bounds
    float x_pos_bound;
    float x_neg_bound;
    float y_pos_bound;
    float y_neg_bound;

    MovingAverage movingAverage_X = new MovingAverage(100);
    MovingAverage movingAverage_Y = new MovingAverage(100);

    //printed accelerometer values
    float val_x, val_x_ave, val_x_pre;
    float val_y, val_y_ave, val_y_pre;
    float magnitude;
    int measurementCount = 0;
    long startTime = 0;
    long currentTime;

    final float threshold = 0.2f;
    final int polling_rate = 60; //in Hz
    float time;
    //calibration vars
    boolean calibrating = false;
    int num_readings = 0;
    int readings_max = 100000;  //change this to determine how many readings the accelerometer calibrates on
    float x_total;
    float y_total;
    float x_pad = 0;
    float y_pad = 0;
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
                Log.d(TAG, "Got: " + msg.obj);
                if (!msg.obj.toString().contains("CON: ")) {
                    mm_coms.write(("CON: " + msg.obj).getBytes());
                }
            } else if (msg.what == MessageConstants.MESSAGE_WRITE) {
                Log.d(TAG, "Sent: \"" + msg.obj + "\"");
            } else if (msg.what == MessageConstants.MESSAGE_TOAST) {
                Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
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

        TextView threshold_text = findViewById(R.id.threshold);
        threshold_text.setText("Acceleration threshold: " + Float.toString(threshold));

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

        currentTime = Calendar.getInstance().getTimeInMillis();
        if (calibrating) {
            live_acceleration.setText("Calibrating");
            calibrateAccelerometer(event);
        }
        else {
            val_x = event.values[0] + x_pad;
            val_y = event.values[1] + y_pad;
            if (currentTime - startTime > time*1000) {

                //ignore values if they're inside a set range
                if (event.values[0] > xmax) {xmax = event.values[0];}
                if (event.values[0] < xmin) {xmin = event.values[0];}

                if (event.values[1] > ymax) { ymax = event.values[1];}
                if (event.values[1] < ymin) { ymin = event.values[1];}

                //float averageAccel[] = MovementCalculation.averageAccel(event, polling_rate);

                //val_x = averageAccel[0];
                //val_y = averageAccel[1];

                //val_x = val_x_ave / measurementCount;
                val_x = movingAverage_X.calculateAverage();
                //val_y = val_y_ave / measurementCount;
                val_y = movingAverage_Y.calculateAverage();

                val_x_ave = event.values[0] + x_pad;
                val_y_ave = event.values[1] + y_pad;
                magnitude = (float) Math.sqrt(Math.pow(val_x, 2) + Math.pow(val_y, 2));

                if (magnitude < threshold) {
                    val_x_ave = 0;
                    val_y_ave = 0;
                }

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

                magnitude = (float) Math.sqrt(Math.pow(val_x, 2) + Math.pow(val_y, 2));
                if (magnitude > threshold) {
                    //val_x_ave += val_x;
                    movingAverage_X.addToWindow(val_x);
                    //val_y_ave += val_y;
                    movingAverage_Y.addToWindow(val_y);
                    x_jerk = (val_x - val_x_pre)*time;
                    y_jerk = (val_y - val_y_pre)*time;
                }
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

    public void activateCalibrate(View view) {
        calibrating = true;
        x_total = 0;
        y_total = 0;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

