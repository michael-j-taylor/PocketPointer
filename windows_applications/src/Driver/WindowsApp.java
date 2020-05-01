package Driver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WindowsApp extends JFrame {
    private JButton connect;
    private JPanel conningPanel;
    private JMenuBar bar;
    private JMenu menu;
    private JMenuItem connedNavi;
    private JMenuItem conningNavi;
    private JMenuItem exitWindow;

    public WindowsApp() {
        add(conningPanel);

        setTitle("PocketPointer Receiver");
        setSize(600, 450);
        setResizable(false);
        getContentPane().setBackground(Color.DARK_GRAY.darker());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        exitWindow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
