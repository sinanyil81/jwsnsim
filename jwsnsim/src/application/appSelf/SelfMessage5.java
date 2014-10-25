package application.appSelf;

import hardware.Register;

public class SelfMessage5 {
	public int nodeid = -1;
	public Register clock = new Register();	
	public Register hardwareClock = new Register();
	public Register progress = new Register();
	
	public int sequence = -1;

	public SelfMessage5(int nodeid, Register clock, Register hardwareClock,Register progress, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register(clock);
		this.hardwareClock = new Register(hardwareClock);
		this.progress = new Register(progress);
		this.sequence = sequence;
	}

	public SelfMessage5(SelfMessage5 msg) {
		this(msg.nodeid, new Register(msg.clock),new Register(msg.hardwareClock),new Register(msg.progress), msg.sequence);
	}

	public SelfMessage5() {

	}
}
