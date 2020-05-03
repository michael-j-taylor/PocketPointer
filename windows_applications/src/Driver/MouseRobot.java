package Driver;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class MouseRobot {

    public static void main(String[] args) throws IOException, AWTException, InterruptedException {

    	//This is testing data for the mouseRobot class
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

    //Powerpoint mode that takes a button press and gives the corrisponding button press in powerpoint mode
    public static void powerPoint(String buttonPress) throws AWTException {
    	Robot robot = new Robot();
    	
    	if (buttonPress.equals("RIGHT")) {
    		
    		//This is the right press button
			robot.keyPress(KeyEvent.VK_RIGHT);
			robot.keyRelease(KeyEvent.VK_RIGHT);
    	}
    	else if (buttonPress.equals("LEFT")) {
    		//This is left press button
    		robot.keyPress(KeyEvent.VK_LEFT);
    		robot.keyRelease(KeyEvent.VK_LEFT);
    	}
    	else if (buttonPress.equals("B")) {
    		//This is for the blank screen
    		robot.keyPress(KeyEvent.VK_B);
    		robot.keyRelease(KeyEvent.VK_B);
    	}

    }

    //This is the method for mouse movement that takes input from the mouse and tells the computer where to move the mouse
    public static void mouseMovement(double x, double y) throws AWTException {
    	Robot robot = new Robot();
    	//gets mouses current location
    	Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

    	//then gets the x and y location for the mouse
    	double mouseX = mouseLocation.getX();
    	double mouseY = mouseLocation.getY();
    	//then create the new x and y coordinates
    	int newX, newY;
    	newX = (int) Math.round(mouseX + x);
    	newY = (int) Math.round(mouseY + y);
    	//tell robot to move the mouse to the new coordinates
    	robot.mouseMove(newX, newY);
    	
    }

    //This takes a mouse button press and tells the computer what button is press for input on the computer
    public static void buttonPress(String buttonPress) throws AWTException{
    	Robot robot = new Robot();

    	//This is if the left mouse button is clicked
    	if (buttonPress.equals("mleft")) {
    		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    		System.out.println("Left");
    	}

    	//This is if the middle button is clicked
    	else if (buttonPress.equals("mmiddle")) {
    		robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
    		robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
    		System.out.println("Middle");
    	}

    	//This is if the right button is clicked
    	else if (buttonPress.equals("mright")) {
    		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
    		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    		System.out.println("Right");
    	}
    	
    }

    //This a method that allows the computer to take a scroll wheel movement
    public static void scroll(double wheelAmt) throws AWTException{
    	Robot robot = new Robot();
    	//takes the input and moves the amount for the scroll wheel
    	robot.mouseWheel((int) Math.round(wheelAmt));
    	
    }

    //this is a method for double tap input for specific applications
    public static void doubleTap() throws AWTException{
		Robot robot = new Robot();
		//presses space for double tap
		robot.keyPress(KeyEvent.VK_SPACE);
		robot.keyRelease(KeyEvent.VK_SPACE);
	}

	//this method for swipe input for specific applications, currently for tab switches in Google Chrome
	public static void swipe(String swipeInput) throws AWTException{
		Robot robot = new Robot();

		//checks if the input is a swipe right
		if (swipeInput.equals("RIGHT")){
			//these press the control + page down button for tab right
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_PAGE_DOWN);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
			System.out.println("Swipe right");
		}

		//checks if the input is a swipe left
		else if (swipeInput.equals("LEFT")){
			//these press the control + page up button for tab left
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_PAGE_UP);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_PAGE_UP);
			System.out.println("Swipe left");
		}

		//checks if the input is a swipe up
		else if (swipeInput.equals("UP")){
			//these press the page up button
			robot.keyPress(KeyEvent.VK_PAGE_UP);
			robot.keyRelease(KeyEvent.VK_PAGE_UP);
		}

		//checks if the input is a swipe down
		else if(swipeInput.equals("Down")){
			//these press the page down button
			robot.keyPress(KeyEvent.VK_PAGE_DOWN);
			robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
		}

    }
}
