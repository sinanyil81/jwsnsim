package sim.radio;

import hardware.Register;
import sim.node.Node;

public class RadioPacket {
	
	private Node sender;
	private Object payload;	
	private Register timestamp = new Register();
	private Register eventTime = new Register();
	private double intensity = 1.0;
	
	public RadioPacket(Object payload){
		this.payload = payload;
	}
	
	public RadioPacket(RadioPacket packet){
		this.sender = packet.getSender();
		this.payload = packet.getPayload();
		this.timestamp = new Register(packet.getTimestamp());
		this.eventTime = new Register(packet.getEventTime());
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
	
	public void setTimestamp(Register timestamp){
		this.timestamp = new Register(timestamp);
	}
	
	public Register getTimestamp(){
		return timestamp;
	}
	
	public void setEventTime(Register eventTime){
		this.eventTime = new Register(eventTime);
	}
	
	public Register getEventTime(){
		return eventTime;
	}
	
	public boolean equals(RadioPacket packet){
		if(payload == packet.getPayload())
			return true;
		
		return false;
	}
}
