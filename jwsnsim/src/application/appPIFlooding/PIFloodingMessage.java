package application.appPIFlooding;

import hardware.Register32;

public class PIFloodingMessage {
	public int nodeid = -1;
	public Register32 clock = new Register32();
	public int rootid = -1;
	public int sequence = -1;
	
	public PIFloodingMessage(int nodeid,int rootid,Register32 clock,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.clock = new Register32(clock);
		this.sequence = sequence;
	}
	
	public PIFloodingMessage(PIFloodingMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock = new Register32(msg.clock);
		this.sequence = msg.sequence;
	}

	public PIFloodingMessage() {
	
	}
}
