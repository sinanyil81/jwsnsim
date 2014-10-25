package application.appRateDetection;

import hardware.Register32;

public class RateMessage {
	public int nodeid = -1;
	public Register32 clock = new Register32();
	public float rate;
	public float x;

	public int sequence = -1;
	
	public RateMessage(int nodeid,Register32 clock,float rate,float x,int sequence){
		this.nodeid = nodeid;
		this.clock = new Register32(clock);
		this.rate = rate;
		this.x = x;
		this.sequence = sequence;
	}
	
	public RateMessage() {
	
	}
	
	public RateMessage(RateMessage msg) {
		this.nodeid = msg.nodeid;
		this.clock = new Register32(msg.clock);
		this.rate = msg.rate;
		this.x = msg.x;
		this.sequence = msg.sequence;
	}
}
