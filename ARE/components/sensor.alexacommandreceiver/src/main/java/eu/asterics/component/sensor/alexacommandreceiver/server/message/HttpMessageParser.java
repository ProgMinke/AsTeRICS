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

public class HttpMessageParser {

    public static void main(String[] args) throws Exception {
        String message = "POST /alexa HTTP/1.1\r\n" + "Host: 3bb4243e9b83.ngrok.io\r\n" + "User-Agent: PostmanRuntime/7.26.8\r\n" + "Content-Length: 64\r\n"
                + "Accept: */*\r\n" + "Accept-Encoding: gzip, deflate, br\r\n" + "Content-Type: application/json\r\n"
                + "Postman-Token: 4601ba85-3ad8-4d15-b230-f6da5e14896f\r\n" + "X-Forwarded-For: 193.171.43.23\r\n" + "X-Forwarded-Proto: http\r\n" + "\r\n"
                + "{\r\n" + "    \"deviceType\": \"APPLICATION\",\r\n" + "    \"payload\": \"NOTEPAD\"\r\n" + "}";
        AlexaRequestJson json = parseAndValidate(message.getBytes(), "POST", "/alexa", "application/json", AlexaRequestJson.class);
        System.out.println(json);
    }

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
