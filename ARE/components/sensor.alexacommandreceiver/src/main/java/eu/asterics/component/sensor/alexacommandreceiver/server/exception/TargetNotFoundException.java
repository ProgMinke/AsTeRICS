package eu.asterics.component.sensor.alexacommandreceiver.server.exception;

/**
 * Exception that is thrown if the incoming message wants to reach a path that
 * is not existing.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 *
 */
public class TargetNotFoundException extends Exception {

    private static final long serialVersionUID = -6641194204964885427L;

    public TargetNotFoundException(String message) {
        super(message);
    }
}
