package application.appSelf;

import hardware.Register;

public class SelfMessage7 {
	public int nodeid = -1;
	public Register clock = new Register();	
	public Register hardwareClock = new Register();
	public Register progress = new Register();
	public Register hardwareProgress = new Register();
	public float rate = 0.0f;
	
	public int sequence = -1;

	public SelfMessage7(int nodeid, Register clock, Register hardwareClock,Register progress,Register hardwareProgress,float rate, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register(clock);
		this.hardwareClock = new Register(hardwareClock);
		this.progress = new Register(progress);
		this.hardwareProgress = hardwareProgress;
		this.rate = rate;
		this.sequence = sequence;		
	}

	public SelfMessage7(SelfMessage7 msg) {
		this(msg.nodeid, new Register(msg.clock),new Register(msg.hardwareClock),new Register(msg.progress),new Register(msg.hardwareProgress),msg.rate, msg.sequence);
	}

	public SelfMessage7() {

	}
}
