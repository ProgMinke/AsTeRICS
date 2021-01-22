package eu.asterics.component.sensor.alexacommandreceiver.server.exception;

/**
 * Exception that is thrown if a request contains an unsupported header detail.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 */
public class UnsupportedRequestException extends Exception {

    private static final long serialVersionUID = 1838627225870622117L;

    public UnsupportedRequestException(String message) {
        super(message);
    }
}
