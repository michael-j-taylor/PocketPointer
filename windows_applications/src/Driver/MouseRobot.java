package Driver;

import java.awt.*;
import java.awt.event.InputEvent;
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
        //Robot robot = new Robot(); 
  
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
        mouseMovement(500, 0);
        buttonPress("mright");
        scroll(5);
        
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
    
    public static void mouseMovement(double x, double y) throws AWTException {
    	Robot robot = new Robot();
    	Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
    	
    	double mouseX = mouseLocation.getX();
    	double mouseY = mouseLocation.getY();
    	int newX, newY;
    	newX = (int) Math.round(mouseX + x);
    	newY = (int) Math.round(mouseY + y);
    	robot.mouseMove(newX, newY);
    	
    }
    
    public static void buttonPress(String buttonPress) throws AWTException{
    	Robot robot = new Robot();
    	
    	if (buttonPress.equals("mleft")) {
    		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    		System.out.println("Left");
    	}
    	
    	else if (buttonPress.equals("mmiddle")) {
    		robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
    		robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
    		System.out.println("Middle");
    	}
    	
    	else if (buttonPress.equals("mright")) {
    		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
    		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    		System.out.println("Right");
    	}
    	
    }
    
    public static void scroll(double wheelAmt) throws AWTException{
    	Robot robot = new Robot();
    	robot.mouseWheel((int) Math.round(wheelAmt));
    	
    }
    
}
