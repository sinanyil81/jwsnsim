package application.appRateDetection;

import hardware.Register;

public class RateMessage {
	public int nodeid = -1;
	public Register clock = new Register();
	public float rate;
	public float x;

	public int sequence = -1;
	
	public RateMessage(int nodeid,Register clock,float rate,float x,int sequence){
		this.nodeid = nodeid;
		this.clock = new Register(clock);
		this.rate = rate;
		this.x = x;
		this.sequence = sequence;
	}
	
	public RateMessage() {
	
	}
	
	public RateMessage(RateMessage msg) {
		this.nodeid = msg.nodeid;
		this.clock = new Register(msg.clock);
		this.rate = msg.rate;
		this.x = msg.x;
		this.sequence = msg.sequence;
	}
}
