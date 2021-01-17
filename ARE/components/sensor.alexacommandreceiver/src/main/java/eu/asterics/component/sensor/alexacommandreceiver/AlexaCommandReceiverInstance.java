
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

package eu.asterics.component.sensor.alexacommandreceiver;

import eu.asterics.component.sensor.alexacommandreceiver.server.LightweightHttpServer;
import eu.asterics.mw.model.runtime.AbstractRuntimeComponentInstance;
import eu.asterics.mw.model.runtime.IRuntimeEventListenerPort;
import eu.asterics.mw.model.runtime.IRuntimeEventTriggererPort;
import eu.asterics.mw.model.runtime.IRuntimeInputPort;
import eu.asterics.mw.model.runtime.IRuntimeOutputPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeEventTriggererPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeOutputPort;
import eu.asterics.mw.services.AstericsThreadPool;

/**
 * 
 * <Describe purpose of this module>
 * 
 * 
 * 
 * @author <your name> [<your email address>] Date:
 */
public class AlexaCommandReceiverInstance extends AbstractRuntimeComponentInstance {
    final IRuntimeOutputPort opDeviceType = new DefaultRuntimeOutputPort();
    final IRuntimeOutputPort opCommandData = new DefaultRuntimeOutputPort();
    final IRuntimeOutputPort opErrorData = new DefaultRuntimeOutputPort();
    // Usage of an output port e.g.:
    // opMyOutPort.sendData(ConversionUtils.intToBytes(10));

    final IRuntimeEventTriggererPort etpCommandReceived = new DefaultRuntimeEventTriggererPort();
    final IRuntimeEventTriggererPort etpErrorOccurred = new DefaultRuntimeEventTriggererPort();
    // Usage of an event trigger port e.g.: etpMyEtPort.raiseEvent();

    String propHostname = "localhost";
    int propPort = 8182;

    // declare member variables here
    private LightweightHttpServer server;

    /**
     * The class constructor.
     */
    public AlexaCommandReceiverInstance() {
        server = new LightweightHttpServer(propHostname, propPort, opDeviceType, opCommandData, opErrorData, etpCommandReceived, etpErrorOccurred);
    }

    /**
     * returns an Input Port.
     * 
     * @param portID the name of the port
     * @return the input port or null if not found
     */
    public IRuntimeInputPort getInputPort(String portID) {

        return null;
    }

    /**
     * returns an Output Port.
     * 
     * @param portID the name of the port
     * @return the output port or null if not found
     */
    public IRuntimeOutputPort getOutputPort(String portID) {
        if ("deviceType".equalsIgnoreCase(portID)) {
            return opDeviceType;
        }
        if ("commandData".equalsIgnoreCase(portID)) {
            return opCommandData;
        }
        if ("errorData".equalsIgnoreCase(portID)) {
            return opErrorData;
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

        return null;
    }

    /**
     * returns an Event Triggerer Port.
     * 
     * @param eventPortID the name of the port
     * @return the EventTriggerer port or null if not found
     */
    public IRuntimeEventTriggererPort getEventTriggererPort(String eventPortID) {
        if ("commandReceived".equalsIgnoreCase(eventPortID)) {
            return etpCommandReceived;
        }
        if ("errorOccurred".equalsIgnoreCase(eventPortID)) {
            return etpErrorOccurred;
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
        if ("hostname".equalsIgnoreCase(propertyName)) {
            return propHostname;
        }
        if ("port".equalsIgnoreCase(propertyName)) {
            return propPort;
        }

        return null;
    }

    /**
     * sets a new value for the given property.
     * 
     * @param propertyName the name of the property
     * @param newValue     the desired property value or null if not found
     */
    public Object setRuntimePropertyValue(String propertyName, Object newValue) {
        if ("hostname".equalsIgnoreCase(propertyName)) {
            final Object oldValue = propHostname;
            propHostname = (String) newValue;
            return oldValue;
        }
        if ("port".equalsIgnoreCase(propertyName)) {
            final Object oldValue = propPort;
            propPort = Integer.parseInt(newValue.toString());
            return oldValue;
        }

        return null;
    }

    /**
     * Input Ports for receiving values.
     */

    /**
     * Event Listerner Ports.
     */

    /**
     * called when model is started.
     */
    @Override
    public void start() {
        super.start();
        AstericsThreadPool.getInstance().execute(server);
    }

    /**
     * called when model is paused.
     */
    @Override
    public void pause() {
        super.pause();
        server.shutdown();
    }

    /**
     * called when model is resumed.
     */
    @Override
    public void resume() {
        super.resume();
        AstericsThreadPool.getInstance().execute(server);
    }

    /**
     * called when model is stopped.
     */
    @Override
    public void stop() {
        super.stop();
        server.shutdown();
    }
}