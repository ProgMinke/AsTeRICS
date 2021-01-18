package eu.asterics.component.sensor.alexacommandreceiver.server.event;

import eu.asterics.component.sensor.alexacommandreceiver.server.message.AlexaRequestJson;

public interface ContentReceivedEventListener {

    void onContentReceived(AlexaRequestJson payload);

    void onErrorOccurred(Exception exception);
}
