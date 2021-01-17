package eu.asterics.component.sensor.alexacommandreceiver.server;

import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpMessageParser.parseAndValidate;
import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpResponseBuilder.JSON_CONTENT_TYPE;
import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_200;
import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_400;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import eu.asterics.component.sensor.alexacommandreceiver.server.exception.NoRequestMessageFoundException;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.UnsupportedRequestException;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.AlexaRequestJson;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpResponseBuilder;
import eu.asterics.mw.model.runtime.IRuntimeEventTriggererPort;
import eu.asterics.mw.model.runtime.IRuntimeOutputPort;
import eu.asterics.mw.services.AstericsErrorHandling;

public class LightweightHttpServer implements Runnable {
    // TODO http://www.java2s.com/Code/Java/Network-Protocol/HttpParser.htm

    private static final int BUFFER_SIZE = 4096;

    private final String hostname;
    private final int port;

    private ServerSocketChannel serverChannel;
    private Selector sel;

    private boolean running = false;

    // asterics ports
    private final IRuntimeOutputPort opDeviceType;
    private final IRuntimeOutputPort opCommandData;
    private final IRuntimeOutputPort opErrorData;

    private final IRuntimeEventTriggererPort etpCommandReceived;
    private final IRuntimeEventTriggererPort etpErrorOccurred;

    public LightweightHttpServer(String propHostname, int propPort, IRuntimeOutputPort opDeviceType, IRuntimeOutputPort opCommandData,
            IRuntimeOutputPort opErrorData, IRuntimeEventTriggererPort etpCommandReceived, IRuntimeEventTriggererPort etpErrorOccurred) {
        this.hostname = propHostname;
        this.port = propPort;

        this.opDeviceType = opDeviceType;
        this.opCommandData = opCommandData;
        this.opErrorData = opErrorData;
        this.etpCommandReceived = etpCommandReceived;
        this.etpErrorOccurred = etpErrorOccurred;

    }

    @Override
    public void run() {
        configureServerSocketChannel();
        AstericsErrorHandling.instance.getLogger().log(Level.ALL, "LightweightHttpServer started...");

        while (running) {
            try {
                sel.select();

                SocketChannel client = serverChannel.accept();
                byte[] messageData = readMessage(client);

                try {
                    AlexaRequestJson requestJson = parseAndValidate(messageData, "POST", "/alexa", "application/json", AlexaRequestJson.class);
                    System.out.println(requestJson);

                    opDeviceType.sendData(requestJson.getCommandType().getBytes(StandardCharsets.ISO_8859_1));
                    opCommandData.sendData(requestJson.getPayload().getBytes(StandardCharsets.ISO_8859_1));
                    opErrorData.sendData("No Error".getBytes(StandardCharsets.ISO_8859_1));
                    // TODO do something with received data
                    String payload = "{ \"errorMessage\":\"no error\" }";
                    String response = new HttpResponseBuilder().statusCode(HTTP_200).contentType(JSON_CONTENT_TYPE).payload(payload).build();
                    System.out.println(response);
                    System.out.println("Bytes written: " + client.write(ByteBuffer.wrap(response.getBytes())));
                    etpCommandReceived.raiseEvent();
                    System.out.println("Raised event commandReceived");
                } catch (UnsupportedRequestException | NoRequestMessageFoundException e) {
                    String payload = "{ \"errorMessage\":\"" + e.getMessage().substring(0, 20) + "\n}";
                    String response = new HttpResponseBuilder().statusCode(HTTP_400).payload(payload).build();

                    System.out.println(response);
                    client.write(ByteBuffer.wrap(response.getBytes()));
                    opErrorData.sendData(response.getBytes(StandardCharsets.ISO_8859_1));
                    etpErrorOccurred.raiseEvent();
                } catch (JsonMappingException | JsonParseException e) {
                    String payload = "{ \"errorMessage\":\"" + e.getMessage().substring(0, 20) + "\n}";
                    String response = new HttpResponseBuilder().statusCode(HTTP_400).payload(payload).build();
                    System.out.println(response);

                    client.write(ByteBuffer.wrap(response.getBytes()));
                    opErrorData.sendData(response.getBytes(StandardCharsets.ISO_8859_1));
                    etpErrorOccurred.raiseEvent();
                }

                client.close();

            } catch (IOException e) {
                // TODO log or re-throw?
                e.printStackTrace();
            }
        }
        try {
            serverChannel.close();
        } catch (IOException e) {
            // TODO log or re-throw?
            e.printStackTrace();
        }
    }

    private static byte[] readMessage(SocketChannel channel) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        int readBytes = channel.read(readBuffer);
        readBuffer.flip();

        byte[] requestBytes = new byte[readBytes];
        readBuffer.get(requestBytes, 0, readBytes);

        return requestBytes;
    }

    private void configureServerSocketChannel() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(hostname, port));
            serverChannel.configureBlocking(false);
            sel = Selector.open();
            serverChannel.register(sel, SelectionKey.OP_ACCEPT);
            running = true;
        } catch (IOException e) {
            AstericsErrorHandling.instance.getLogger().severe("An error occurred while opening the server socket channel. Message: " + e.getMessage());
            e.printStackTrace(); // TODO remove
            running = false; // maybe remove if really unnecessary
        }
    }

    public void shutdown() {
        running = false;
    }
}
