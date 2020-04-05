package com.example.testmouseapp.activities;

import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.testmouseapp.R;
import com.example.testmouseapp.recyclerView.DevicesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class DevicesActivity extends AppCompatActivity implements DevicesRecyclerViewAdapter.ItemClickListener {

    private static final UUID mm_uuid = UUID.fromString("97c337c7-a148-4a8d-9ccf-eeb76cb477a0");

    DevicesRecyclerViewAdapter paired_adapter;
    DevicesRecyclerViewAdapter available_adapter;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ArrayList<String> available_devices = new ArrayList<>();
    ArrayList<String> paired_devices = new ArrayList<>();
    ArrayList<String> unpaired_devices = new ArrayList<>();
    ArrayList<BluetoothDevice> device_list = new ArrayList<BluetoothDevice>();



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
                        paired_devices.add(device_name);
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
        paired_adapter = new DevicesRecyclerViewAdapter(this, paired_devices);
        paired_adapter.setClickListener(this);
        pairedDevices_recyclerView.setAdapter(paired_adapter);

        // set up the availableDevices_recyclerView
        RecyclerView availableDevices_recyclerView = findViewById(R.id.availableDevices_recyclerView);
        LinearLayoutManager available_linearLayoutManager = new LinearLayoutManager(this);
        availableDevices_recyclerView.setLayoutManager(available_linearLayoutManager);
        availableDevices_recyclerView.addItemDecoration(dividerItemDecoration);
        available_adapter = new DevicesRecyclerViewAdapter(this, available_devices);
        available_adapter.setClickListener(this);
        availableDevices_recyclerView.setAdapter(available_adapter);

        if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();

        Toast.makeText(this, mm_uuid.toString(), Toast.LENGTH_SHORT).show();

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
                if (unpaired_devices.indexOf(device.getName()) == -1) {
                    device_list.add(device);
                    unpaired_devices.add(device.getName());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Discovery canceled", Toast.LENGTH_SHORT).show();
                // discovery has finished, give a call to fetchUuidsWithSdp on first device in list.
                if (!device_list.isEmpty()) {
                    BluetoothDevice device = device_list.remove(0);
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
                            Toast.makeText(context, "uuid matches", Toast.LENGTH_SHORT).show();
                            assert deviceExtra != null;
                            String device_name = deviceExtra.getName();
                            if (available_devices.indexOf(device_name) == -1) {
                                assert device_name != null;
                                available_devices.add(device_name);
                                available_adapter.notifyItemInserted(available_devices.indexOf(device_name));
                            }
                        }
                        else Toast.makeText(context, deviceExtra.getName() + " - " + ((ParcelUuid)p).getUuid().toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "uuidExtra is still null", Toast.LENGTH_SHORT).show();
                }
                if (!device_list.isEmpty()) {
                    BluetoothDevice device = device_list.remove(0);
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
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }


    class CheckDeviceList extends Thread {
        private boolean running = true;

        public void run() {
            while (running) {
                try {

                    sleep(1000);
                    if (!device_list.isEmpty()) {

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

}
