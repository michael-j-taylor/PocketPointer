package com.example.testmouseapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;


public class SettingsFragment extends Fragment {

    private static final String TAG = "Presentation Activity";
    private MainActivity mm_main_activity;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        mm_main_activity = (MainActivity) getActivity();
        assert mm_main_activity != null;


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
