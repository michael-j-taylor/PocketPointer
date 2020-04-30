package Driver;

import javax.bluetooth.BluetoothStateException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Bluetooth.BluetoothServer;
import Bluetooth.PPMessage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.concurrent.TimeoutException;


public class Receiver extends JPanel
{
    static GraphicsConfiguration gc;

    public static void main(String args[])
    {
    	
    	JFrame frame = new JFrame(gc);
        frameSetUp(frame);
    	
    	BluetoothServer server = new BluetoothServer();
    	try {
    		server.openServer();
    		server.simulateMessage();
    	} catch (Exception e) {
    		if (e.getClass() == TimeoutException.class) {
    			System.out.println("In receiver, Timed out");
    		} else if (e.getClass() == BluetoothStateException.class) {
    			System.out.println("In receiver, failed to use Bluetooth");
    		} else
    			System.out.println("Exception from openServer:\n" + e + e.getMessage() + "\n");
    	}
    	
    	
        
        //TODO: initialize components. pass frame to method
//        robotTest();
        //while(true)
        //TODO: main sequence
    }

    public static void robotTest()
    {
        String command = "notepad.exe";
        Runtime run = Runtime.getRuntime();
        Robot robot = null;
        try {
            run.exec(command);
            Thread.sleep(2000);
            robot = new Robot();
            
            // Press keys using robot. A gap of
            // of 500 mili seconds is added after
            // every key press
            robot.keyPress(KeyEvent.VK_F);
            MouseRobot.powerPoint("B");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Create an instance of Robot class
       
    }
    
    public void paint(Graphics g) {
    	g.drawString("Hello world", 10, 10);
    }
    
    public static void frameSetUp(JFrame frame) {
    	    	
    	frame.setTitle("PocketPointer Receiver");
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setResizable(false);
        //frame.getContentPane().setBackground(Color.DARK_GRAY.darker());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    
        
    }
}