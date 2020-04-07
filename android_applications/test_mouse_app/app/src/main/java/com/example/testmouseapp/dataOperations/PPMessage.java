package com.example.testmouseapp.dataOperations;

public class PPMessage {
    public static final int MESSAGE_SIZE = 1024;

    Commands what;
    String text;

    public interface Commands {
        int END = -1;
        int MOUSE_COORDS = 0;
        int KEY_PRESS = 1;
    }

    public PPMessage(Commands what, String text) {
        this.what = what;
        this.text = text;
    }
}
