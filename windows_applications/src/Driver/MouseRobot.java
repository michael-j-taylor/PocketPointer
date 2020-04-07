package Driver;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent; 
import java.io.*;

public class MouseRobot {

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
    
    public static void powerPoint(int buttonPress) throws AWTException {
    	Robot robot = new Robot();
    	
    	if (buttonPress == 1) {
    		//This is the right press button
			robot.keyPress(KeyEvent.VK_RIGHT);
    	}
    	else if (buttonPress == 2) {
    		//This is left press button
    		robot.keyPress(KeyEvent.VK_LEFT);
    	}
    	else if (buttonPress == 3) {
    		//This is for the blank screen
    		robot.keyPress(KeyEvent.VK_B);
    	}	
    }
    
}
