package application.appFcsaRt;

import sim.type.UInt32;

public class GradientMessage {
	
	/* node's perspective */
	public int nodeid = -1;
	public UInt32 localTime = new UInt32();
	public UInt32 globalTime = new UInt32();
	public float multiplier = 0;
	
	/* flooded data */
	public int rootid = -1;
	public float rootMultiplier = 0;
	public UInt32 rootOffset = new UInt32();
	public int sequence = -1;
	
	public GradientMessage() {
		
	}
		
	public GradientMessage(GradientMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.localTime = new UInt32(msg.localTime);
		this.globalTime = new UInt32(msg.globalTime);
		this.sequence = msg.sequence;
		this.multiplier = msg.multiplier;
		this.rootMultiplier = msg.rootMultiplier;
		this.rootOffset = msg.rootOffset;
	}
}