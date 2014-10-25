package application.appSelf;

import sim.type.Register;

public class SelfMessage9 {
	public int nodeid = -1;
	public Register clock = new Register();	
	public Register offset = new Register();
	
	public Register hardwareClock = new Register();
	public float rateMultiplier = 0.0f;
	
	public int sequence = -1;

	public SelfMessage9(int nodeid, Register clock, Register offset, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register(clock);
		this.offset = new Register(offset);		
		this.sequence = sequence;
	}
	
	public SelfMessage9(int nodeid, Register clock, Register offset,Register hardwareClock,float rateMultiplier,int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register(clock);
		this.offset = new Register(offset);
		
		this.hardwareClock = new Register(hardwareClock);
		this.rateMultiplier = rateMultiplier;
		
		this.sequence = sequence;
	}

	public SelfMessage9(SelfMessage9 msg) {
		this(msg.nodeid, new Register(msg.clock),new Register(msg.offset),new Register(msg.hardwareClock),msg.rateMultiplier,msg.sequence);
	}

	public SelfMessage9() {

	}
}
