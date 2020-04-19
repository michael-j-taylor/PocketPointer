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
  
    	System.out.println("Running");
        // Create an instance of Robot class 
        Robot robot = new Robot(); 
  
        // Press keys using robot. A gap of 
        
        powerPoint("B");
        powerPoint("B");
        powerPoint("B");
        powerPoint("B");
        powerPoint("B");
        powerPoint("B");
        
        powerPoint("LEFT");
        powerPoint("RIGHT");
        powerPoint("B");
        
        System.out.println("Finished");
    }
    
    public static void powerPoint(String buttonPress) throws AWTException {
    	Robot robot = new Robot();
    	
    	if (buttonPress.equals("RIGHT")) {
    		
    		//This is the right press button
			robot.keyPress(KeyEvent.VK_RIGHT);
    	}
    	else if (buttonPress.equals("LEFT")) {
    		//This is left press button
    		robot.keyPress(KeyEvent.VK_LEFT);
    	}
    	else if (buttonPress.equals("B")) {
    		//This is for the blank screen
    		robot.keyPress(KeyEvent.VK_B);
    	}
    	
    	return;
    }
    
}