package Driver;

import Bluetooth.BluetoothServer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.bluetooth.BluetoothStateException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class WindowsApp extends JFrame {
    private JPanel mainPanel;
    private JButton connectDeviceButton;
    private JList deviceList;
    private JButton connectNewDeviceButton;
    private JButton updateButton;
    private JButton saveNewButton;
    private JButton deleteButton;

    public ArrayList<BtDevices> getBtDevicesArrayList() {
        return btDevicesArrayList;
    }

    private ArrayList<BtDevices> btDevicesArrayList;
    private DefaultListModel listModel;
    private BluetoothServer server;


    public JLabel connectingOutput;
    public JTextField devNameField;
    public JTextField devPriorityField;
    public JLabel devBtIdField;
    public JButton disconnectDeviceButton;
    private JButton stopConnectingButton;

    public WindowsApp() {
        super("PocketPointer Receiver");
        WindowsApp window = this;

        setSize(850, 400);
        setResizable(false);
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
            }

            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(null, "Are You Sure to Close Application?", "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                    if (server != null) {
                        server.end(true);
                        System.exit(0);
                    } else {
                        System.exit(0);
                    }

                }
            }
        };

        addWindowListener(exitListener);
        getContentPane().add(mainPanel);

        btDevicesArrayList = new ArrayList<BtDevices>();
        listModel = new DefaultListModel();
        deviceList.setModel(listModel);

        deviceList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int deviceNum = deviceList.getSelectedIndex();
                if (deviceNum >= 0) {
                    BtDevices dev = btDevicesArrayList.get(deviceNum);
                    devNameField.setText(dev.getDevName());
                    devPriorityField.setText(String.valueOf(deviceNum + 1));
                    devBtIdField.setText(dev.getDevBtId());

                    updateButton.setVisible(true);
                    deleteButton.setVisible(true);
                } else {
                    updateButton.setVisible(false);
                    deleteButton.setVisible(false);
                }
            }
        });

        connectDeviceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                server = new BluetoothServer(window);
                try {
                    server.openServer();
                    connectingOutput.setText("Waiting for connection...");
                } catch (Exception e) {
                    if (e instanceof BluetoothStateException) {
                        System.out.println("In receiver, failed to use Bluetooth");
                    } else
                        System.out.println("Exception from openServer:\n" + e + e.getMessage() + "\n");
                }

            }
        });

        disconnectDeviceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.end(true);
            }
        });

        saveNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BtDevices dev = new BtDevices(devNameField.getText(), devBtIdField.getText());
                btDevicesArrayList.add(dev);
                try {
                    int current = btDevicesArrayList.size() - 1;
                    int target = Integer.parseInt(devPriorityField.getText());
                    btDevicesArrayList = orderList(btDevicesArrayList, current, target);
                } catch (NumberFormatException exception) {
                    System.out.println("Exception: " + exception);
                }

                refreshDeviceList();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int deviceNum = deviceList.getSelectedIndex();
                BtDevices dev = btDevicesArrayList.get(deviceNum);
                dev.setDevName(devNameField.getText());
                dev.setDevBtId(devBtIdField.getText());

                try {
                    int target = Integer.parseInt(devPriorityField.getText());
                    btDevicesArrayList = orderList(btDevicesArrayList, deviceNum, target);
                } catch (NumberFormatException exception) {
                    System.out.println("Exception: " + exception);
                }

                refreshDeviceList();

            }
        });

    }

    public void addBtDevice(BtDevices dev, int position) {
        int i;

        btDevicesArrayList.add(dev);
        i = btDevicesArrayList.size();

        btDevicesArrayList = orderList(btDevicesArrayList, i, position);

        refreshDeviceList();
    }

    public ArrayList<BtDevices> orderList(ArrayList firstList, int initial, int destination) {
        ArrayList<BtDevices> updatedList = new ArrayList();
        System.out.println("initial index: " + initial + " target index: " + destination);

        if (initial == 0 && initial < destination) {
            if (destination == firstList.size() - 1) {
                updatedList = (ArrayList<BtDevices>) firstList.subList(1, destination);
                updatedList.add((BtDevices) firstList.get(initial));

                //System.out.println("tried");

                return updatedList;
            } else if (destination < firstList.size() - 1) {
                //algo 2

                return updatedList;
            } else {
                for (int k = 0; k < firstList.size(); k++) {
                    System.out.println("firstList element: " + k);
                    System.out.println("device in that element: " + firstList.get(k));
                }
                return firstList;
            }
        } else if (initial > 0 && initial < destination && initial < firstList.size() - 1) {
            if (destination == firstList.size() - 1) {
                //algo 3

                return updatedList;
            } else if (destination < firstList.size() - 1) {
                //algo 4

                return updatedList;
            } else {
                return firstList;
            }
        } else if (destination == 0 && destination < initial) {
            if (initial == firstList.size() - 1) {
                //algo 5

                return updatedList;
            } else if (initial < firstList.size() - 1) {
                //algo 6

                return updatedList;
            } else {
                return firstList;
            }
        } else if (destination > 0 && destination < initial && destination < firstList.size() - 1) {
            if (destination == firstList.size() - 1) {
                //algo 7

                return updatedList;
            } else if (destination < firstList.size() - 1) {
                //algo 8

                return updatedList;
            } else {
                return firstList;
            }
        } else {
            return firstList;
        }

    }

    public void refreshDeviceList() {
        listModel.removeAllElements();
        for (BtDevices dev : btDevicesArrayList) {
            listModel.addElement(dev.getDevName());
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setBackground(new Color(-14737633));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-14737633));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setBackground(new Color(-14737633));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setBackground(new Color(-14737633));
        panel2.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.setBackground(new Color(-14737633));
        panel2.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setBackground(new Color(-14737633));
        panel2.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        connectingOutput = new JLabel();
        connectingOutput.setEnabled(true);
        connectingOutput.setForeground(new Color(-1644826));
        connectingOutput.setText("Connecting...");
        panel5.add(connectingOutput, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setBackground(new Color(-14737633));
        panel6.setEnabled(false);
        panel1.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        connectDeviceButton = new JButton();
        connectDeviceButton.setBackground(new Color(-13421000));
        connectDeviceButton.setEnabled(true);
        connectDeviceButton.setForeground(new Color(-10174465));
        connectDeviceButton.setHideActionText(false);
        connectDeviceButton.setText("Connect Device");
        panel6.add(connectDeviceButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setForeground(new Color(-1644826));
        label1.setText("Connect to Android Application via Bluetooth");
        panel6.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.setBackground(new Color(-14737633));
        panel7.setEnabled(false);
        panel6.add(panel7, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        stopConnectingButton = new JButton();
        stopConnectingButton.setBackground(new Color(-13421000));
        stopConnectingButton.setForeground(new Color(-10174465));
        stopConnectingButton.setHideActionText(false);
        stopConnectingButton.setText("Stop Connecting");
        stopConnectingButton.setVisible(false);
        panel7.add(stopConnectingButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel7.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        disconnectDeviceButton = new JButton();
        disconnectDeviceButton.setActionCommand("");
        disconnectDeviceButton.setBackground(new Color(-13421000));
        disconnectDeviceButton.setForeground(new Color(-10174465));
        disconnectDeviceButton.setText("Disconnect Device");
        disconnectDeviceButton.setVisible(false);
        panel6.add(disconnectDeviceButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(4, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel8.setBackground(new Color(-14737633));
        panel8.setEnabled(false);
        mainPanel.add(panel8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel9.setBackground(new Color(-14737633));
        panel8.add(panel9, new GridConstraints(2, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setForeground(new Color(-1644826));
        label2.setText("Device Name");
        panel9.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setForeground(new Color(-1644826));
        label3.setText("Priority Number");
        panel9.add(label3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setForeground(new Color(-1644826));
        label4.setText("Bluetooth ID");
        panel9.add(label4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        devNameField = new JTextField();
        devNameField.setBackground(new Color(-13421000));
        devNameField.setForeground(new Color(-1644826));
        devNameField.setSelectedTextColor(new Color(-1));
        devNameField.setSelectionColor(new Color(-11967840));
        devNameField.setText("");
        panel9.add(devNameField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        devPriorityField = new JTextField();
        devPriorityField.setBackground(new Color(-13421000));
        devPriorityField.setForeground(new Color(-1644826));
        devPriorityField.setSelectedTextColor(new Color(-1));
        devPriorityField.setSelectionColor(new Color(-11967840));
        panel9.add(devPriorityField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel9.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        devBtIdField = new JLabel();
        devBtIdField.setForeground(new Color(-1644826));
        devBtIdField.setText("Exampleid");
        panel9.add(devBtIdField, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.setBackground(new Color(-14737633));
        panel10.setEnabled(false);
        panel8.add(panel10, new GridConstraints(0, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel11.setBackground(new Color(-14737633));
        panel11.setForeground(new Color(-1644826));
        panel8.add(panel11, new GridConstraints(1, 3, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
        panel12.setBackground(new Color(-14737633));
        panel8.add(panel12, new GridConstraints(3, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        updateButton = new JButton();
        updateButton.setBackground(new Color(-13421000));
        updateButton.setForeground(new Color(-10174465));
        updateButton.setText("Update Device");
        updateButton.setVisible(false);
        panel12.add(updateButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel12.add(spacer4, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel12.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        saveNewButton = new JButton();
        saveNewButton.setBackground(new Color(-13421000));
        saveNewButton.setForeground(new Color(-10174465));
        saveNewButton.setText("Save New");
        panel12.add(saveNewButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setBackground(new Color(-13421000));
        deleteButton.setForeground(new Color(-10174465));
        deleteButton.setText("Delete");
        deleteButton.setVisible(false);
        panel12.add(deleteButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel12.add(spacer6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel12.add(spacer7, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel13, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel14.setBackground(new Color(-13421000));
        panel14.setEnabled(true);
        panel13.add(panel14, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setForeground(new Color(-1644826));
        label5.setText("List of Devices");
        panel14.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel15.setBackground(new Color(-13421000));
        panel14.add(panel15, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        deviceList = new JList();
        deviceList.setBackground(new Color(-10591901));
        deviceList.setForeground(new Color(-4013374));
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        defaultListModel1.addElement("Ben's Phone");
        defaultListModel1.addElement("Alejandro's Phone");
        defaultListModel1.addElement("Michael's Phone");
        deviceList.setModel(defaultListModel1);
        deviceList.setSelectionBackground(new Color(-11967840));
        deviceList.setSelectionForeground(new Color(-1));
        deviceList.setSelectionMode(0);
        panel14.add(deviceList, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel16.setBackground(new Color(-14737633));
        panel8.add(panel16, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
