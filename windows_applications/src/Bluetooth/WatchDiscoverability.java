package Bluetooth;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;

class WatchDiscoverability extends Thread {
	private boolean mm_running = true;
	private final BluetoothServer mm_server;
	
	public WatchDiscoverability(BluetoothServer server) {
		mm_server = server;
	}
	
	public void run() {
		System.out.println("Start watcher thread");
		try {
        	while (mm_running) {
        		//If discoverability reverts to NOT_DISCOVERABLE after 1 minute before connecting, shut down server
        		if (LocalDevice.getLocalDevice().getDiscoverable() == DiscoveryAgent.NOT_DISCOVERABLE) {
        			if (!mm_server.isConnected()) {
        				System.out.println("Device has been discoverable for 1 minute without connecting. Stopping");
        				mm_server.end(false);
        				mm_running = false;
        			}
        		}
        	}
		} catch (BluetoothStateException e) {
			System.out.println("Warning: Bluetooth is not on. Cannot check discoverability.");
		}
		
		System.out.println("Stop watcher thread");
	}

	public boolean isRunning() {
		return mm_running;
	}
	
	public void end() {
		mm_running = false;
	}
}
