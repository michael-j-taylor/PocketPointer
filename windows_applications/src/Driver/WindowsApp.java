package Driver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WindowsApp extends JFrame {
    private JPanel mainPanel;
    private JButton connectExistingDeviceButton;
    private JList deviceList;
    private JLabel connectingOutput;
    private JButton connectNewDeviceButton;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;

    public WindowsApp() {
        super("PocketPointer Receiver");

        setSize(850, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().add(mainPanel);

        setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
