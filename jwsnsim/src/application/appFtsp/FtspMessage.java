package application.appFtsp;

import sim.type.UInt32;

public class FtspMessage {
	public int nodeid = -1;
	public int rootid = -1;
	public UInt32 clock = new UInt32();
	public int sequence = -1;
	
	public FtspMessage(int nodeid,int rootid,UInt32 clock,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.clock = new UInt32(clock);
		this.sequence = sequence;
	}
	
	public FtspMessage(FtspMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock = new UInt32(msg.clock);
		this.sequence = msg.sequence;
	}

	public FtspMessage() {
	
	}
}
