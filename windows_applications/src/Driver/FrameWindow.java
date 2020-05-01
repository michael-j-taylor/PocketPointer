package Driver;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class FrameWindow extends JFrame implements ActionListener{
	
	private static GraphicsConfiguration gc;
	private JFrame frame;
	private JPanel conningPanel, connedPanel;
	private JButton conningButton;
	private JLabel testLabel;
	private JMenuBar bar = new JMenuBar();
	private JMenu menu = new JMenu("Menu");
	private JMenuItem connedNavi = new JMenuItem("Connected Devices");
	private JMenuItem conningNavi = new JMenuItem("Connect to a Device");
	private JMenuItem exitWindow = new JMenuItem("Exit");
	
	public FrameWindow(String[] deviceList) {
		//Setting up the Frame
		frame = new JFrame(gc);
		frame.setTitle("PocketPointer Receiver");
        frame.setSize(600, 450);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.DARK_GRAY.darker());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        //Setting up the Menu Bar
        menu.add(conningNavi);
        menu.add(connedNavi);
        menu.addSeparator();
        menu.add(exitWindow);
        bar.add(menu);
        frame.setJMenuBar(bar);
//        frame.add(listDevices);
        
        //Setting up the Connect Devices Panel
        conningPanel = new JPanel();
		conningPanel.setBackground(Color.GREEN.darker());
        
        //Setting up the Connected Devices Panel
        connedPanel = new JPanel();
		connedPanel.setBackground(Color.BLUE.darker());
        
        exitWindow.addActionListener(this);
        conningNavi.addActionListener(this);
        connedNavi.addActionListener(this);
        
        frame.setVisible(true);
	}
	
	
	
	public void connedPanelSetup(JPanel panel2, JFrame frame) {
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals("Exit")) {
			System.exit(0);
		} else if (e.getActionCommand().equals("Connected Devices")) {
			frame.add(connedPanel);	
		} else if (e.getActionCommand().equals("Connect to a Device")) {
			frame.add(conningPanel);
		}
	}

}



