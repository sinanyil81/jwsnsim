package sim.jprowler.applications.LowPower;

import sim.jprowler.Node;
import sim.jprowler.Protocol;
import sim.jprowler.RadioPacket;
import sim.jprowler.Simulator;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class LowPowerProtocol extends Protocol implements TimerHandler{
	
	private static final int BEACON_RATE = 20000000;
	
	Timer timer0 = null;
		
	public LowPowerProtocol(Node node){
		super (node);	
		
		getNode().turnOn();
		timer0 = new Timer(getNode().getClock(), this);
		timer0.startPeriodic(BEACON_RATE);
	}
	
	
	public void receiveMessage(RadioPacket packet){
		ApplicationPayload payload = (ApplicationPayload)packet.getPayload();
		System.out.println("------------------------------------------------------------");
		System.out.println("Node:"+getNode().getId() + " receiving from node " + payload.nodeid);
		System.out.println("------------------------------------------------------------");
	}
	
	private void send(){
		ApplicationPayload outgoingMsg = new ApplicationPayload();
		outgoingMsg.nodeid = getNode().getId();
		RadioPacket packet = new RadioPacket(outgoingMsg);
		sendMessage(packet);
	}	
	
	public void sendMessageDone(boolean sendSuccess){
		
	}	
	
	@Override
	public void fireEvent(Timer timer) {
		if(timer == timer0){
			send();
		}
		
	}
	
	public String toString(){
		String s = "" + Simulator.getInstance().getSecond();
	
//		System.out.println(s);
		
		return s;
	}
}

