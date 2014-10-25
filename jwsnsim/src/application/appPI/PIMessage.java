package application.appPI;

import hardware.Register;

public class PIMessage {
	public int nodeid = -1;
	public Register clock = new Register();	
	public int recipient = -1;

	public PIMessage(int nodeid, Register clock,int recipient) {
		this.nodeid = nodeid;
		this.clock = new Register(clock);
		this.recipient = recipient;
	}

	public PIMessage(PIMessage msg) {
		this(msg.nodeid, new Register(msg.clock),msg.recipient);
	}

	public PIMessage() {

	}
}
