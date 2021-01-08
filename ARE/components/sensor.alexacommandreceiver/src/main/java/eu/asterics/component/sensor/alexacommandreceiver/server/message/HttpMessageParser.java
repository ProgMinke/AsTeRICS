package eu.asterics.component.sensor.alexacommandreceiver.server.message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eu.asterics.component.sensor.alexacommandreceiver.server.exception.NoRequestMessageFoundException;
import eu.asterics.component.sensor.alexacommandreceiver.server.exception.UnsupportedRequestException;

public class HttpMessageParser {

    public static <T> T parseAndValidate(byte[] requestArray, String httpMethod, String url, String contentType, Class<T> jsonClass)//
            throws UnsupportedRequestException, NoRequestMessageFoundException, JsonParseException, JsonMappingException, IOException {
        String[] requestLines = new String(requestArray, StandardCharsets.ISO_8859_1).split("\n");

        validateHttpMethodUrlAndContentType(httpMethod, url, contentType, requestLines);
        int messageBeginIndex = validateMessagePayload(requestLines);

        String json = extractMessagePayload(requestLines, messageBeginIndex);

        return new ObjectMapper().readValue(json, jsonClass);
    }

    private static String extractMessagePayload(String[] requestLines, int messageBeginIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = messageBeginIndex; i < requestLines.length; i++) {
            sb.append(requestLines[i]);
        }

        return sb.toString();
    }

    private static int validateMessagePayload(String[] requestLines) throws NoRequestMessageFoundException {
        int messageBeginIndex = -1;
        for (int i = 0; i < requestLines.length; i++) {
            if (requestLines[i].contains("Content-Length")) {
                messageBeginIndex = i + 1;
            }
        }

        if (messageBeginIndex == -1 || messageBeginIndex >= requestLines.length) {
            throw new NoRequestMessageFoundException("There was no request-message to parse in request.");
        }
        return messageBeginIndex;
    }

    private static void validateHttpMethodUrlAndContentType(String httpMethod, String url, String contentType, String[] requestLines)
            throws UnsupportedRequestException {
        if (!(requestLines[0].contains(httpMethod) && requestLines[0].contains(url + " ") && requestLines[1].contains(contentType))) {
            throw new UnsupportedRequestException("Unsupported http request.");
        }
    }

}
