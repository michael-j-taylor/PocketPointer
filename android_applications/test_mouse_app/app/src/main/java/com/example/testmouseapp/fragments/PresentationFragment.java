package com.example.testmouseapp.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.testmouseapp.R;
import com.example.testmouseapp.activities.MainActivity;
import com.example.testmouseapp.dataOperations.PPMessage;
import com.google.android.material.navigation.NavigationView;

public class PresentationFragment extends Fragment {

    private static final String TAG = "Presentation Activity";
    private MainActivity mm_main_activity;
    private View view;

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

        //hide any items not relevant to this fragment
        menuItem_mouse_lock.setVisible(false);

        // show all items relevant to this fragment

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

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        TextView device_view = view.findViewById(R.id.presentationDeviceText);
        if (mm_main_activity.bt_service != null && mm_main_activity.bt_service.isConnected()) {
            String s = "Connected to " + mm_main_activity.bt_service.device.getName();
            device_view.setText(s);
        } else {
            device_view.setText(R.string.not_connected);
        }
    }





    /*----------BLUETOOTH MESSAGING FUNCTIONS----------*/


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
