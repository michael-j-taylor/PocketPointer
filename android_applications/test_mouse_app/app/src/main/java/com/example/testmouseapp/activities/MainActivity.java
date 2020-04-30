package com.example.testmouseapp.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.testmouseapp.R;
import com.example.testmouseapp.dataOperations.KeyPressListener;
import com.example.testmouseapp.fragments.HomeFragment;
import com.example.testmouseapp.fragments.PresentationFragment;
import com.example.testmouseapp.fragments.TouchpadFragment;
import com.example.testmouseapp.services.BluetoothService;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements BluetoothService.ServiceCallback{
    private static final String TAG = "Main Activity";

    private AppBarConfiguration mAppBarConfiguration;
    public NavigationView navigationView;
    private KeyPressListener listener;
    public boolean overrideVolumeKeys = false;


    //used to communicate with an instance of PresentationFragment to override volume keys
    public void setKeyPressListener(KeyPressListener keyPressListener) {
        this.listener = keyPressListener;
    }

    public Button button_connect;
    public Button button_disconnect;

    //Bluetooth vars
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final int REQUEST_ENABLE_BT = 3;
    private final int SHOW_DEVICES = 9;
    private final int REQUEST_FINE_LOCATION = 6;
    private final int REQUEST_COARSE_LOCATION = 12;

    public BluetoothService bt_service;
    private boolean mm_bound;
    private ServiceConnection mm_connection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bt_service = binder.getService();
            mm_bound = true;
            bt_service.setCallbacks(MainActivity.this);
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

        // Bind to BluetoothService
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mm_connection, Context.BIND_AUTO_CREATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_presentation, R.id.nav_touchpad)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //create instance of PresentationFragment
        //note that this is NOT the same instance of the fragment the user interacts with
        //this is neccessary to call the fragment function when volume keys are overridden
        PresentationFragment pfragment = new PresentationFragment();
        setKeyPressListener(pfragment);


        //Register connect device button listener
        button_connect = findViewById(R.id.footer_button_connect_device);
        button_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectDevice();
            }
        });

        //Register disconnect device button listener
        button_disconnect = findViewById(R.id.footer_button_disconnect_device);
        button_disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnectDevice();
            }
        });
    }

    private void connectDevice() {
        if (bluetoothAdapter == null) {
            String noBtMsg = "Your device does not support Bluetooth. Please connect using a USB cable.";

            Toast noBtToast = Toast.makeText(this, noBtMsg, Toast.LENGTH_LONG);
            noBtToast.show();
        }
        else {
            Log.d(TAG, "Build version is " + Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                } else {
                    enableBluetooth();
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
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
            Intent showDevices = new Intent(this, DevicesActivity.class);
            startActivityForResult(showDevices, SHOW_DEVICES);
        }
    }

    private void disconnectDevice() {
        bt_service.closeConnection();

        TextView device_view = findViewById(R.id.homeDeviceText);

        device_view.setText(R.string.not_connected);
        button_connect.setVisibility(View.VISIBLE);
        button_disconnect.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                //String btEnabledMsg = "Thank you for activating Bluetooth.";
                //Toast noBtToast = Toast.makeText(getApplicationContext(), btEnabledMsg, Toast.LENGTH_LONG);
                //noBtToast.show();
                Intent showDevices = new Intent(this, DevicesActivity.class);
                startActivityForResult(showDevices, SHOW_DEVICES);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "You must enable Bluetooth for wireless connection.", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else
                Toast.makeText(this, "You must enable location permissions to discover devices", Toast.LENGTH_LONG).show();

        }
        if (requestCode == REQUEST_COARSE_LOCATION) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else
                Toast.makeText(this, "You must enable location permissions to discover devices", Toast.LENGTH_LONG).show();

        }
        if (requestCode == SHOW_DEVICES) {
            if (resultCode == Activity.RESULT_OK) {
                bt_service.openConnection((BluetoothDevice) data.getParcelableExtra("device"));
            }
        }
    }

    public void updateConnection() {
        //Fragment visible_frag = getVisibleFragment();
        //TextView device_view;
        //if (visible_frag instanceof HomeFragment) {
        //    device_view = navigationView.inflate().findViewById(R.id.homeDeviceText);
        //    Log.d(TAG, "In HomeFragment");
        //} else if (visible_frag instanceof TouchpadFragment) {
        //    device_view = findViewById(R.id.touchpadDeviceText);
        //    Log.d(TAG, "In TouchpadFragment");
        //} else {
        //    device_view = findViewById(R.id.presentationDeviceText);
        //    Log.d(TAG, "In PresentationFragment");
        //}

        String s = "Connected to " + bt_service.device.getName();
        //device_view.setText(s);
        button_connect.setVisibility(View.INVISIBLE);
        button_disconnect.setVisibility(View.VISIBLE);
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for(Fragment fragment : fragments){
        if(fragment != null && fragment.isVisible())
            Log.d(TAG, "Visible fragment is " + fragment);
            return fragment;
        }
        return null;
    }

    //Used to override volume keys in PresentationMode fragment
    //this method cannot be used in a fragment, so is overridden here
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (overrideVolumeKeys) {
            int keyCode = event.getKeyCode();

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

                //otherwise this function is called on key down AND up
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    listener.onKeyDown(event.getKeyCode());
                    return true;
                }

                //ignore event sent when key is released
                else if (event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
            }
        }

        //allow every other key to perform default functionality
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void onDestroy() {
        if (mm_bound) {
            bt_service.setCallbacks(null);
            unbindService(mm_connection);
            mm_bound = false;
        }
        super.onDestroy();
    }
}