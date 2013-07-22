package sim.jprowler;

public class RadioPacket {

	private Object payload;
	private UInt32 timestamp = new UInt32();
	private UInt32 eventTime = new UInt32();
	
	public RadioPacket(Object payload){
		this.payload = payload;
	}
	
	public Object getPayload() {
		return payload;
	}
	
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	
	public UInt32 getEventTime() {
		return eventTime;
	}
	public void setEventTime(UInt32 eventTime) {
		this.eventTime = eventTime;
	}

	public UInt32 getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(UInt32 timestamp) {
		this.timestamp = timestamp;
	}	
}
