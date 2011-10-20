package application.appGtsp;

import sim.type.UInt32;

public class GtspMessage {
	public int nodeid = -1;	
	
	public UInt32 logicalClock = new UInt32();
	public double rate = 1;
	
	public GtspMessage(int nodeid,UInt32 logical,double rate){
		this.nodeid = nodeid;
		this.logicalClock = new UInt32(logical);
		this.rate = rate;
	}
	
	public GtspMessage(GtspMessage msg){
		this.nodeid = msg.nodeid;
		this.logicalClock = new UInt32(msg.logicalClock);
		this.rate = msg.rate;
	}

	public GtspMessage() {
	
	}
}
