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
            Thread.sleep(2000); 
        } 
        catch (InterruptedException e) 
        { 
            e.printStackTrace(); 
        } 
  
        // Create an instance of Robot class 
        Robot robot = new Robot(); 
  
        // Press keys using robot. A gap of 
        // of 500 mili seconds is added after 
        // every key press 
        robot.keyPress(KeyEvent.VK_F);
    }
}
