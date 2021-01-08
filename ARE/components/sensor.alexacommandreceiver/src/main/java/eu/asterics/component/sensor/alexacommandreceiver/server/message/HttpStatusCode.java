package eu.asterics.component.sensor.alexacommandreceiver.server.message;

public enum HttpStatusCode {

	HTTP_200("200 OK"), //
	HTTP_202("202 Accepted"), //
	HTTP_204("204 No Content"), //
	HTTP_400("400 Bad Request"), //
	HTTP_404("404 Not Found"), //
	HTTP_405("405 Method Not Allowed"), //
	HTTP_415("415 Unsupported Media Type"), //
	HTTP_500("500 Internal Server Error");

	private String message;

	private HttpStatusCode(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
