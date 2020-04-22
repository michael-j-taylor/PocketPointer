package com.example.testmouseapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.PPMessage;

public class PresentationFragment extends Fragment {

    private static final String TAG = "Presentation Activity";
    private MainActivity mm_main_activity;
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_presentation, container, false);

        mm_main_activity = (MainActivity) getActivity();
        assert mm_main_activity != null;

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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        TextView device_view = view.findViewById(R.id.presentationDeviceText);
        if (mm_main_activity.bt_service.isConnected()) {
            String s = "Connected to " + mm_main_activity.bt_service.device.getName();
            device_view.setText(s);
        } else {
            device_view.setText(R.string.not_connected);
        }
    }

    private void nextSlide() {
        try {
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "RIGHT"));
        } catch (IllegalStateException ignored) { }
    }

    private void previousSlide() {
        try {
            mm_main_activity.bt_service.writeMessage(new PPMessage(PPMessage.Command.KEY_PRESS, "LEFT"));
        } catch (IllegalStateException ignored) { }
    }
}
