package eu.asterics.component.sensor.alexacommandreceiver.server.message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eu.asterics.component.sensor.alexacommandreceiver.server.exception.NoRequestMessageFoundException;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.TargetNotFoundException;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.UnsupportedRequestException;

/**
 * This class implements a simple parser for HTTP messages.
 * 
 * @author Thomas Sulzbacher
 * @author Lisa Fixl
 *
 */
public class HttpMessageParser {

    /**
     * Parses and validates the given HTTP message {@link Byte} array.
     * 
     * @param <T>          the generic type of the message's body to parse the
     *                     content into
     * @param requestArray the http message as {@link Byte} array
     * @param httpMethod   the desired HTTP method as {@link String}
     * @param url          the wanted path eg. /alexa
     * @param contentType  the desired Content-Type value
     * @param jsonClass    the type of class to parse the HTTP message's body into
     * @return the parsed response in type of the jsonClass parameter
     * @throws UnsupportedRequestException    if the Content-Type header is missing
     *                                        or <code>null</code>
     * @throws NoRequestMessageFoundException if the Content-Length header is
     *                                        missing
     * @throws TargetNotFoundException        if the given url is not the target of
     *                                        the HTTP message
     * @throws JsonParseException             if an error occurs while parsing the
     *                                        messages body
     * @throws JsonMappingException           if an error occurs while parsing the
     *                                        messages body
     * @throws IOException                    if an error ocurrs while parsing the
     *                                        messages body
     */
    public static <T> T parseAndValidate(byte[] requestArray, String httpMethod, String url, String contentType, Class<T> jsonClass)//
            throws UnsupportedRequestException, NoRequestMessageFoundException, TargetNotFoundException, JsonParseException, JsonMappingException, IOException {
        String[] requestLines = new String(requestArray, StandardCharsets.ISO_8859_1).split("\n");
        Map<String, String> headers = extractHeaders(requestLines);

        validateMethodAndUrl(requestLines[0], httpMethod, url);
        checkHeaders(headers, contentType);

        String jsonString = extractMessage(requestLines, headers);
        return new ObjectMapper().readValue(jsonString, jsonClass);
    }

    private static String extractMessage(String[] requestLines, Map<String, String> headers) {
        int startIndex = Integer.parseInt(headers.get("data-start-index"));
        return Arrays.stream(Arrays.copyOfRange(requestLines, startIndex, requestLines.length)).collect(Collectors.joining());
    }

    private static void checkHeaders(Map<String, String> headers, String contentType) throws UnsupportedRequestException, NoRequestMessageFoundException {
        if (headers.get("Content-Type") == null || !headers.get("Content-Type").contains(contentType)) {
            throw new UnsupportedRequestException("This endpoint only accepts '" + contentType + "' as Content-Type.");
        } else if (headers.get("Content-Length") == null) {
            throw new NoRequestMessageFoundException("The http request must have content. Content-Length header not found.");
        }

    }

    private static Map<String, String> extractHeaders(String[] requestLines) {
        HashMap<String, String> headers = new HashMap<>();

        for (int i = 1; i < requestLines.length; i++) {
            String line = requestLines[i];
            if (line.equals("\r")) {
                headers.put("data-start-index", String.valueOf(i));
                break;
            }
            String[] parts = line.split(": ");
            headers.put(parts[0], Arrays.toString(Arrays.copyOfRange(parts, 1, parts.length)));
        }

        return headers;
    }

    private static void validateMethodAndUrl(String requestHead, String httpMethod, String url) throws TargetNotFoundException {
        String[] parts = requestHead.split(" ");
        if (!parts[0].contains(httpMethod)) {
            new UnsupportedRequestException("Http method '" + parts[0] + "' not supported.");
        } else if (!parts[1].contains(url + " ")) {
            new TargetNotFoundException("There is not target found for '" + parts[1] + "'.");
        } else if (!parts[2].equals("HTTP/1.1")) {
            new UnsupportedRequestException("'" + parts[2] + "' not supported.");
        }
    }
}
