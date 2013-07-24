package sim.jprowler.applications.LowPower;

import sim.jprowler.UInt32;

public class ApplicationPayload {
	public int nodeid = -1;
	public UInt32 applicationData = new UInt32();
	
	/** piggybacked time sync information */
	public UInt32 clock = new UInt32();	

	public ApplicationPayload(int nodeid, UInt32 clock) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
	}

	public ApplicationPayload(ApplicationPayload msg) {
		this(msg.nodeid, new UInt32(msg.clock));
	}

	public ApplicationPayload() {

	}
}
