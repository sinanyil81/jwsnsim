package application.appSelf;

import sim.type.UInt32;

public class SelfMessage {
	public int nodeid = -1;
	public UInt32 clock = new UInt32();
	public float criticality; // 0 - 100
	public int sequence = -1;

	public SelfMessage(int nodeid, UInt32 clock, float criticality, int sequence) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
		this.criticality = criticality;
		this.sequence = sequence;
	}

	public SelfMessage(SelfMessage msg) {
		this(msg.nodeid, new UInt32(msg.clock), msg.criticality, msg.sequence);
	}

	public SelfMessage() {

	}
}
