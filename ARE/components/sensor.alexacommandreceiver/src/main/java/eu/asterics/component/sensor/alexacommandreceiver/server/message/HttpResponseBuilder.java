package eu.asterics.component.sensor.alexacommandreceiver.server.message;

import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_204;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Small helper class to build HTTP response messages.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 */
public class HttpResponseBuilder {

    public static final String JSON_CONTENT_TYPE = "application/json;charset=utf-8";

    private Map<String, String> additionalHeaders;

    private String httpVersion = "HTTP/1.1";
    private HttpStatusCode statusCode = HTTP_204;
    private String connection = "close";
    private String contentType = JSON_CONTENT_TYPE;
    private String payload = "";

    /**
     * Set the http version of the response.
     * 
     * @param httpVersion the http version for the response header
     * @return the builder
     */
    public HttpResponseBuilder httpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    /**
     * Sets the Connection HTTP header.
     * 
     * @param connection the value of the Connection http header
     * @return the builder
     */
    public HttpResponseBuilder connection(String connection) {
        this.connection = connection;
        return this;
    }

    /**
     * Set the Content-Type Header.
     * 
     * @param contentType the value for the Content-Type header
     * @return the builder
     */
    public HttpResponseBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets the HTTP response status code in the HTTP header.
     * 
     * @param statusCode the HTTP status code {@link HttpStatusCode}
     * @return the builder
     */
    public HttpResponseBuilder statusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * The HTTP message payload (body). Please note that Content-Length will be
     * automatically set based on the payload length. The Content-Type header will
     * be automatically set if the payload is not <code>null</code>.
     * 
     * @param payload the messages body as {@link String}
     * @return the builder
     */
    public HttpResponseBuilder payload(String payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Setter for additional HTTP headers for their name and their value.
     * 
     * @param header the HTTP header name as {@link String}
     * @param value  the HTTP header's value as {@link String}
     * @return the builder
     */
    public HttpResponseBuilder generic(String header, String value) {
        additionalHeaders.put(header, value);
        return this;
    }

    /**
     * Builds the HTTP response including all headers and the message body and
     * returns it as {@link Byte} array.
     * 
     * @return the HTTP response as {@link Byte} array
     */
    public byte[] build() {
        StringBuffer sb = new StringBuffer();
        sb.append(httpVersion + " " + statusCode.getMessage() + "\n");
        sb.append("Connection: " + connection + "\n");
        if (payload != null) {
            sb.append("Content-Type: " + contentType + "\n");
            sb.append("Content-Length: " + payload.length() + "\n\n");
            sb.append(payload);
        }
        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}
