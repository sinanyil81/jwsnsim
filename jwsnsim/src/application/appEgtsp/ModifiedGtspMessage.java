package application.appEgtsp;

import sim.type.UInt32;

public class ModifiedGtspMessage {
	
	public final static int REPLY = 1;
	public final static int BEACON = 2;
	
	public int nodeid = -1;	
	public int type = 2;
	
	UInt32 logicalClock = new UInt32();
		
	public ModifiedGtspMessage(int nodeid){
		this.nodeid = nodeid;
	}
	
	public ModifiedGtspMessage(ModifiedGtspMessage msg){
		this.nodeid = msg.nodeid;
		this.type = msg.type;
		this.logicalClock = new UInt32(msg.logicalClock);
	}

	public ModifiedGtspMessage() {
	
	}
}
