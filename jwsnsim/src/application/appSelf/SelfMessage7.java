package application.appSelf;

import hardware.Register32;

public class SelfMessage7 {
	public int nodeid = -1;
	public Register32 clock = new Register32();	
	public Register32 hardwareClock = new Register32();
	public Register32 progress = new Register32();
	public Register32 hardwareProgress = new Register32();
	public float rate = 0.0f;
	
	public int sequence = -1;

	public SelfMessage7(int nodeid, Register32 clock, Register32 hardwareClock,Register32 progress,Register32 hardwareProgress,float rate, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register32(clock);
		this.hardwareClock = new Register32(hardwareClock);
		this.progress = new Register32(progress);
		this.hardwareProgress = hardwareProgress;
		this.rate = rate;
		this.sequence = sequence;		
	}

	public SelfMessage7(SelfMessage7 msg) {
		this(msg.nodeid, new Register32(msg.clock),new Register32(msg.hardwareClock),new Register32(msg.progress),new Register32(msg.hardwareProgress),msg.rate, msg.sequence);
	}

	public SelfMessage7() {

	}
}
