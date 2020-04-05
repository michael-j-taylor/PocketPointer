/******************************
 * Fortnite Challenge		  *
 * Ryan Atkinson			  *
 ******************************/
 
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

public class Driver
{    
    public static void main(String[] args) 
    {
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	AppWindow window = new AppWindow(screenSize);
    	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	window.setSize((int)(screenSize.getWidth()*.8) ,(int)(screenSize.getHeight()*.8));
    	window.setVisible(true);
    }
}

class AppWindow extends JFrame
{
	private Container contentPane, introPane;
	private JPanel menuPanel, settingsPanel, mapPanel;
	private JButton getChallenge, toggleBus, generateChallenge, firstWeaponForgiveness;
	private JComboBox selectRand, selectDifficulty, distanceFromBus;
	private JTextArea challenge;
	private JLabel mapComp;
	private GraphicsEnvironment env;
	private Rectangle bounds;
	private ChallengeGetter easy, medium, hard, xHard, all;
	
	final private String[] difficulty = {"Just a \"Warm-up\"", "A Good Challenge", "A REAL Challenge", "You're Probs Gonna Die", "Random Difficulty", "Custom Difficulty"};
	final private String[] randLocation = {"Named and Un-named Locations","Named Locations", "Un-named Locations", "Ramdom Sector", "Random Coordinates"};
	final private String[] distance = {"<= One Sector", "One Sector", "Two Sectors", "Three Sectors", "Four Sectors", "IDC Sectors"};
	private boolean bus = false, forgive = true;
	private int randIndex = 0, diffIndex = 0, busDIndex = 0, mapX, mapY;
	Random rand;
	
