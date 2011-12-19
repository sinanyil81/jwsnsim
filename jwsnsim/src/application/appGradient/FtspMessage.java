package application.appGradient;

import sim.type.UInt32;

public class FtspMessage {
	public int nodeid = -1;
	public int rootid = -1;
	public UInt32 clock = new UInt32();
	public float rate;
	public UInt32 rootClock = new UInt32();
	public int sequence = -1;	
	
	public FtspMessage(int nodeid,UInt32 clock,int rootid,float rate, UInt32 rootClock,int sequence){
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
		
		this.rootid = rootid;
		this.rate = rate;
		this.rootClock = new UInt32(rootClock);
		
		this.sequence = sequence;
	}
	
	public FtspMessage(FtspMessage msg){
		this.nodeid = msg.nodeid;
		this.clock = new UInt32(msg.clock);
		
		this.rootid = msg.rootid;
		this.rate = msg.rate;
		this.rootClock = new UInt32(msg.rootClock);

		this.sequence = msg.sequence;
	}

	public FtspMessage() {
	
	}
}
