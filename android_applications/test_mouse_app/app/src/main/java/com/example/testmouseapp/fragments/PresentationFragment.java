package com.example.testmouseapp.fragments;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.example.testmouseapp.dataOperations.KeyPressListener;
import com.google.android.material.navigation.NavigationView;

public class PresentationFragment extends Fragment implements KeyPressListener {

    private static final String TAG = "Presentation Activity";
    private MainActivity mm_main_activity;
    private View view;
    private MainActivity mActivity;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //show action bar for this fragment
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        view = inflater.inflate(R.layout.fragment_presentation, container, false);

        mm_main_activity = (MainActivity) getActivity();
        assert mm_main_activity != null;

        /*----------VOLATILE NAVIGATION DRAWER BUTTON CREATION----------*/
        //using the public NavigationView in our MainActivity, we can access navigation drawer elements
        //and interact with them. This allows the setup of quick settings for each mode of the application
        NavigationView navigationView = mm_main_activity.navigationView;

        //get all quick setting menu items
        MenuItem menuItem_mouse_lock = navigationView.getMenu().findItem(R.id.nav_switch_mousemode);
        MenuItem menuItem_switch_override_volume_keys = navigationView.getMenu().findItem(R.id.nav_switch_override_volume_keys);

        //hide any items not relevant to this fragment
        menuItem_mouse_lock.setVisible(false);

        // show all items relevant to this fragment
        menuItem_switch_override_volume_keys.setVisible(true);

        //volume lock switch
        //use volume up and down keys as inputs to connected device
        //enable warning bar over fragment

        SwitchCompat button_override_volume_keys = menuItem_switch_override_volume_keys.getActionView().findViewById(R.id.menu_switch_override_volume_keys);
        button_override_volume_keys.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mm_main_activity.overrideVolumeKeys = true;
                    setVolumeOverrideMode(R.color.PPAlert_red);
                    Log.d(TAG, "override = " + mm_main_activity.overrideVolumeKeys);
                } else {
                    mm_main_activity.overrideVolumeKeys = false;
                    setVolumeOverrideMode(R.color.PPdark_grey);
                    Log.d(TAG, "override = " + mm_main_activity.overrideVolumeKeys);
                }
            }
        });


        /*----------STANDARD BUTTON CREATION----------*/

        //Register nextslide button listener
        Button button_nextslide = view.findViewById(R.id.button_nextslide);
        button_nextslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextSlide();
            }
        });

        //Register prevslide button listener
        Button button_prevslide = view.findViewById(R.id.button_prevslide);
        button_prevslide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousSlide();
            }
        });

        Log.d(TAG, "TEST1: " + mActivity);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        TextView device_view = view.findViewById(R.id.presentationDeviceText);
        if (mActivity.bt_service != null && mActivity.bt_service.isConnected()) {
            String s = "Connected to " + mActivity.bt_service.device.getName();
            device_view.setText(s);
        } else {
            device_view.setText(R.string.not_connected);
        }
    }


    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);

        if (ctx instanceof MainActivity){
            mActivity = (MainActivity) ctx;
        }
    }


    //programatically change color of status bar to specified color
    public void setVolumeOverrideMode(int resourceColor) {

        ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(resourceColor)));

    }


    /*----------BLUETOOTH MESSAGING FUNCTIONS----------*/


    //called in MainActivity when overridden dispatchKeyEvent function detects keypress
    @Override
    public void onKeyDown(int key_code) {
        Log.d(TAG, "Fragment method called");

        if (key_code == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d(TAG, "volume up");
            //TODO: send PPMessage here
        }
        else if (key_code == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d(TAG, "volume down");
        }
    }


    //called when button_nextslide is toggled
    private void nextSlide() {
        try {
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "RIGHT"));
        } catch (IllegalStateException ignored) { }
    }


    //called when button_prevslide is toggled
    private void previousSlide() {
        try {
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "LEFT"));
        } catch (IllegalStateException ignored) { }
    }
}
