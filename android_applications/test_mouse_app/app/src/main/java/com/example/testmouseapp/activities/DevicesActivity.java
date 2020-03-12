package com.example.testmouseapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.testmouseapp.R;
import com.example.testmouseapp.recyclerView.DevicesRecyclerViewAdapter;

import java.util.ArrayList;


public class DevicesActivity extends AppCompatActivity implements DevicesRecyclerViewAdapter.ItemClickListener {

    DevicesRecyclerViewAdapter adapter;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);



        // data to populate the RecyclerView with
        ArrayList<String> availableDevices = new ArrayList<>();
        availableDevices.add("Horse");
        availableDevices.add("Cow");
        availableDevices.add("Camel");
        availableDevices.add("Sheep");
        availableDevices.add("Goat");

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.devices_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new DevicesRecyclerViewAdapter(this, availableDevices);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }



}
