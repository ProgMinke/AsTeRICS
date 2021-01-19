
/*
 *    AsTeRICS - Assistive Technology Rapid Integration and Construction Set
 * 
 * 
 *        d8888      88888888888       8888888b.  8888888 .d8888b.   .d8888b. 
 *       d88888          888           888   Y88b   888  d88P  Y88b d88P  Y88b
 *      d88P888          888           888    888   888  888    888 Y88b.     
 *     d88P 888 .d8888b  888   .d88b.  888   d88P   888  888         "Y888b.  
 *    d88P  888 88K      888  d8P  Y8b 8888888P"    888  888            "Y88b.
 *   d88P   888 "Y8888b. 888  88888888 888 T88b     888  888    888       "888
 *  d8888888888      X88 888  Y8b.     888  T88b    888  Y88b  d88P Y88b  d88P
 * d88P     888  88888P' 888   "Y8888  888   T88b 8888888 "Y8888P"   "Y8888P" 
 *
 *
 *                    homepage: http://www.asterics.org 
 *
 *         This project has been funded by the European Commission, 
 *                      Grant Agreement Number 247730
 *  
 *  
 *         Dual License: MIT or GPL v3.0 with "CLASSPATH" exception
 *         (please refer to the folder LICENSE)
 * 
 */

package eu.asterics.component.processor.alexacommandprocessor;

import java.nio.charset.StandardCharsets;

import eu.asterics.mw.data.ConversionUtils;
import eu.asterics.mw.model.runtime.AbstractRuntimeComponentInstance;
import eu.asterics.mw.model.runtime.IRuntimeEventListenerPort;
import eu.asterics.mw.model.runtime.IRuntimeEventTriggererPort;
import eu.asterics.mw.model.runtime.IRuntimeInputPort;
import eu.asterics.mw.model.runtime.IRuntimeOutputPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeEventTriggererPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeInputPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeOutputPort;

/**
 * 
 * <Describe purpose of this module>
 * 
 * 
 * 
 * @author <your name> [<your email address>] Date:
 */
public class AlexaCommandProcessorInstance extends AbstractRuntimeComponentInstance {
    final IRuntimeOutputPort opOutInt = new DefaultRuntimeOutputPort();
    final IRuntimeOutputPort opOutString = new DefaultRuntimeOutputPort();
    // Usage of an output port e.g.:
    // opMyOutPort.sendData(ConversionUtils.intToBytes(10));

    final IRuntimeEventTriggererPort etpMouseAction = new DefaultRuntimeEventTriggererPort();
    final IRuntimeEventTriggererPort etpKeyboardAction = new DefaultRuntimeEventTriggererPort();
    final IRuntimeEventTriggererPort etpApplicationAction = new DefaultRuntimeEventTriggererPort();
    // Usage of an event trigger port e.g.: etpMyEtPort.raiseEvent();

    // declare member variables here
    private String deviceType;
    private String commandData;

    /**
     * The class constructor.
     */
    public AlexaCommandProcessorInstance() {
        // empty constructor
    }

    /**
     * returns an Input Port.
     * 
     * @param portID the name of the port
     * @return the input port or null if not found
     */
    public IRuntimeInputPort getInputPort(String portID) {
        if ("deviceType".equalsIgnoreCase(portID)) {
            return ipDeviceType;
        }
        if ("commandData".equalsIgnoreCase(portID)) {
            return ipCommandData;
        }

        return null;
    }

    /**
     * returns an Output Port.
     * 
     * @param portID the name of the port
     * @return the output port or null if not found
     */
    public IRuntimeOutputPort getOutputPort(String portID) {
        if ("outInt".equalsIgnoreCase(portID)) {
            return opOutInt;
        }
        if ("outString".equalsIgnoreCase(portID)) {
            return opOutString;
        }

        return null;
    }

    /**
     * returns an Event Listener Port.
     * 
     * @param eventPortID the name of the port
     * @return the EventListener port or null if not found
     */
    public IRuntimeEventListenerPort getEventListenerPort(String eventPortID) {
        if ("commandReceived".equalsIgnoreCase(eventPortID)) {
            return elpCommandReceived;
        }

        return null;
    }

    /**
     * returns an Event Triggerer Port.
     * 
     * @param eventPortID the name of the port
     * @return the EventTriggerer port or null if not found
     */
    public IRuntimeEventTriggererPort getEventTriggererPort(String eventPortID) {
        if ("mouseAction".equalsIgnoreCase(eventPortID)) {
            return etpMouseAction;
        }
        if ("keyboardAction".equalsIgnoreCase(eventPortID)) {
            return etpKeyboardAction;
        }
        if ("applicationAction".equalsIgnoreCase(eventPortID)) {
            return etpApplicationAction;
        }

        return null;
    }

    /**
     * returns the value of the given property.
     * 
     * @param propertyName the name of the property
     * @return the property value or null if not found
     */
    public Object getRuntimePropertyValue(String propertyName) {

        return null;
    }

    /**
     * sets a new value for the given property.
     * 
     * @param propertyName the name of the property
     * @param newValue     the desired property value or null if not found
     */
    public Object setRuntimePropertyValue(String propertyName, Object newValue) {

        return null;
    }

    /**
     * Input Ports for receiving values.
     */
    private final IRuntimeInputPort ipDeviceType = new DefaultRuntimeInputPort() {

        public void receiveData(byte[] data) {
            deviceType = ConversionUtils.stringFromBytes(data);
        }
    };
    private final IRuntimeInputPort ipCommandData = new DefaultRuntimeInputPort() {

        public void receiveData(byte[] data) {
            commandData = ConversionUtils.stringFromBytes(data);
        }
    };

    /**
     * Event Listener Ports.
     */
    final IRuntimeEventListenerPort elpCommandReceived = new IRuntimeEventListenerPort() {
        public void receiveEvent(final String data) {
            if (deviceType.equals("MOUSE")) {
                handleMouse();
            } else if (deviceType.equals("KEYBOARD")) {
                handleKeyboard();
            } else if (deviceType.equals("APPLICATION")) {
                handleApplication();
            } else {
                System.out.println("Nothing to do... (unknown deviceType) " + deviceType);
            }
        }

        private void handleApplication() {
            switch (commandData) {
            case "7ZIP":
                opOutString.sendData("C:\\Program Files\\7-Zip\\7z.exe".getBytes(StandardCharsets.ISO_8859_1));
                etpApplicationAction.raiseEvent();
                break;
            case "NOTEPAD":
                opOutString.sendData("c:\\windows\\notepad.exe".getBytes(StandardCharsets.ISO_8859_1));
                etpApplicationAction.raiseEvent();
                break;
            default:
                System.out.println("Starting application '" + commandData + "' not supported yet.");
            }
        }

        private void handleKeyboard() {
            opOutString.sendData(commandData.getBytes(StandardCharsets.ISO_8859_1));
            etpKeyboardAction.raiseEvent();
        }

        private void handleMouse() {
            switch (commandData) {
            case "RIGHT_CLICK":
                etpMouseAction.raiseEvent();
                break;
            default:
                System.out.println("Mouse-command '" + commandData + "' not supported yet.");
            }

        }
    };

    /**
     * called when model is started.
     */
    @Override
    public void start() {

        super.start();
    }

    /**
     * called when model is paused.
     */
    @Override
    public void pause() {
        super.pause();
    }

    /**
     * called when model is resumed.
     */
    @Override
    public void resume() {
        super.resume();
    }

    /**
     * called when model is stopped.
     */
    @Override
    public void stop() {

        super.stop();
    }
}