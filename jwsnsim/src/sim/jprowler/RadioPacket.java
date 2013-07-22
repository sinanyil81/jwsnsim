package sim.jprowler;

import sim.type.UInt32;

public class RadioPacket {

	private Object payload;
	private UInt32 timestamp = new UInt32();
	
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
		return timestamp;
	}
	public void setEventTime(UInt32 timestamp) {
		this.timestamp = timestamp;
	}	
}
