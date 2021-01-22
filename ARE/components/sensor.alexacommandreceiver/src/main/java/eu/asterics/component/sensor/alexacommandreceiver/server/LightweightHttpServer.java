package eu.asterics.component.sensor.alexacommandreceiver.server;

import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpMessageParser.parseAndValidate;
import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_204;
import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_400;
import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_404;
import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_500;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eu.asterics.component.sensor.alexacommandreceiver.server.event.ContentReceivedEventListener;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.NoRequestMessageFoundException;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.TargetNotFoundException;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.UnsupportedRequestException;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.AlexaRequestJson;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.AlexaResponseJson;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpResponseBuilder;
import eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode;
import eu.asterics.mw.services.AstericsErrorHandling;

/**
 * This class implements a simple HTTP server. It is capable of waiting for a
 * single connection, handling it, sending some response and then closing the
 * connection. You can register a listener to get notified about incoming
 * messages and their payload. The server listens on the configured port and
 * hostname under the subpath "/alexa" and checks basic HTTP headers and
 * protocol structure. Note that it's not possible accept multiple connections
 * at a time.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 *
 */
public class LightweightHttpServer implements Runnable {

    private static final int BUFFER_SIZE = 4096;

    private final String hostname;
    private final int port;

    private ServerSocketChannel serverChannel;
    private Selector sel;

    private boolean running = false;

    private Map<String, ContentReceivedEventListener> listeners = new HashMap<>();

    public LightweightHttpServer(String propHostname, int propPort) {
        this.hostname = propHostname;
        this.port = propPort;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName() + new Random().nextInt(100));
        configureServerSocketChannel();
        AstericsErrorHandling.instance.getLogger().log(Level.ALL, "LightweightHttpServer started...");

        while (running) {
            try {
                if (sel.select(500) == 0) {
                    continue;
                }

                SocketChannel client = serverChannel.accept();
                byte[] messageData = readMessage(client);

                try {
                    AlexaRequestJson requestJson = parseAndValidate(messageData, "POST", "/alexa", "application/json", AlexaRequestJson.class);
                    sendResponse(client, null, HTTP_204);
                    callListenerOnReceived(requestJson);
                } catch (UnsupportedRequestException | NoRequestMessageFoundException | JsonMappingException | JsonParseException exception) {
                    sendResponse(client, exception.getMessage(), HTTP_400);
                    callListenerOnError(exception);
                } catch (TargetNotFoundException exception) {
                    sendResponse(client, exception.getMessage(), HTTP_404);
                    callListenerOnError(exception);
                }

                client.close();
                resetKeyForNextConnection();
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

    private void resetKeyForNextConnection() {
        Iterator<SelectionKey> selector = sel.selectedKeys().iterator();
        selector.next();
        selector.remove(); // needed that it can be selected again
    }

    private void callListenerOnError(final Exception e) {
        listeners.values().forEach(new Consumer<ContentReceivedEventListener>() {
            @Override
            public void accept(ContentReceivedEventListener listener) {
                listener.onErrorOccurred(e);
            }
        });
    }

    private void callListenerOnReceived(final AlexaRequestJson requestJson) {
        listeners.values().forEach(new Consumer<ContentReceivedEventListener>() {
            @Override
            public void accept(ContentReceivedEventListener listener) {
                listener.onContentReceived(requestJson);
            }
        });
    }

    private static void sendResponse(SocketChannel client, String payload, HttpStatusCode statusCode) throws IOException {
        HttpResponseBuilder builder = new HttpResponseBuilder().statusCode(statusCode);

        if (payload != null) {
            String jsonString = null;
            try {
                jsonString = new ObjectMapper().writeValueAsString(new AlexaResponseJson(payload));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (jsonString != null) {
                builder.payload(jsonString);
            } else {
                builder.statusCode(HTTP_500);
            }
        }

        client.write(ByteBuffer.wrap(builder.build()));
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
            running = false;
        }
    }

    /**
     * Closes incoming connections and safely turns off the server thread.
     */
    public void shutdown() {
        running = false;
        try {
            sel.wakeup();
            sel.close();
        } catch (IOException e) {
        }
    }

    /**
     * Adds a {@link ContentReceivedEventListener} for event notifications.
     * 
     * @param name     the name of the event listener
     * @param listener the listener implementation itself
     */
    public void addContentReceivedEventListener(String name, ContentReceivedEventListener listener) {
        if (listeners.containsKey(name)) {
            throw new IllegalArgumentException("Event listner names must be unique. Delete before entering a listener with the same name.");
        }

        listeners.put(name, listener);
    }

    /**
     * Deletes the listener with the given name.
     * 
     * @param name the name of the listener
     */
    public void deleteContentReceivedEventListener(String name) {
        listeners.remove(name);
    }
}
