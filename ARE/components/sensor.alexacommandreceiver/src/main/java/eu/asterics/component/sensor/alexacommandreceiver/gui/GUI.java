package eu.asterics.component.sensor.alexacommandreceiver.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * A simple gui for debug purposes. Can be deactivated with a property.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 *
 */
public class GUI extends JPanel {

    private static final long serialVersionUID = -4832269915523104986L;

    private JLabel deviceType;
    private JLabel commandData;
    private JLabel errorData;
    private JLabel lastRequestTimeStamp;

    /**
     * The GUI's contructor.
     * 
     * @param space the desired space
     */
    public GUI(Dimension space) {
        setPreferredSize(space);
        design();
        setVisible(true);
    }

    /**
     * Sets the given data into the gui elements.
     * 
     * @param deviceTypeMessage  the text for the deviceType label
     * @param commandDataMessage the text for the commandData label
     * @param errorDataMessage   the text for the errorMessage label
     */
    public void setMessage(String deviceTypeMessage, String commandDataMessage, String errorDataMessage) {
        deviceType.setText(deviceTypeMessage);
        commandData.setText(commandDataMessage);
        errorData.setText(errorDataMessage);
        lastRequestTimeStamp.setText(new Date().toString());
    }

    private void design() {
        JPanel root = new JPanel(new BorderLayout());

        root.add(new JLabel("Asterics Alexa Command Receiver - Debug View"), BorderLayout.NORTH);
        root.add(new JSeparator());

        Box deviceTypeLine = Box.createHorizontalBox();
        deviceTypeLine.setAlignmentX(0);
        deviceType = new JLabel();
        deviceTypeLine.add(new JLabel("device type: "));
        deviceTypeLine.add(deviceType);

        Box commandDataLine = Box.createHorizontalBox();
        commandDataLine.setAlignmentX(0);
        commandData = new JLabel();
        commandDataLine.add(new JLabel("command data: "));
        commandDataLine.add(commandData);

        Box errorDataLine = Box.createHorizontalBox();
        errorDataLine.setAlignmentX(0);
        errorData = new JLabel();
        errorDataLine.add(new JLabel("error data: "));
        errorDataLine.add(errorData);

        Box timeLine = Box.createHorizontalBox();
        timeLine.setAlignmentX(0);
        lastRequestTimeStamp = new JLabel("no request received yet");
        timeLine.add(new JLabel("last received request: "));
        timeLine.add(lastRequestTimeStamp);

        Box centerBox = Box.createVerticalBox();
        centerBox.add(new JSeparator());
        centerBox.add(deviceTypeLine);
        centerBox.add(new JSeparator());
        centerBox.add(commandDataLine);
        centerBox.add(new JSeparator());
        centerBox.add(errorDataLine);
        centerBox.add(new JSeparator());
        centerBox.add(timeLine);

        root.add(centerBox, BorderLayout.CENTER);
        root.add(new JLabel("\u00a9 F4L 2021"), BorderLayout.SOUTH);
        add(root);
    }
}
