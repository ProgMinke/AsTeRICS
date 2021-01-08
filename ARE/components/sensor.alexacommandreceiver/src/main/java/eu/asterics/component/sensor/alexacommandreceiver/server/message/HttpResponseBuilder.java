package eu.asterics.component.sensor.alexacommandreceiver.server.message;

import static eu.asterics.component.sensor.alexacommandreceiver.server.message.HttpStatusCode.HTTP_204;

import java.util.Map;

public class HttpResponseBuilder {

	public static final String JSON_CONTENT_TYPE = "application/json;charset=utf-8";

	public static void main(String[] args) {
		System.out.println(new HttpResponseBuilder().build());
	}

	private Map<String, String> additionalHeaders;

	private String httpVersion = "HTTP/1.1";
	private HttpStatusCode statusCode = HTTP_204;
	private String connection = "close";
	private String contentType = JSON_CONTENT_TYPE;
	private String payload = "";

	public HttpResponseBuilder httpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
		return this;
	}

	public HttpResponseBuilder connection(String connection) {
		this.connection = connection;
		return this;
	}

	public HttpResponseBuilder contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	public HttpResponseBuilder statusCode(HttpStatusCode statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	public HttpResponseBuilder payload(String payload) {
		this.payload = payload;
		return this;
	}

	public HttpResponseBuilder generic(String header, String value) {
		additionalHeaders.put(header, value);
		return this;
	}

	public String build() {
		StringBuffer sb = new StringBuffer();
		sb.append(httpVersion + " " + statusCode.getMessage() + "\n");
		sb.append("Connection: " + connection + "\n");
		if (payload != null) {
			sb.append("Content-Type: " + contentType + "\n");
			sb.append("Content-Length: " + payload.length() + "\n\n");
			sb.append(payload);
		}
		return sb.toString();
	}
}
