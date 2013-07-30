package sim.jprowler.applications.LowPower;

import sim.jprowler.Node;
import sim.jprowler.Protocol;
import sim.jprowler.RadioPacket;
import sim.jprowler.Simulator;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class TDMAProtocol extends Protocol implements TimerHandler{

	Timer timer0 = null;

	public TDMAProtocol(Node node){
		super (node);	
		
		getNode().turnOn();
		timer0 = new Timer(getNode().getClock(), this);
		RadioPacket packet = new RadioPacket(null);
		sendMessage(packet);
	}
	
	
	public void receiveMessage(RadioPacket packet){

	}
	
	public void sendMessageDone(boolean sendSuccess){
		
	}	
	
	@Override
	public void fireEvent(Timer timer) {
		if(timer == timer0){
			
		}
		
	}
	
	public String toString(){
		String s = "" + Simulator.getInstance().getSecond();
		
		TDMANode node = (TDMANode)getNode();
		LogicalClock logical = node.synchronizer.logicalClock;

		s += " " + node.getId();
		s += " " + logical.getValue(node.getClock().getValue()).toString();
		s += " " + Float.floatToIntBits((1.0f+logical.rate)*(float)(1.0f+getNode().getClock().getDrift()));
		
		System.out.println(s);
		
		return s;
	}
}

