package application.appSelfFlooding;

import hardware.Register;

public class SelfFloodingMessage {
	public int nodeid = -1;
	public Register clock = new Register();
	public int rootid = -1;
	public int sequence = -1;
	
	public SelfFloodingMessage(int nodeid,int rootid,Register clock,int sequence){
		this.nodeid = nodeid;
		this.rootid = rootid;
		this.clock = new Register(clock);
		this.sequence = sequence;
	}
	
	public SelfFloodingMessage(SelfFloodingMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock = new Register(msg.clock);
		this.sequence = msg.sequence;
	}

	public SelfFloodingMessage() {
	
	}
}
