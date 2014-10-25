package application.appFcsa;

import hardware.Register32;

public class FloodingMessage {
	public int nodeid = -1;
	public Register32 clock = new Register32();
	public float multiplier;
	
	public int rootid = -1;
	public Register32 rootClock = new Register32();
	public int sequence = -1;
	
	public FloodingMessage(int nodeid,int rootid,Register32 clock,Register32 rootClock,float mutiplier,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.clock = new Register32(clock);
		this.rootClock = new Register32(rootClock);
		this.multiplier = mutiplier;
		this.sequence = sequence;
	}
	
	public FloodingMessage(FloodingMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock = new Register32(msg.clock);
		this.rootClock = new Register32(msg.rootClock);
		this.sequence = msg.sequence;
		this.multiplier = msg.multiplier;
	}

	public FloodingMessage() {
	
	}
}
