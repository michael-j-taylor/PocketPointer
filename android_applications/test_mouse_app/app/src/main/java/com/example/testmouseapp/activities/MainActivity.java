package com.example.testmouseapp.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.testmouseapp.R;
import com.example.testmouseapp.dataOperations.KeyPressListener;
import com.example.testmouseapp.fragments.PresentationFragment;
import com.example.testmouseapp.services.BluetoothService;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";

    private AppBarConfiguration mAppBarConfiguration;
    public NavigationView navigationView;
    private KeyPressListener listener;
    public boolean overrideVolumeKeys = false;


    //used to communicate with an instance of PresentationFragment to override volume keys
    public void setKeyPressListener(KeyPressListener keyPressListener) {
        this.listener = keyPressListener;
    }

    //Bluetooth vars
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
            unbindService(mm_connection);
            mm_bound = false;
        }
        super.onDestroy();
    }
}