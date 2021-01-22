package eu.asterics.component.sensor.alexacommandreceiver.server.event;

import eu.asterics.component.sensor.alexacommandreceiver.server.message.AlexaRequestJson;

/**
 * Listener API definition to react to incoming HTTP request.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 */
public interface ContentReceivedEventListener {

    /**
     * Called if the HTTP server successfully receives a message.
     * 
     * @param payload the HTTP message's body parsed into a {@link AlexaRequestJson}
     */
    void onContentReceived(AlexaRequestJson payload);

    /**
     * Called if an exception occurred while parsing the incoming HTTP message.
     * 
     * @param exception the exception that occurred
     */
    void onErrorOccurred(Exception exception);
}
