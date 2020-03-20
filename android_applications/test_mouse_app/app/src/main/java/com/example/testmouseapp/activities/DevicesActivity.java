package com.example.testmouseapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.testmouseapp.R;
import com.example.testmouseapp.recyclerView.DevicesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Set;


public class DevicesActivity extends AppCompatActivity implements DevicesRecyclerViewAdapter.ItemClickListener {

    DevicesRecyclerViewAdapter adapter;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ArrayList<String> availableDevices = new ArrayList<>();
    ArrayList<String> pairedDevices = new ArrayList<>();
    Set<BluetoothDevice> unpairedDevices = bluetoothAdapter.getBondedDevices();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        //Register for broadcasts when the Bluetooth state is changed
        IntentFilter state_filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(state_receiver, state_filter);
        // Register for broadcasts when a device is discovered.
        IntentFilter device_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(device_receiver, device_filter);

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if (bondedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : bondedDevices) {
                String device_name = device.getName();
                pairedDevices.add(device_name);
            }
        }

        // set up  pairedDevices_recyclerView
        RecyclerView pairedDevices_recyclerView = findViewById(R.id.pairedDevices_recyclerView);
        LinearLayoutManager paired_linearLayoutManager = new LinearLayoutManager(this);
        pairedDevices_recyclerView.setLayoutManager(paired_linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pairedDevices_recyclerView.getContext(), paired_linearLayoutManager.getOrientation());
        pairedDevices_recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new DevicesRecyclerViewAdapter(this, pairedDevices);
        adapter.setClickListener(this);
        pairedDevices_recyclerView.setAdapter(adapter);

        // set up the availableDevices_recyclerView
        RecyclerView availableDevices_recyclerView = findViewById(R.id.availableDevices_recyclerView);
        LinearLayoutManager available_linearLayoutManager = new LinearLayoutManager(this);
        availableDevices_recyclerView.setLayoutManager(available_linearLayoutManager);
        availableDevices_recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new DevicesRecyclerViewAdapter(this, availableDevices);
        adapter.setClickListener(this);
        availableDevices_recyclerView.setAdapter(adapter);

        if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver device_receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String device_name = device.getName();
                assert device_name != null;
                availableDevices.add(device_name);
                adapter.notifyItemInserted(availableDevices.indexOf(device_name));
            }
        }
    };

    // Create a BroadcastReceiver to monitor the Bluetooth state
    private final BroadcastReceiver state_receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (!bluetoothAdapter.isEnabled()) finish();
                Toast.makeText(context, "You must keep Bluetooth enabled", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(device_receiver);
        Toast.makeText(this, "Discovery canceled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        availableDevices.remove(position);
        adapter.notifyItemRemoved(position);
        availableDevices.add("Test Device");
        adapter.notifyItemInserted(availableDevices.size()-1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }



}
