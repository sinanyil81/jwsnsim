package application.appPI;

import sim.type.UInt32;

public class PIMessage {
	public int nodeid = -1;
	public UInt32 clock = new UInt32();	
	public int recipient = -1;

	public PIMessage(int nodeid, UInt32 clock,int recipient) {
		this.nodeid = nodeid;
		this.clock = new UInt32(clock);
		this.recipient = recipient;
	}

	public PIMessage(PIMessage msg) {
		this(msg.nodeid, new UInt32(msg.clock),msg.recipient);
	}

	public PIMessage() {

	}
}
