package application.appGradient;

import sim.type.UInt32;

public class FloodingMessage {
	public int nodeid = -1;
	public UInt32 localTime = new UInt32();
	public UInt32 globalTime = new UInt32();
	public float multiplier;
	
	public int rootid = -1;
	public float rootMultiplier;
	public int sequence = -1;
	
	public FloodingMessage(int nodeid,int rootid,UInt32 clock,UInt32 rootClock,float mutiplier,float rootMultiplier,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.localTime = new UInt32(clock);
		this.globalTime = new UInt32(rootClock);
		this.multiplier = mutiplier;
		this.rootMultiplier = rootMultiplier;
		this.sequence = sequence;
	}
	
	public FloodingMessage(FloodingMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.localTime = new UInt32(msg.localTime);
		this.globalTime = new UInt32(msg.globalTime);
		this.sequence = msg.sequence;
		this.multiplier = msg.multiplier;
		this.rootMultiplier = msg.rootMultiplier;
	}

	public FloodingMessage() {
	
	}
}
