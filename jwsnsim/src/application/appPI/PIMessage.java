package application.appPI;

import hardware.Register32;

public class PIMessage {
	public int nodeid = -1;
	public Register32 clock = new Register32();	
	public int recipient = -1;

	public PIMessage(int nodeid, Register32 clock,int recipient) {
		this.nodeid = nodeid;
		this.clock = new Register32(clock);
		this.recipient = recipient;
	}

	public PIMessage(PIMessage msg) {
		this(msg.nodeid, new Register32(msg.clock),msg.recipient);
	}

	public PIMessage() {

	}
}
