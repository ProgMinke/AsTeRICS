package eu.asterics.component.sensor.alexacommandreceiver.server.message;

public class AlexaRequestJson {

	private String deviceType;
	private String payload;

	public AlexaRequestJson() {
	}

	public AlexaRequestJson(String deviceType, String payload) {
		this.deviceType = deviceType;
		this.payload = payload;
	}

	public String getCommandType() {
		return deviceType;
	}

	public String getPayload() {
		return payload;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "AlexaRequestJson [deviceType=" + deviceType + ", payload=" + payload + "]";
	}
}