	public AppWindow(Dimension screenSize)
	{
		super("Fortnite Challenge");
		
		env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		bounds = env.getMaximumWindowBounds();
		try { setIconImage(ImageIO.read(new File("fc.png"))); }
		catch (Exception e){ System.out.println(e); }
		BufferedImage map = null;
		try { map = ImageIO.read(new File("map.jpg")); }
		catch (Exception e) {System.out.println(e); }
		
		rand = new Random();
		
		//menuPanel = new JPanel();
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.setPreferredSize(new Dimension((int)(screenSize.getWidth()*.2), (int)(screenSize.getHeight()*.8)));
		//settingsPanel.setMaximumSize(new Dimension((int)(screenSize.getWidth()*.2), (int)(screenSize.getHeight())));
		mapPanel = new JPanel();
		contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		//JButtons
		getChallenge = new JButton("Get a Challenge");
		toggleBus = new JButton("Bus Path Determines Random Location: False");
		generateChallenge = new JButton("Generate a Challenge");
		firstWeaponForgiveness = new JButton("First Weapon Forgiveness: Yes");
		//JComboBoxes
		selectRand = new JComboBox(randLocation);
		selectDifficulty = new JComboBox(difficulty);
		distanceFromBus = new JComboBox(distance);
		//Text Field
		challenge = new JTextArea(new String());
		challenge.setLineWrap(true);
		challenge.setWrapStyleWord(true);
		challenge.setPreferredSize(new Dimension((int)(screenSize.getWidth()*.18),75));
		//challenge.setMaximumSize(new Dimension(75,200));
		
		//Map
		Image newMap = map.getScaledInstance((int)(screenSize.getHeight()*.74),(int)(screenSize.getHeight()*.74), Image.SCALE_SMOOTH);
		mapComp = new JLabel(new ImageIcon(newMap));
		
		//Settings panel alignment
		getChallenge.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleBus.setAlignmentX(Component.CENTER_ALIGNMENT);
		generateChallenge.setAlignmentX(Component.CENTER_ALIGNMENT);
		firstWeaponForgiveness.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectRand.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectDifficulty.setAlignmentX(Component.CENTER_ALIGNMENT);
		distanceFromBus.setAlignmentX(Component.CENTER_ALIGNMENT);
		challenge.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//Creating Challenge Objects
		easy = new ChallengeGetter("easy_challenges.dat");
		medium = new ChallengeGetter("medium_challenges.dat");
		hard = new ChallengeGetter("hard_challenges.dat");
		xHard = new ChallengeGetter("xtra_hard_challenges.dat");
		all = new ChallengeGetter("challenges.dat");
		
		getChallenge.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) //Get challenge button (objective. not overall challenge)
		{
			setObjective();
		
		}});
		
		toggleBus.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) //Bus path toggle button
		{
				if (bus)
				{
					bus = false;
					toggleBus.setText("Bus Path Determines Random Location: False");
				}
				else
				{
					bus = true;
					toggleBus.setText("Bus Path Determines Random Location: True");
				}
		}});
		
		firstWeaponForgiveness.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) //First weapon forgiveness toggle
		{
			if (forgive)
			{
				forgive = false;
				firstWeaponForgiveness.setText("First Weapon Forgiveness: No");
			}
			else
			{
				forgive = true;
				firstWeaponForgiveness.setText("First Weapon Forgiveness: Yes");
			}
		}});
		
		generateChallenge.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) //Generate a challenge based 
		{
				generateChallenge();
		}});
		
		selectRand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) //select
		{
				randIndex = selectRand.getSelectedIndex();
		}});
		
		selectDifficulty.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) //Option box for difficulty
		{
				diffIndex = selectDifficulty.getSelectedIndex();
				
				if (diffIndex < 5)
				{
					selectRand.setEnabled(false);
					distanceFromBus.setEnabled(false);
					getChallenge.setEnabled(false);
					firstWeaponForgiveness.setEnabled(false);
					toggleBus.setEnabled(false);
				}
				else
				{
					selectRand.setEnabled(true);
					distanceFromBus.setEnabled(true);
					getChallenge.setEnabled(true);
					firstWeaponForgiveness.setEnabled(true);
					toggleBus.setEnabled(true);
				}
		}});
		
		distanceFromBus.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) //ComboBox for distance from bus
		{
				busDIndex = distanceFromBus.getSelectedIndex();
		}});
		
		settingsPanel.add(selectDifficulty);
		settingsPanel.add(generateChallenge);
		settingsPanel.add(selectRand);
		settingsPanel.add(toggleBus);
		settingsPanel.add(distanceFromBus);
		settingsPanel.add(getChallenge);
		settingsPanel.add(challenge);
		settingsPanel.add(firstWeaponForgiveness);
		mapPanel.add(mapComp);
		
		contentPane.add(mapPanel, BoxLayout.X_AXIS);
		contentPane.add(settingsPanel, BoxLayout.X_AXIS);
		
		selectRand.setEnabled(false);
		distanceFromBus.setEnabled(false);
		getChallenge.setEnabled(false);
		firstWeaponForgiveness.setEnabled(false);
		toggleBus.setEnabled(false);
		
		mapX = mapComp.getX();
		mapY = mapComp.getY();
		
		//Image mapR = map.getScaledInstance(mapComp.getWidth(), mapComp.getHeight(), Image.SCALE_SMOOTH);
		//mapComp.setIcon(new ImageIcon(mapR));	
	}
	
	public void generateChallenge()
	{
		Random rand = new Random();
		int locationType = rand.nextInt(2);
		int diff = diffIndex;
		if (diff == 4)
		{
			diff = rand.nextInt(3);
		}
	}
	
	public void setObjective()
	{
		switch(diffIndex)
		{
			case 0:
				challenge.setText(easy.challenges.get((rand.nextInt(easy.challenges.size()-1))));
				break;
			case 1:
				challenge.setText(medium.challenges.get((rand.nextInt(medium.challenges.size()-1))));
				break;
			case 2:
				challenge.setText(hard.challenges.get((rand.nextInt(hard.challenges.size()-1))));
				break;
			case 3:
				challenge.setText(xHard.challenges.get((rand.nextInt(xHard.challenges.size()-1))));
				break;
			case 4: //case 4 and 5 are the same
			case 5:
				challenge.setText(all.challenges.get((rand.nextInt(all.challenges.size()-1))));
				break;
		}	
	}
	
	public ArrayList<String> getNamedLocals()
	{
		return LocationGetter.named();
	}
	
	public ArrayList<String> getUnnamedLocals()
	{
		return LocationGetter.unnamed();
	}
	
	public void update(Graphics g)
	{
		env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		bounds = env.getMaximumWindowBounds();
		paint(g);
	}
}
