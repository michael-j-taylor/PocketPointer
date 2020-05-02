package Bluetooth;

import java.awt.AWTException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.bluetooth.BluetoothStateException;

import Driver.MouseRobot;

class CommunicationThread extends Thread {

    private InputStream mm_input_stream;
    private OutputStream mm_output_stream;
    private boolean mm_running = true;
    private BluetoothServer mm_server;
    private ConnectThread mm_connection;

    public CommunicationThread(BluetoothServer server, ConnectThread connection) {
        super();
        mm_connection = connection;
        mm_server = server;
        
        try {
			mm_input_stream = mm_connection.getStream().openInputStream();
			mm_output_stream = mm_connection.getStream().openOutputStream();
		} catch (IOException e) {
			System.out.println("Failed to open streams");
			e.printStackTrace();
		}
    }

    //Watch for messages from buffer and read them
    @Override
    public void run() {
    	System.out.println("Start communication thread");
        try {

            while (mm_running) {
            	byte[] buffer = new byte[PPMessage.MESSAGE_SIZE];
            	int numBytes;

            	//Read from InputStream
            	while ( (numBytes = mm_input_stream.read(buffer)) <= PPMessage.MESSAGE_SIZE) {
            		if (numBytes == -1) {
            			//Connection broke so close socket
            			end();
            			return;
            		} else if (numBytes == 0) break;
            		//System.out.println("Only read " + numBytes + ", not " + PPMessage.MESSAGE_SIZE);
                	readMessage(buffer);
            	}
                    
            }
        } catch (IOException ignored) {
        	
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failure in communication thread:\n" + e + "\n");
            mm_server.end();
        }
        
    	System.out.println("Stop communication thread");
    }
    
    private void readMessage(byte[] buffer) {
    	//Get message from buffer
        byte what = buffer[0];
        //Got null message. Discard and return
        if (what == PPMessage.Command.NULL) return;
        
        String text = new String(buffer, 1, PPMessage.MESSAGE_SIZE-1, StandardCharsets.UTF_8);
        text = text.trim();
    	
        System.out.println("Got: " + PPMessage.toString(what) + text);
        executeMessage(new PPMessage(what, text));        
    }
    
    private void executeMessage(PPMessage m) {
        if (m.what == PPMessage.Command.KEY_PRESS) {
        	//If message is a key press
        	try {
				MouseRobot.powerPoint(m.text);
			} catch (AWTException e) {
				System.out.println("Failed to execute command");
			}
        	
        } else if (m.what == PPMessage.Command.BUTTON) {
        	//If message is a button press
        	try {
        		MouseRobot.buttonPress(m.text);
        	} catch (AWTException e) {
				System.out.println("Failed to execute command");
			}
        	
        } else if (m.what == PPMessage.Command.SCROLL) {
        	//If message is Scrolling
        	try {
        		double [] coords = new double[2];
        		coords = m.getDoubles();
        		MouseRobot.scroll(coords[1]);
        	} catch (AWTException e) {
        		System.out.println("Failed to execute command");
        	}
        	
        } else if (m.what == PPMessage.Command.MOUSE_COORDS) {
        	//If message is Mouse Coordinates
        	double[] coords = new double[2];
        	coords = m.getDoubles();
        	try {
        		MouseRobot.mouseMovement(coords[0], coords[1]);
        	} catch (AWTException e) {
				System.out.println("Failed to execute command");
			}
        	
        } else if (m.what == PPMessage.Command.END) {
        	//If message is notification to terminate, do so
        	mm_server.end();
        	try {
				mm_server.restartServer();
			} catch (BluetoothStateException ignored) {
			}
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

        } catch (IOException ignroed) {
        }
    }

    public boolean isRunning() {
        return mm_running;
    }

    public void setRunning(boolean running) {
        mm_running = running;
    }
    
    public void end() {
        mm_running = false;
        
        try {
            mm_connection.getStream().close();
            mm_input_stream.close();
            mm_output_stream.close();
        } catch (IOException ignored) {
        }
    }
}
