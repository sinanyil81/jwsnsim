package application.appPulseSync;

import sim.type.UInt32;

public class PulseSyncMessage {
	public int nodeid = -1;
	public int rootid = -1;
	public UInt32 clock = new UInt32();
	public int sequence = -1;
	
	public PulseSyncMessage(int nodeid,int rootid,UInt32 clock,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.clock = new UInt32(clock);
		this.sequence = sequence;
	}
	
	public PulseSyncMessage(PulseSyncMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock = new UInt32(msg.clock);
		this.sequence = msg.sequence;
	}

	public PulseSyncMessage() {
	
	}
}
