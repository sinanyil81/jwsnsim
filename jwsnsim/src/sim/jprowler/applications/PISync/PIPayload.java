package sim.jprowler.applications.PISync;

import sim.type.UInt32;

public class PIPayload {
	public int nodeid = -1;
	public UInt32 clock = new UInt32();	

	public PIPayload(int nodeid, UInt32 clock) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
	}

	public PIPayload(PIPayload msg) {
		this(msg.nodeid, new UInt32(msg.clock));
	}

	public PIPayload() {

	}
}
