package application.appEgtsp;

import hardware.Register;

public class GradientMessage {
	
	/* node's perspective */
	public int nodeid = -1;
	public Register localTime = new Register();
	public Register globalTime = new Register();
	public float multiplier = 0;
	
	/* flooded data */
	public int rootid = -1;
	public float rootMultiplier = 0;
	public Register rootOffset = new Register();
	public int sequence = -1;
	
	public GradientMessage() {
		
	}
		
	public GradientMessage(GradientMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.localTime = new Register(msg.localTime);
		this.globalTime = new Register(msg.globalTime);
		this.sequence = msg.sequence;
		this.multiplier = msg.multiplier;
		this.rootMultiplier = msg.rootMultiplier;
		this.rootOffset = msg.rootOffset;
	}
}