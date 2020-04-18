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

import Driver.MouseRobot;


public class BluetoothServer {
	private static UUID mm_uuid = new UUID("97c337c7a1484a8d9ccfeeb76cb477a0", false);		//UUID of program
	private static ConnectThread mm_connect_thread;											//Thread to control the connection to client
    private static CommunicationThread mm_communication_thread;								//Thread to control the communcation with client
    private static boolean mm_connected = false;
    private final Object lock = new Object();
	
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
            end();
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
				end();
				throw new TimeoutException();
			}
			
		} catch (BluetoothStateException e) {
			System.out.println("Warning: Bluetooth is not on. Cannot check discoverability.");
		}
	}
	
	public void simulateMessage() {
		try {
			synchronized (lock) {
				while (!this.isConnected())
					lock.wait(100);
			}
		} catch (InterruptedException e) {
			System.out.println("Wait interrupted, " + e);
		}
		mm_communication_thread.write(new PPMessage(PPMessage.Command.STRING, "Test message from Windows\n"));
	}
	
	public boolean isConnected() {
		return mm_connected;	
	}
	
	
	public void end() {
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
                        synchronized (lock) {
                        	mm_communication_thread.start();
                        	System.out.println("Connected");
                        	mm_connected = true;
                        	lock.notify();
                        }
                        sleep(100);
                    } else {
                        System.out.println("No connection created");
                    }

                } catch (Exception e) {
                	if (e.getClass() == InterruptedIOException.class) {
                		System.out.println("Warning in ConnectThread: acceptAndOpen was interrupted while waiting for connection");
                	} else if (e.getClass() == IOException.class) {
                		System.out.println("Connection lost");
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
                		if (numBytes == -1) {
                			//Connection broke so close socket
                			this.cancel();
                			return;
                		}
                		//System.out.println("Only read " + numBytes + ", not " + PPMessage.MESSAGE_SIZE);
                	}
                	
                	//Get message from buffer
                    byte what = buffer[0];
                    //Got null message. Discard and continue
                    if (what == PPMessage.Command.NULL) continue;
                    
                    String text = new String(buffer, 1, PPMessage.MESSAGE_SIZE-1, StandardCharsets.UTF_8);

                    text = text.trim();
                	
                    System.out.println("Got: " + PPMessage.toString(what) + text);
                    
                    
                    PPMessage m = new PPMessage(what, text);
                    //TODO Do something with message here
                    if (m.what == PPMessage.Command.KEY_PRESS) {
                    	MouseRobot.powerPoint(text);
                    }
                    
                    //If message is notification to terminate, do so
                    if (m.what == PPMessage.Command.END) {
                    	end();
                    }
	                    
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

        public void write(PPMessage message) {
            try {
            	
            	byte[] b = new byte[PPMessage.MESSAGE_SIZE];
            	
            	//Convert message to byte[]
            	//Message type is first byte
                b[0] = message.what;
                //Rest of buffer is message text
                byte[] text = message.text.getBytes(StandardCharsets.UTF_8);
                if (text.length > PPMessage.MESSAGE_SIZE-1) {
                	//Throw exception if text is too long
                    throw new IllegalArgumentException();
                }
                System.arraycopy(text, 0, b, 1, text.length);
                
                mm_output_stream.write(b);
                mm_output_stream.flush();
                System.out.println("Sent: " + PPMessage.toString(message.what) + message.text);

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
