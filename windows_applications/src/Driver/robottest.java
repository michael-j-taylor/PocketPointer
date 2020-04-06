package Driver;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent; 
import java.io.*;

public class robottest {

    public static void main(String[] args) throws IOException, AWTException, InterruptedException {

        String command = "notepad.exe"; 
        Runtime run = Runtime.getRuntime(); 
        run.exec(command); 
        try { 
            Thread.sleep(2000); //wait 2s for notepad to open, could probably be reduced on faster computers
        } 
        catch (InterruptedException e) 
        { 
            e.printStackTrace(); 
        } 
  
        // Create an instance of Robot class 
        Robot robot = new Robot(); 
  
        // Press keys using robot. A gap of 
        robot.keyPress(KeyEvent.VK_F);
    }
}
