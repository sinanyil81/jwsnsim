package application.appSelf;

import sim.type.UInt32;

public class SelfMessage5 {
	public int nodeid = -1;
	public UInt32 clock = new UInt32();	
	public UInt32 hardwareClock = new UInt32();
	public UInt32 progress = new UInt32();
	
	public int sequence = -1;

	public SelfMessage5(int nodeid, UInt32 clock, UInt32 hardwareClock,UInt32 progress, int sequence) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
		this.hardwareClock = new UInt32(hardwareClock);
		this.progress = new UInt32(progress);
		this.sequence = sequence;
	}

	public SelfMessage5(SelfMessage5 msg) {
		this(msg.nodeid, new UInt32(msg.clock),new UInt32(msg.hardwareClock),new UInt32(msg.progress), msg.sequence);
	}

	public SelfMessage5() {

	}
}
