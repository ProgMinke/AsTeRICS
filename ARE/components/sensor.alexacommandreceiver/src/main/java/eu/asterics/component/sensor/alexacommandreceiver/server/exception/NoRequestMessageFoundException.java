package eu.asterics.component.sensor.alexacommandreceiver.server.exception;

/**
 * Exception that is thrown if an incomming message does not contain a message
 * body.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 */
public class NoRequestMessageFoundException extends Exception {

    private static final long serialVersionUID = 1000625140444282459L;

    public NoRequestMessageFoundException(String message) {
        super(message);
    }

}
