package com.example.testmouseapp.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testmouseapp.R;
import com.example.testmouseapp.recyclerView.DevicesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class DevicesActivity extends AppCompatActivity implements DevicesRecyclerViewAdapter.ItemClickListener {
    public static boolean active = false;

    private static final UUID mm_uuid= UUID.fromString("97c337c7-a148-4a8d-9ccf-eeb76cb477a0");

    private DevicesRecyclerViewAdapter mm_paired_adapter;
    private DevicesRecyclerViewAdapter mm_available_adapter;

    private ArrayList<String> mm_available_names = new ArrayList<>();
    private ArrayList<String> mm_paired_names = new ArrayList<>();
    private ArrayList<String> mm_scanned_names = new ArrayList<>();
    private ArrayList<BluetoothDevice> mm_available_devices = new ArrayList<>();
    private ArrayList<BluetoothDevice> mm_paired_devices = new ArrayList<>();
    private ArrayList<BluetoothDevice> mm_scanned_devices = new ArrayList<>();
    private CheckDeviceList check_devices = new CheckDeviceList();

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final String TAG = "DevicesActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        active = true;

        //Set up toolbar for this activity
        Toolbar toolbar = findViewById(R.id.devices_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.devices_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        //Register for broadcasts when the Bluetooth state is changed
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        //Register for broadcasts when a device is discovered
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //Register for broadcasts when discovery finishes
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        //Register for broadcasts when UUIDS are found
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_UUID));

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if (bondedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : bondedDevices) {
                String device_name = device.getName();
                ParcelUuid[] device_uuids = device.getUuids();
                for (ParcelUuid u : device_uuids) {
                    if (u.getUuid().equals(mm_uuid)) {
                        mm_paired_names.add(device_name);
                        mm_paired_devices.add(device);
                        break;
                    }
                }
            }
        }

        // set up  pairedDevices_recyclerView
        RecyclerView pairedDevices_recyclerView = findViewById(R.id.pairedDevices_recyclerView);
        LinearLayoutManager paired_linearLayoutManager = new LinearLayoutManager(this);
        pairedDevices_recyclerView.setLayoutManager(paired_linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pairedDevices_recyclerView.getContext(), paired_linearLayoutManager.getOrientation());
        pairedDevices_recyclerView.addItemDecoration(dividerItemDecoration);
        mm_paired_adapter = new DevicesRecyclerViewAdapter(this, mm_paired_names);
        mm_paired_adapter.setClickListener(this);
        pairedDevices_recyclerView.setAdapter(mm_paired_adapter);

        // set up the availableDevices_recyclerView
        RecyclerView availableDevices_recyclerView = findViewById(R.id.availableDevices_recyclerView);
        LinearLayoutManager available_linearLayoutManager = new LinearLayoutManager(this);
        availableDevices_recyclerView.setLayoutManager(available_linearLayoutManager);
        availableDevices_recyclerView.addItemDecoration(dividerItemDecoration);
        mm_available_adapter = new DevicesRecyclerViewAdapter(this, mm_available_names);
        mm_available_adapter.setClickListener(this);
        availableDevices_recyclerView.setAdapter(mm_available_adapter);

        if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "about to start discovery");
        check_devices.start();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "found device");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if (mm_scanned_names.indexOf(device.getName()) == -1) {
                    //If the found device has not already been scanned, check it
                    mm_scanned_devices.add(device);
                    mm_scanned_names.add(device.getName());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // discovery has finished, give a call to fetchUuidsWithSdp on first device in list.
                if (!mm_scanned_devices.isEmpty()) {
                    BluetoothDevice device = mm_scanned_devices.remove(0);
                    device.fetchUuidsWithSdp();
                }

            } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                // This is when we can be assured that fetchUuidsWithSdp has completed.
                // So get the uuids and call fetchUuidsWithSdp on another device in list

                BluetoothDevice deviceExtra = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                if (uuidExtra != null) {
                    for (Parcelable p : uuidExtra) {
                        ParcelUuid uuid = (ParcelUuid) p;
                        if (uuid.getUuid().equals(mm_uuid)) {
                            assert deviceExtra != null;
                            String device_name = deviceExtra.getName();
                            assert device_name != null;
                            if (mm_available_names.indexOf(device_name) == -1 && mm_paired_names.indexOf(device_name) == -1) {
                                //If the device with the correct UUID is not already in the available or paired list, add it to the available list
                                mm_available_devices.add(deviceExtra);
                                mm_available_names.add(device_name);
                                mm_available_adapter.notifyItemInserted(mm_available_names.indexOf(device_name));
                            }
                        }
                    }
                }
                if (!mm_scanned_devices.isEmpty()) {
                    BluetoothDevice device = mm_scanned_devices.remove(0);
                    device.fetchUuidsWithSdp();
                }

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(), "You must keep Bluetooth enabled", Toast.LENGTH_SHORT).show();
                    DevicesActivity.this.setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
    };

    public static UUID getUuid() {
        return mm_uuid;
    }


    @Override
    public void onItemClick(View view, int position, DevicesRecyclerViewAdapter adapter) {
        String name = adapter.getItem(position);
        ArrayList<BluetoothDevice> devices;
        if (adapter.equals(mm_paired_adapter)) devices = mm_paired_devices;
        else devices = mm_available_devices;


        for (BluetoothDevice d : devices) {
            if (d.getName().equals(name)) {
                //Return d to calling activity
                check_devices.stopChecking();
                Toast.makeText(this, "Attempting to connect to " + d.getName(), Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("device", d);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else Toast.makeText(this, "Device " + position +" not found in list", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;

        if (check_devices.isRunning()) check_devices.stopChecking();
        unregisterReceiver(receiver);

    }


    private class CheckDeviceList extends Thread {
        private boolean running = true;

        public void run() {
            while (running) {
                try {
                    bluetoothAdapter.startDiscovery();
                    Log.d(TAG, "discovery started");
                    sleep(1000);
                    if (!mm_scanned_devices.isEmpty()) {

                        bluetoothAdapter.cancelDiscovery();
                        sleep(500);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void stopChecking() {
            running = false;
        }

        boolean isRunning() {
            return running;
        }
    }

}


