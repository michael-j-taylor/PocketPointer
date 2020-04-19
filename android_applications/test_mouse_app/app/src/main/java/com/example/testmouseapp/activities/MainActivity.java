package com.example.testmouseapp.activities;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.testmouseapp.R;
import com.example.testmouseapp.dataOperations.Calibrater;
import com.example.testmouseapp.dataOperations.MovingAverage;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.services.BluetoothService;

import java.io.IOException;
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

    final int polling_rate = 60; //in Hz
    float time;

    Calibrater calibrater = new Calibrater(1000);

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
    final int REQUEST_ENABLE_BT = 3;
    final int OPEN_BT_SETTINGS = 6;
    final int SHOW_DEVICES = 9;
    final int REQUEST_COARSE_LOCATION = 12;
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

        //HIDDEN FOR DEMO PURPOSES
        /*
        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activateCalibrate(v);
            }
        });
         */

        //button setup
        Button button_nextslide = findViewById(R.id.button_nextslide);
        Button button_prevslide = findViewById(R.id.button_prevslide);
        Button button_hidescreen = findViewById(R.id.button_hidescreen);

        button_nextslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendKeyPress("RIGHT");
            }
        });

        button_prevslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendKeyPress("LEFT");
            }
        });

        button_hidescreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendKeyPress("B");
            }
        });

        //Set up Action Bar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        // Bind to BluetoothService
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mm_connection, Context.BIND_AUTO_CREATE);
    }

    //on sensor value change, display X and Z values
    @Override
    public void onSensorChanged(SensorEvent event) {

        //TODO: move the below objects out of this function - they don't need to be initialized every time the sensor updates
        // HIDDEN FOR DEMO PURPOSES
        TextView live_acceleration;
        //TextView max_acceleration;
        //TextView position;
        live_acceleration = findViewById(R.id.acceleration);
        //max_acceleration = findViewById(R.id.maximums);
        //position = findViewById(R.id.position);
        //TextView threshold_text = findViewById(R.id.threshold);


        currentTime = Calendar.getInstance().getTimeInMillis();

        raw_x = event.values[0];
        raw_y = event.values[1];

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

    /*
    temporary, for project demo
    writes string s to bluetooth buffer
     */
    public void sendKeyPress(String s) {
        mm_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, s));
    }

    public void testMessages(View view) {
        //Send messages to server here
        mm_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 1 from client"));
        mm_service.writeMessage(new PPMessage(PPMessage.Command.STRING, "Test message 2 from client\n"));
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
                assert d != null;
                try {
                    mm_service.openConnection(d);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to connect to " + d.getName(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to connect to " + d.getName());
                    Intent showDevices = new Intent(this, DevicesActivity.class);
                    startActivityForResult(showDevices, SHOW_DEVICES);
                }
            }
        }
    }

    public void onDestroy() {
        if (mm_bound) {
            unbindService(mm_connection);
            mm_bound = false;
        }
        super.onDestroy();
    }
}