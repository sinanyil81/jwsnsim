package application.appSelf;

import sim.type.Register;

public class SelfMessage6 {
	public int nodeid = -1;
	public Register clock = new Register();	
	public Register hardwareClock = new Register();
	public Register progress = new Register();
	public float rate = 0.0f;
	
	public int sequence = -1;

	public SelfMessage6(int nodeid, Register clock, Register hardwareClock,Register progress,float rate, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register(clock);
		this.hardwareClock = new Register(hardwareClock);
		this.progress = new Register(progress);
		this.rate = rate;
		this.sequence = sequence;		
	}

	public SelfMessage6(SelfMessage6 msg) {
		this(msg.nodeid, new Register(msg.clock),new Register(msg.hardwareClock),new Register(msg.progress),msg.rate, msg.sequence);
	}

	public SelfMessage6() {

	}
}
