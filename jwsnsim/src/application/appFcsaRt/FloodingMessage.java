package application.appFcsaRt;

import hardware.Register;

public class FloodingMessage {
	
	/* node's perspective */
	public int nodeid = -1;
	public Register localTime = new Register();
	public Register globalTime = new Register();
	public float multiplier = 0;
	
	/* flooded data */
	public int rootid = -1;
	public float rootMultiplier = 0;
	public int sequence = -1;
	
	public FloodingMessage() {
		
	}
		
	public FloodingMessage(FloodingMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.localTime = new Register(msg.localTime);
		this.globalTime = new Register(msg.globalTime);
		this.sequence = msg.sequence;
		this.multiplier = msg.multiplier;
		this.rootMultiplier = msg.rootMultiplier;
	}
}