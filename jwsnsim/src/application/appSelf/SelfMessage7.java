package application.appSelf;

import sim.type.UInt32;

public class SelfMessage7 {
	public int nodeid = -1;
	public UInt32 clock = new UInt32();	
	public UInt32 hardwareClock = new UInt32();
	public UInt32 progress = new UInt32();
	public UInt32 hardwareProgress = new UInt32();
	public float rate = 0.0f;
	
	public int sequence = -1;

	public SelfMessage7(int nodeid, UInt32 clock, UInt32 hardwareClock,UInt32 progress,UInt32 hardwareProgress,float rate, int sequence) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
		this.hardwareClock = new UInt32(hardwareClock);
		this.progress = new UInt32(progress);
		this.hardwareProgress = hardwareProgress;
		this.rate = rate;
		this.sequence = sequence;		
	}

	public SelfMessage7(SelfMessage7 msg) {
		this(msg.nodeid, new UInt32(msg.clock),new UInt32(msg.hardwareClock),new UInt32(msg.progress),new UInt32(msg.hardwareProgress),msg.rate, msg.sequence);
	}

	public SelfMessage7() {

	}
}
