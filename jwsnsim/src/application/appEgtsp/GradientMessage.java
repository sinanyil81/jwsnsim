package application.appEgtsp;

import hardware.Register32;

public class GradientMessage {
	
	/* node's perspective */
	public int nodeid = -1;
	public Register32 localTime = new Register32();
	public Register32 globalTime = new Register32();
	public float multiplier = 0;
	
	/* flooded data */
	public int rootid = -1;
	public float rootMultiplier = 0;
	public Register32 rootOffset = new Register32();
	public int sequence = -1;
	
	public GradientMessage() {
		
	}
		
	public GradientMessage(GradientMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.localTime = new Register32(msg.localTime);
		this.globalTime = new Register32(msg.globalTime);
		this.sequence = msg.sequence;
		this.multiplier = msg.multiplier;
		this.rootMultiplier = msg.rootMultiplier;
		this.rootOffset = msg.rootOffset;
	}
}