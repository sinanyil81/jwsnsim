package application.appSelf;

import hardware.Register32;

public class SelfMessage {
	public int nodeid = -1;
	public Register32 clock = new Register32();	
	public Register32 offset = new Register32();
	
	public Register32 hardwareClock = new Register32();
	public float rateMultiplier = 0.0f;
	
	public int sequence = -1;	

	public SelfMessage(int nodeid, Register32 clock, Register32 offset, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register32(clock);
		this.offset = new Register32(offset);		
		this.sequence = sequence;
	}
	
	public SelfMessage(int nodeid, Register32 clock, Register32 offset,Register32 hardwareClock,float rateMultiplier, int sequence) {
		this.nodeid = nodeid;
		this.clock = new Register32(clock);
		this.offset = new Register32(offset);
		
		this.hardwareClock = new Register32(hardwareClock);
		this.rateMultiplier = rateMultiplier;
		
		this.sequence = sequence;
	}

	public SelfMessage(SelfMessage msg) {
		this(msg.nodeid, new Register32(msg.clock),new Register32(msg.offset),new Register32(msg.hardwareClock),msg.rateMultiplier, msg.sequence);
	}

	public SelfMessage() {

	}
}
