package Driver;

import Bluetooth.BluetoothServer;

import javax.bluetooth.BluetoothStateException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;


public class Receiver extends JPanel
{
    static GraphicsConfiguration gc;

    public static void main(String[] args)
    {

  	  /*JFrame frame = new JFrame(gc);
  	  WindowsApp window = new WindowsApp();
  	  frame.setContentPane(window.conningPanel);
  	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  	  frame.pack();
  	  frame.setVisible(true);*/
        //String deviceList[] = {"Alejandro's Device", "Ben's Device", "Ryan's Device", "Taren's Device"};
//        frameSetUp(frame, deviceList);
//    	FrameWindow frame = new FrameWindow(deviceList);
    	WindowsApp window = new WindowsApp();
    	
    	BluetoothServer server = new BluetoothServer();
    	try {
    		server.openServer();
    	} catch (Exception e) {
            if (e instanceof BluetoothStateException) {
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
    
    public static void frameSetUp(JFrame frame, String[] deviceList) {
    	
    	JMenuBar bar = new JMenuBar();
    	JMenu menu = new JMenu("Menu");
    	JMenuItem conDevNavi = new JMenuItem("Connected Devices");
    	JMenuItem conNavi = new JMenuItem("Connect to a Device");
    	JMenuItem exitWindow = new JMenuItem("Exit");
    	JList listDevices = new JList(deviceList);
    	
    	frame.setTitle("PocketPointer Receiver");
        frame.setSize(600, 450);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.DARK_GRAY.darker());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               
        menu.add(conNavi);
        menu.add(conDevNavi);
        menu.addSeparator();
        menu.add(exitWindow);
        bar.add(menu);
        frame.setJMenuBar(bar);
//        frame.add(listDevices);
        
        exitWindow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                System.out.println("Exit was pressed");
            	System.exit(0);
            }
        });
        
        frame.setVisible(true);
    }

	
}


