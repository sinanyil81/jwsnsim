package sim.radio;

import hardware.Register32;
import sim.node.Node;

public class RadioPacket {
	
	private Node sender;
	private Object payload;	
	private Register32 timestamp = new Register32();
	private Register32 eventTime = new Register32();
	private double intensity = 1.0;
	
	public RadioPacket(Object payload){
		this.payload = payload;
	}
	
	public RadioPacket(RadioPacket packet){
		this.sender = packet.getSender();
		this.payload = packet.getPayload();
		this.timestamp = new Register32(packet.getTimestamp());
		this.eventTime = new Register32(packet.getEventTime());
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
	
	public void setTimestamp(Register32 timestamp){
		this.timestamp = new Register32(timestamp);
	}
	
	public Register32 getTimestamp(){
		return timestamp;
	}
	
	public void setEventTime(Register32 eventTime){
		this.eventTime = new Register32(eventTime);
	}
	
	public Register32 getEventTime(){
		return eventTime;
	}
	
	public boolean equals(RadioPacket packet){
		if(payload == packet.getPayload())
			return true;
		
		return false;
	}
}
