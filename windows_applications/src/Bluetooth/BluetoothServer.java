package Bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.commons.io.IOUtils;


public class BluetoothServer {
	private static UUID mm_uuid = new UUID("97c337c7a1484a8d9ccfeeb76cb477a0", false);		//UUID of program
	private static ConnectThread mm_connect_thread;											//Thread to control the connection to client
    private static CommunicationThread mm_communication_thread;								//Thread to control the communcation with client
    private static boolean mm_connected = false;
	
    StreamConnectionNotifier notifier;
    StreamConnection connection = null;
	
	//Throws TimeoutException if discoverable for 1 minute and no connection
    //Throws BluetoothStateException if encountering a bluetooth error (probably off)
	public void openServer() throws TimeoutException, BluetoothStateException {
		try {	
			
			final LocalDevice local_device = LocalDevice.getLocalDevice();

			//Set discoverable mode
            local_device.setDiscoverable(DiscoveryAgent.LIAC);
            
            System.out.println("Make " + local_device.getFriendlyName() + " discoverable\n");
            System.out.println("UUID: " + mm_uuid);
            
            //Opens a discoverable connection with the built url
            String url = "btspp://localhost:" + mm_uuid.toString() + ";name=PocketPointer";
            notifier = (StreamConnectionNotifier) Connector.open(url);
            
            System.out.println("Local address: " + local_device.getBluetoothAddress());
            System.out.println("Local name:  " + local_device.getFriendlyName() + "\n");
            
		} catch (Exception e) {
			System.out.println("Failure in openServer:\n" + e + "\n");
            System.out.println("Error " + e.getMessage() + "\n");
            stop();
            throw new BluetoothStateException();
		}
		
		mm_connected = false;
		mm_connect_thread = new ConnectThread();
        mm_connect_thread.start();
        
        try {
        	
        	//Loop until discoverability reverts to NOT_DISCOVERABLE after 1 minute
        	while (LocalDevice.getLocalDevice().getDiscoverable() != DiscoveryAgent.NOT_DISCOVERABLE);
        
			if (!mm_connected) {
				System.out.println("Device has been discoverable for 1 minute without connecting. Stopping");
				stop();
				throw new TimeoutException();
			}
			
		} catch (BluetoothStateException e) {
			System.out.println("Warning: Bluetooth is not on. Cannot check discoverability.");
		}
	}
	
	public void simulateMessage() {
		while (!this.isConnected());
		mm_communication_thread.write("Test message from Windows");
	}
	
	public boolean isConnected() {
		return mm_connected;
	}
	
	
	public void stop() {
		System.out.println("Stop server");
		
		//Shut down threads
        if (mm_connect_thread != null) {
        	mm_connect_thread.cancel();
        	System.out.println("Stop connect thread");
        }
        
        //Shut down notifier
        if (notifier != null) {
        	try {
				notifier.close();
			} catch (IOException e) {
				System.out.println("Warning: notifier was waiting for connection" + e + "\n");
			}
        }
        
        //Turn off discoverability if possible
        try {
        		LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
		} catch (BluetoothStateException e) {
			//Bluetooth is off, so it cannot be discoverable. No problem
		}
	}	
	
	
	class ConnectThread extends Thread {

        private boolean running = true;

        @Override
        public void run() {

            System.out.println("Start connect thread");

            while (running) {
                try {
                	
                	//Wait until communication thread is free
                    while (mm_communication_thread != null && mm_communication_thread.isRunning()) {
                        sleep(1000);
                    }

                    System.out.println("Waiting for connection...");

//                    if (communicationThread != null) {
//                        System.out.println("Kill existing connection");
//                        communicationThread.setRunning(false);
//                        communicationThread.interrupt();
//                    }

                    //Wait for client to connect
                    connection = notifier.acceptAndOpen();
                    if (connection != null) {
                        //Start communication thread based off current connection to client
                        mm_communication_thread = new CommunicationThread(connection);
                        mm_communication_thread.start();
                        System.out.println("Connected");
                        mm_connected = true;
                        sleep(100);
                    } else {
                        System.out.println("No connection created");
                    }

                } catch (Exception e) {
                	if (e.getClass() == InterruptedIOException.class) {
                		System.out.println("Warning in ConnectThread: acceptAndOpen was interrupted while waiting for connection");
                	} else if (e.getClass() == IOException.class) {
                		System.out.println("Warning in ConnectThread: IOException handled\n" + e.getMessage() + "\n");
                	} else {
                		System.out.println("Failure in connect thread:\n" + e + e.getMessage() + "\n");
                	}
                    cancel();
                    return;
                }
            }
        }

        public void cancel() {
            running = false;
            
            if (mm_communication_thread != null) {
                mm_communication_thread.cancel();
            }
            
            //Shut down notifier
            if (notifier != null) {
            	try {
    				notifier.close();
    			} catch (IOException e) {
    				System.out.println("Warning: notifier was waiting for connection" + e + "\n");
    			}
            }
        }
    }

    class CommunicationThread extends Thread {

        private StreamConnection mm_connection = null;
        private InputStream mm_input_stream = null;
        private OutputStream mm_output_stream = null;
        private boolean mm_running = false;

        public CommunicationThread(StreamConnection a_connection) {
            super();
            mm_connection = a_connection;
            
            try {
				mm_input_stream = connection.openInputStream();
				mm_output_stream = connection.openOutputStream();
			} catch (IOException e) {
				System.out.println("Failed to open streams");
				e.printStackTrace();
			}
        }

        @Override
        public void run() {
            mm_running = true;
            try {

                while (mm_running) {
                	byte[] buffer = new byte[PPMessage.MESSAGE_SIZE];
                	int numBytes;

                	//Read from InputStream
                	numBytes = mm_input_stream.read(buffer);
                	if (numBytes < PPMessage.MESSAGE_SIZE) {
                		//TODO if numBytes is -1, connection broke
                		if (numBytes == -1) {
                			//Connection broke so close socket
                			this.cancel();
                			return;
                		}
                		System.out.println("Only read " + numBytes + ", not " + PPMessage.MESSAGE_SIZE);
                	}
                	
                	String message = new String(buffer, StandardCharsets.UTF_8);
                    System.out.println("Got: " + message);
	                    
                    sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failure in communication thread:\n" + e + "\n");
                try {
                    mm_connection.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Connection closed:");
            }
        }

        public void write(String message) {
            try {
            	
            	byte[] b = new byte[PPMessage.MESSAGE_SIZE];
            	b = message.getBytes(StandardCharsets.UTF_8);
                mm_output_stream.write(b);
                mm_output_stream.flush();
                System.out.println("Sent:\n" + message + "\n");

            } catch (IOException e) {
                System.out.println("Failure in write:");
                e.printStackTrace();
            }
        }

        public boolean isRunning() {
            return mm_running;
        }

        public void setRunning(boolean running) {
            mm_running = running;
        }
        
        public void cancel() {
            mm_running = false;
            
            try {
                mm_connection.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
