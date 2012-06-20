package application.appSelf;

import sim.type.UInt32;

public class SelfMessage {
	public int nodeid = -1;
	public UInt32 clock = new UInt32();	
	public int sequence = -1;

	public SelfMessage(int nodeid, UInt32 clock, int sequence) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);		
		this.sequence = sequence;
	}

	public SelfMessage(SelfMessage msg) {
		this(msg.nodeid, new UInt32(msg.clock), msg.sequence);
	}

	public SelfMessage() {

	}
}
