package com.example.testmouseapp.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.testmouseapp.R;
import com.example.testmouseapp.recyclerView.DevicesRecyclerViewAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class DevicesActivity extends AppCompatActivity implements DevicesRecyclerViewAdapter.ItemClickListener {

    private static final UUID mm_uuid = UUID.fromString("97c337c7-a148-4a8d-9ccf-eeb76cb477a0");

    private DevicesRecyclerViewAdapter mm_paired_adapter;
    private DevicesRecyclerViewAdapter mm_available_adapter;

    private ArrayList<String> mm_available_names = new ArrayList<>();
    private ArrayList<String> mm_paired_names = new ArrayList<>();
    private ArrayList<String> mm_scanned_names = new ArrayList<>();
    private ArrayList<BluetoothDevice> mm_available_devices = new ArrayList<BluetoothDevice>();
    private ArrayList<BluetoothDevice> mm_scanned_devices = new ArrayList<BluetoothDevice>();

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

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
        bluetoothAdapter.startDiscovery();

        CheckDeviceList c = new CheckDeviceList();
        c.start();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if (mm_scanned_names.indexOf(device.getName()) == -1) {
                    mm_scanned_devices.add(device);
                    mm_scanned_names.add(device.getName());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Discovery canceled", Toast.LENGTH_SHORT).show();
                // discovery has finished, give a call to fetchUuidsWithSdp on first device in list.
                if (!mm_scanned_devices.isEmpty()) {
                    BluetoothDevice device = mm_scanned_devices.remove(0);
                    boolean result = device.fetchUuidsWithSdp();
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
                            mm_available_devices.add(deviceExtra);
                            String device_name = deviceExtra.getName();
                            if (mm_available_names.indexOf(device_name) == -1) {
                                assert device_name != null;
                                mm_available_names.add(device_name);
                                mm_available_adapter.notifyItemInserted(mm_available_names.indexOf(device_name));
                            }
                        }
                    }
                }
                if (!mm_scanned_devices.isEmpty()) {
                    BluetoothDevice device = mm_scanned_devices.remove(0);
                    boolean result = device.fetchUuidsWithSdp();
                } else bluetoothAdapter.startDiscovery();
            }


                /*if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                String device_name = device.getName();
                ParcelUuid[] uuids = device.getUuids();
                if (uuids != null) {
                    Toast.makeText(context, "UUID: " + uuids[0].getUuid().toString(), Toast.LENGTH_SHORT).show();
                } else Toast.makeText(context, "uuids is null", Toast.LENGTH_SHORT).show();
                assert device_name != null;
                available_devices.add(device_name);
                available_adapter.notifyItemInserted(available_devices.indexOf(device_name));

            } */ else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (!bluetoothAdapter.isEnabled()) finish();
                Toast.makeText(context, "You must keep Bluetooth enabled", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);

    }

    @Override
    public void onItemClick(View view, int position, DevicesRecyclerViewAdapter adapter) {
        String name = adapter.getItem(position);
        for (BluetoothDevice d : mm_available_devices) {
            if (d.getName().equals(name)) {
                Toast.makeText(this, "You clicked " + d.getName() + ". Device is " + d.toString(), Toast.LENGTH_SHORT).show();
                ConnectThread connectThread = new ConnectThread(d, this);
                connectThread.start();
            } else Toast.makeText(this, "Device not found in list" + position, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }


    private class CheckDeviceList extends Thread {
        private boolean running = true;

        public void run() {
            while (running) {
                try {

                    sleep(1000);
                    if (!mm_scanned_devices.isEmpty()) {

                        bluetoothAdapter.cancelDiscovery();
                        sleep(500);
                        bluetoothAdapter.startDiscovery();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopChecking() {
            running = false;
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final Context context;

        public ConnectThread(BluetoothDevice device, Context context) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            this.context = context;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice based on the program's UUID
                tmp = device.createRfcommSocketToServiceRecord(mm_uuid);
            } catch (IOException e) {
                Toast.makeText(context, "Socket's create() method failed", Toast.LENGTH_SHORT).show();
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Toast.makeText(context, "Could not close the client socket", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(context, "Could not close the client socket", Toast.LENGTH_SHORT).show();
            }
        }
    }

}


