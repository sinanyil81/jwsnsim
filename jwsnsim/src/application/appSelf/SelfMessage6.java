package application.appSelf;

import hardware.Register32;

public class SelfMessage6 {
	public int nodeid = -1;
	public Register32 clock = new Register32();	
	public Register32 hardwareClock = new Register32();
	public Register32 progress = new Register32();
	public float rate = 0.0f;
	
	public int sequence = -1;

	public SelfMessage6(int nodeid, Register32 clock, Register32 hardwareClock,Register32 progress,float rate, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register32(clock);
		this.hardwareClock = new Register32(hardwareClock);
		this.progress = new Register32(progress);
		this.rate = rate;
		this.sequence = sequence;		
	}

	public SelfMessage6(SelfMessage6 msg) {
		this(msg.nodeid, new Register32(msg.clock),new Register32(msg.hardwareClock),new Register32(msg.progress),msg.rate, msg.sequence);
	}

	public SelfMessage6() {

	}
}
