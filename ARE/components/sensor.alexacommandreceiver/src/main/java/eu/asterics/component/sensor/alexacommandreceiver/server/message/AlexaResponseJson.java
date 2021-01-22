package eu.asterics.component.sensor.alexacommandreceiver.server.message;

/**
 * A simple POJO representing the response part.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 */
public class AlexaResponseJson {

    private String message;

    public AlexaResponseJson(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
