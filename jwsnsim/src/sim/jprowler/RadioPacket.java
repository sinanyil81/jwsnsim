package sim.jprowler;

import sim.type.UInt32;

public class RadioPacket {

	private Object payload;
	private UInt32 timestamp;
	
	public RadioPacket(Object payload){
		this.payload = payload;
	}
	
	public Object getPayload() {
		return payload;
	}
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	public UInt32 getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(UInt32 timestamp) {
		this.timestamp = timestamp;
	}	
}
