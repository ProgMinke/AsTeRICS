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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import eu.asterics.component.sensor.alexacommandreceiver.server.exception.NoRequestMessageFoundException;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.UnsupportedRequestException;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.AlexaRequestJson;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpResponseBuilder;
import eu.asterics.mw.services.AstericsErrorHandling;

public class LightweightHttpServer {
    // TODO http://www.java2s.com/Code/Java/Network-Protocol/HttpParser.htm

    public static void main(String[] args) {
        new LightweightHttpServer("localhost", 8182).run();
    }

    private static final int BUFFER_SIZE = 4096;

    private final String hostname;
    private final int port;

    private ServerSocketChannel serverChannel;
    private Selector sel;

    private boolean running = false;

    public LightweightHttpServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    private byte[] readMessage(SocketChannel channel) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        int readBytes = channel.read(readBuffer);
        readBuffer.flip();

        byte[] requestBytes = new byte[readBytes];
        readBuffer.get(requestBytes, 0, readBytes);

        return requestBytes;
    }

    public void run() {
        configureServerSocketChannel();
        System.out.println("Server started...");

        while (running) {
            try {
                sel.select();

                SocketChannel client = serverChannel.accept();
                byte[] messageData = readMessage(client);

                try {
                    AlexaRequestJson requestJson = parseAndValidate(messageData, "POST", "/alexa", "application/json", AlexaRequestJson.class);
                    System.out.println(requestJson);
                    // TODO do something with received data
                    String payload = "{ \"errorMessage\":\"no error\" }";
                    String response = new HttpResponseBuilder().statusCode(HTTP_200).contentType(JSON_CONTENT_TYPE).payload(payload).build();
                    System.out.println(response);
                    System.out.println("Bytes written: " + client.write(ByteBuffer.wrap(response.getBytes())));
                } catch (UnsupportedRequestException | NoRequestMessageFoundException e) {
                    String payload = "{ \"errorMessage\":\"" + e.getMessage().substring(0, 20) + "\n}";
                    String response = new HttpResponseBuilder().statusCode(HTTP_400).payload(payload).build();

                    System.out.println(response);
                    client.write(ByteBuffer.wrap(response.getBytes()));
                } catch (JsonMappingException | JsonParseException e) {
                    String payload = "{ \"errorMessage\":\"" + e.getMessage().substring(0, 20) + "\n}";
                    String response = new HttpResponseBuilder().statusCode(HTTP_400).payload(payload).build();
                    System.out.println(response);

                    client.write(ByteBuffer.wrap(response.getBytes()));
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
}
