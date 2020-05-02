package Bluetooth;

import java.io.IOException;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;


public class BluetoothServer {
	private static UUID mm_uuid = new UUID("97c337c7a1484a8d9ccfeeb76cb477a0", false);		//UUID of program
	private static ConnectThread mm_connect_thread;											//Thread to control the connection to client
    private static boolean mm_connected = false;
    private WatchDiscoverability mm_watcher;
	
    StreamConnectionNotifier notifier;
	
	//Throws TimeoutException if discoverable for 1 minute and no connection
    //Throws BluetoothStateException if encountering a bluetooth error (probably off)
	public void openServer() throws BluetoothStateException {
		//Set up Bluetooth Server
		try {	
			
			final LocalDevice local_device = LocalDevice.getLocalDevice();

			//Set discoverable mode
            if (!local_device.setDiscoverable(DiscoveryAgent.LIAC)) System.out.println("Unable to start discoverability");
            else {
	            //Watch for discoverability to expire
	            mm_watcher = new WatchDiscoverability(this);
	            mm_watcher.start();
	            
	            System.out.println("Make " + local_device.getFriendlyName() + " discoverable\n");
	            System.out.println("UUID: " + mm_uuid);
	            
	            //Opens a discoverable connection with the built url
	            String url = "btspp://localhost:" + mm_uuid.toString() + ";name=PocketPointer";
	            notifier = (StreamConnectionNotifier) Connector.open(url);
	            
	            System.out.println("Local address: " + local_device.getBluetoothAddress());
	            System.out.println("Local name:  " + local_device.getFriendlyName() + "\n");
            }
            

            
		} catch (Exception e) {
			System.out.println("Failure in openServer:\n" + e + "\n");
            System.out.println("Error " + e.getMessage() + "\n");
            end();
            throw new BluetoothStateException();
		}
		
		//Begin searching for connection in seperate thread
		mm_connect_thread = new ConnectThread(this);
        mm_connect_thread.start();
	}
	
	public void restartServer() throws BluetoothStateException {
		mm_connect_thread = null;
		mm_watcher = null;
		mm_connected = false;
		notifier = null;
		
		openServer();
	}
	
	public boolean isConnected() {
		return mm_connected;	
	}
	
	public void setConnected(boolean connected) {
		mm_connected = connected;
	}
	
	public StreamConnectionNotifier getNotifier() {
		return notifier;
	}
	
	public void stopWatching() {
		mm_watcher.cancel();
	}
	
	public void end() {
		System.out.println("Stop server");
		
		//Shut down threads
        if (mm_connect_thread != null) {
        	mm_connect_thread.end();
        } else {
	        //Shut down notifier
	        if (notifier != null) {
	        	try {
					notifier.close();
				} catch (IOException e) {
					System.out.println("Warning: notifier was waiting for connection" + e + "\n");
				}
	        }
        }
        
        //Turn off discoverability if possible
        try {
        		LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
		} catch (BluetoothStateException ignored) {
			//Bluetooth is off, so it cannot be discoverable. No problem
		}
	}	
}
