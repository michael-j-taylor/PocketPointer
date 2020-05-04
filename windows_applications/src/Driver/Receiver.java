package Driver;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.BluetoothStateException;
import javax.swing.JPanel;

import Bluetooth.BluetoothServer;


public class Receiver extends JPanel
{
    static GraphicsConfiguration gc;

    public static void main(String args[])
    {
    	WindowsApp window = new WindowsApp();
    	window.setVisible(true);

        BtDevices alejandro = new BtDevices("Alejandro's Device","123456789");
        BtDevices michael = new BtDevices("Michael's Device","234567891");
        BtDevices terran = new BtDevices("Terran's Device","345678912");
        BtDevices ryan = new BtDevices("Ryan's Device","456789123");
        BtDevices ben = new BtDevices("Ben's Device","567891234");

        window.addBtDevice(ben,1);
        window.addBtDevice(michael, 2);
        window.addBtDevice(alejandro, 3);
        window.addBtDevice(ryan, 4);
        window.addBtDevice(terran, 5);


        /*BluetoothServer server = new BluetoothServer();
    	try {
    		server.openServer();
    	} catch (Exception e) {
            if (e instanceof BluetoothStateException) {
                System.out.println("In receiver, failed to use Bluetooth");
            } else
                System.out.println("Exception from openServer:\n" + e + e.getMessage() + "\n");
    	}*/
        
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


}


