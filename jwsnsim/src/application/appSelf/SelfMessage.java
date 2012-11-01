package application.appSelf;

import sim.type.UInt32;

public class SelfMessage {
	public int nodeid = -1;
	public UInt32 clock = new UInt32();	
	public UInt32 offset = new UInt32();
	
	public UInt32 hardwareClock = new UInt32();
	public float rateMultiplier = 0.0f;
	
	public int sequence = -1;

	public SelfMessage(int nodeid, UInt32 clock, UInt32 offset, int sequence) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
		this.offset = new UInt32(offset);		
		this.sequence = sequence;
	}
	
	public SelfMessage(int nodeid, UInt32 clock, UInt32 offset,UInt32 hardwareClock,float rateMultiplier, int sequence) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
		this.offset = new UInt32(offset);
		
		this.hardwareClock = new UInt32(hardwareClock);
		this.rateMultiplier = rateMultiplier;
		
		this.sequence = sequence;
	}

	public SelfMessage(SelfMessage msg) {
		this(msg.nodeid, new UInt32(msg.clock),new UInt32(msg.offset),new UInt32(msg.hardwareClock),msg.rateMultiplier, msg.sequence);
	}

	public SelfMessage() {

	}
}
