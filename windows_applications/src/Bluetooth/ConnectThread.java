package Bluetooth;

import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

class ConnectThread extends Thread {

    private boolean running = true;
    private CommunicationThread mm_communication_thread;
    private final BluetoothServer mm_server;
    private StreamConnection mm_connection_stream;

	public ConnectThread(BluetoothServer server) {
    	mm_server = server;
    }

	@Override
    public void run() {

        System.out.println("Start connect thread");

        while (running) {
            try {
            	
            	//Wait until communication thread is free
                while (mm_communication_thread != null && mm_communication_thread.isRunning()) {
                    sleep(1000);
                }
                if (mm_communication_thread != null && !mm_communication_thread.isRunning()) return;
                
                System.out.println("Waiting for connection...");
                mm_server.getWindow().connectingOutput.setText("Waiting for connection...");


                //Wait for client to connect
                mm_connection_stream = mm_server.getNotifier().acceptAndOpen();
                if (mm_connection_stream != null) {
                    //Start communication thread based off current connection to client
                    mm_communication_thread = new CommunicationThread(mm_server, this);
                	mm_communication_thread.start();
                	
                	successfulConnection();

                    sleep(100);
                } else {
                    System.out.println("No connection created");
                }

            } catch (Exception e) {
            	if (!(e instanceof IOException)) {
            		e.printStackTrace();
            	}
                end();
                return;
            }
        }
        
    	System.out.println("Stop connect thread");
    }
	
	private void successfulConnection() {
    	System.out.println("Connected to remote device");
        mm_server.successfulConnection();

        //Send test message
		mm_communication_thread.write(new PPMessage(PPMessage.Command.STRING, "Test message from Windows\n"));
    	
    	//Stop watching for expired discoverability
    	mm_server.stopWatching();
    	
    	//Turn off discoverability if possible
        try {
        		LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
		} catch (BluetoothStateException ignored) {
			//Bluetooth is off, so it cannot be discoverable. No problem
		}
	}
	
	public StreamConnection getStream() {
		return mm_connection_stream;
	}

	public RemoteDevice getConnectedDevice() throws IOException {
	    return RemoteDevice.getRemoteDevice(mm_connection_stream);
    }

    public CommunicationThread getComs() {
	    return mm_communication_thread;
    }

    public void end() {
        running = false;
        
        if (mm_communication_thread != null) {
            mm_communication_thread.end();
        } else {
            try {
            	if (mm_connection_stream != null) mm_connection_stream.close();
            } catch (IOException ignored) {
            }
        }
        
        //Shut down notifier
        if (mm_server.getNotifier() != null) {
        	try {
				mm_server.getNotifier().close();
			} catch (IOException ignored) {
			}
        }
    }
}
