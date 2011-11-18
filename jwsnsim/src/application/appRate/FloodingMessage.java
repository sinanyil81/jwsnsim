package application.appRate;

import sim.type.UInt32;

public class FloodingMessage {
	
	public int nodeid = -1;	
	public UInt32 clock = new UInt32();

	public int rootid = -1;	
	public UInt32 rootClock = new UInt32();
	public float rootRate;	
	public int sequence = -1;
	
	public FloodingMessage(int nodeid,int rootid,UInt32 clock,UInt32 rootClock,float rootRate,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.clock = new UInt32(clock);
		this.rootClock = new UInt32(rootClock);
		this.rootRate = rootRate;
		this.sequence = sequence;
	}
	
	public FloodingMessage(FloodingMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock = new UInt32(msg.clock);
		this.rootClock = new UInt32(msg.rootClock);
		this.sequence = msg.sequence;
		this.rootRate = msg.rootRate;
	}

	public FloodingMessage() {
	
	}
}