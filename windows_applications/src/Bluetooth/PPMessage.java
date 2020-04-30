package Bluetooth;

public class PPMessage {
    public static final int MESSAGE_SIZE = 1024;

    public byte what;
    public String text;

    public interface Command {
        byte END = -1;
        byte NULL = 0;
        byte MOUSE_COORDS = 1;
        byte KEY_PRESS = 2;
        byte SWIPE = 3;
        byte STRING = 4;
        byte BUTTON = 5;
        byte DOUBLETAP = 6;
        byte SCROLL = 7;
    }

    public interface Button {
        String MOUSE_RIGHT = "mright";
        String MOUSE_LEFT = "mleft";
        String MOUSE_MIDDLE = "mmiddle";
    }

    public PPMessage(byte what, String text) throws IllegalArgumentException {
        this.what = what;
        if (what == Command.NULL) throw new IllegalArgumentException();

        //Ensure text ends in newline character
        text = text.trim();
        if (text.charAt(text.length()-1) == '\n') this.text = text + "\n";
        else this.text = text;
    }

    public PPMessage(byte what, double x_coord, double y_coord) throws IllegalArgumentException {
        this.what = what;
        if (what == Command.NULL) throw new IllegalArgumentException();

        String x = String.valueOf(x_coord);
        String y = String.valueOf(y_coord);
        this.text = x + " " + y + "\n";
    }

    public static String toString(byte what) {
        if (what == Command.END) {
            return "END - ";
        } else if (what == Command.KEY_PRESS) {
            return "KEY - ";
        } else if (what == Command.MOUSE_COORDS) {
            return "COORDS - ";
        } else if (what == Command.SWIPE) {
            return "SWIPE - ";
        } else if (what == Command.STRING) {
            return "STRING - ";
        } else if (what == Command.BUTTON) {
            return "MOUSE BUTTON - ";
        } else if (what == Command.DOUBLETAP) {
            return "DOUBLETAP - ";
        } else if (what == Command.SCROLL) {
            return "SCROLL - ";
        }

        //If what not found
        return null;
    }

    public double[] getDoubles() {
        String[] parts = text.split(" ");
        if (parts[1].charAt(parts[1].length()-1) == '\n') {
            parts[1] = parts[1].substring(0, parts[1].length()-1);
        }
        double[] results = new double[2];
        results[0] = Double.parseDouble(parts[0]);
        results[1] = Double.parseDouble(parts[1]);

        return results;
    }
}