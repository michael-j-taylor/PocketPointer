package Driver;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Bluetooth.BluetoothServer;

//import javax.bluetooth.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;


public class Receiver extends JPanel
{
    static GraphicsConfiguration gc;

    public static void main(String args[])
    {
    	BluetoothServer server = new BluetoothServer();
    	server.openServer();
    	
//        JFrame frame = new JFrame(gc);
//        frame.setTitle("PocketPointer Receiver");
//        frame.setSize(800, 600);
//        frame.setVisible(true);
//        frame.setResizable(false);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        //TODO: initialize components. pass frame to method
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


        // Press keys using robot. A gap of
        // of 500 mili seconds is added after
        // every key press
        robot.keyPress(KeyEvent.VK_F);
    }
}