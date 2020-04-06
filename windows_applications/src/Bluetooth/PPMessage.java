package Bluetooth;

public class PPMessage {
	Commands what;
	String text;
	
	public interface Commands {
		int MOUSE_COORDS = 0;
		int KEY_PRESS = 1;
	}
	
	public PPMessage(Commands what, String text) {
        this.what = what;
        this.text = text;
    }
}
