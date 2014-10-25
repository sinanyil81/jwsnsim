package application.appFcsa;

import hardware.Register;

public class FloodingMessage {
	public int nodeid = -1;
	public Register clock = new Register();
	public float multiplier;
	
	public int rootid = -1;
	public Register rootClock = new Register();
	public int sequence = -1;
	
	public FloodingMessage(int nodeid,int rootid,Register clock,Register rootClock,float mutiplier,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.clock = new Register(clock);
		this.rootClock = new Register(rootClock);
		this.multiplier = mutiplier;
		this.sequence = sequence;
	}
	
	public FloodingMessage(FloodingMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock = new Register(msg.clock);
		this.rootClock = new Register(msg.rootClock);
		this.sequence = msg.sequence;
		this.multiplier = msg.multiplier;
	}

	public FloodingMessage() {
	
	}
}
