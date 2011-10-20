package sim.radio;

import sim.node.Node;
import sim.type.UInt32;

public class RadioPacket {
	
	private Node sender;
	private Object payload;	
	private UInt32 timestamp = new UInt32();
	private UInt32 eventTime = new UInt32();
	private double intensity = 1.0;
	
	public RadioPacket(Object payload){
		this.payload = payload;
	}
	
	public RadioPacket(RadioPacket packet){
		this.sender = packet.getSender();
		this.payload = packet.getPayload();
		this.timestamp = new UInt32(packet.getTimestamp());
		this.eventTime = new UInt32(packet.getEventTime());
		this.intensity = packet.intensity;
	}
	
	public void setIntensity(double intensity){
		this.intensity = intensity;
	}
	
	public double getIntensity(){
		return intensity;
	}
	
	public void setSender(Node node){
		this.sender = node;
	}
	
	public Node getSender() {
		return sender;
	}

	public void setPayload(Object payload){
		this.payload = payload;
	}
	
	public Object getPayload(){
		return payload;
	}
	
	public void setTimestamp(UInt32 timestamp){
		this.timestamp = new UInt32(timestamp);
	}
	
	public UInt32 getTimestamp(){
		return timestamp;
	}
	
	public void setEventTime(UInt32 eventTime){
		this.eventTime = new UInt32(eventTime);
	}
	
	public UInt32 getEventTime(){
		return eventTime;
	}
	
	public boolean equals(RadioPacket packet){
		if(payload == packet.getPayload())
			return true;
		
		return false;
	}
}
