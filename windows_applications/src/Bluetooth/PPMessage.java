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
        byte STRING = 3;
    }

    public PPMessage(byte what, String text) throws IllegalArgumentException {
        this.what = what;
        if (what == Command.NULL) throw new IllegalArgumentException();

        //Ensure text ends in newline character
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
        } else if (what == Command.STRING) {
            return "STRING - ";
        }

        //If what not found
        return null;
    }

    public double[] getDoubles() {
        String[] parts = text.split(" ");
        if (parts[1].charAt(parts[1].length()) == '\n') {
            parts[1] = parts[1].substring(0, parts[1].length()-1);
        }
        double[] results = new double[2];
        results[0] = Double.parseDouble(parts[0]);
        results[1] = Double.parseDouble(parts[1]);

        return results;
    }
}