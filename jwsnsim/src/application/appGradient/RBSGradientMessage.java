package application.appGradient;

import sim.type.UInt32;

public class RBSGradientMessage {

	public final static int REPLY = 1;
	public final static int INFO = 2;
	
	public int type;
	public int rootid;
	public int nodeid;
	public UInt32 clock;
	public int sequence;
			
	public RBSGradientMessage(int nodeid){
		this.nodeid = nodeid;
		rootid = -1;
		clock =  new UInt32();
		sequence = 0;			
	}
	
	public RBSGradientMessage(RBSGradientMessage msg){
		this.nodeid = msg.nodeid;
		this.rootid = msg.rootid;
		this.clock =  new UInt32(msg.clock);
		this.type = msg.type;
		this.sequence = msg.sequence;
	}
}
