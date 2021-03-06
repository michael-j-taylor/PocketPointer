package com.example.testmouseapp.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;
import com.google.android.material.navigation.NavigationView;


public class SettingsFragment extends Fragment {

    private static final String TAG = "Presentation Activity";
    private MainActivity mm_main_activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //show action bar for this fragment
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mm_main_activity = (MainActivity) getActivity();
        assert mm_main_activity != null;

        /*----------VOLATILE NAVIGATION DRAWER BUTTON CREATION----------*/
        //using the public NavigationView in our MainActivity, we can access navigation drawer elements
        //and interact with them. This allows the setup of quick settings for each mode of the application
        NavigationView navigationView = mm_main_activity.navigationView;

        //get all quick setting menu items
        MenuItem menuItem_mouse_lock = navigationView.getMenu().findItem(R.id.nav_switch_mousemode);
        MenuItem menuItem_switch_overrideVolumeKeys = navigationView.getMenu().findItem(R.id.nav_switch_override_volume_keys);

        //hide any items not relevant to this
        menuItem_mouse_lock.setVisible(false);
        menuItem_switch_overrideVolumeKeys.setVisible(false);

        // show all items relevant to this fragment

        /*----------STANDARD BUTTON CREATION----------*/


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        //TextView device_view = view.findViewById(R.id.presentationDeviceText);
        if (mm_main_activity.bt_service != null && mm_main_activity.bt_service.isConnected()) {
            String s = "Connected to " + mm_main_activity.bt_service.device.getName();
            //device_view.setText(s);
        } else {
            //device_view.setText(R.string.not_connected);
        }
    }


}
