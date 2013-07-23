package sim.jprowler.applications.PISync;

import sim.jprowler.Node;
import sim.jprowler.Protocol;
import sim.jprowler.RadioPacket;
import sim.jprowler.Simulator;
import sim.jprowler.UInt32;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class PIProtocol extends Protocol implements TimerHandler{
	
	private static final int BEACON_RATE = 30000000;
	private static final float MAX_PPM = 0.0001f;
	
	private static final float BOUNDARY = 2.0f*MAX_PPM*(float)BEACON_RATE;
	float K_max = 0.000004f/BOUNDARY;
	
	Timer timer0 = null;
	PIClock piClock = new PIClock();
		
	public PIProtocol(Node node){
		super (node);	
		
		getNode().turnOn();
		timer0 = new Timer(getNode().getClock(), this);
		timer0.startPeriodic(BEACON_RATE);
	}
	
	int calculateSkew(RadioPacket packet) {
		PIPayload msg = (PIPayload) packet.getPayload();

		UInt32 neighborClock = msg.clock;
		UInt32 myClock = piClock.getValue(packet.getEventTime());

		return neighborClock.subtract(myClock).toInteger();
	}
	
	private void algorithmPI(RadioPacket packet) {
		UInt32 updateTime = packet.getEventTime();
		piClock.update(updateTime);

		int skew = calculateSkew(packet);
		
		/*  initial offset compensation */ 
		if(Math.abs(skew) <= BOUNDARY){	
					
			float x = BOUNDARY - Math.abs(skew);					
			float K_i = x*K_max/BOUNDARY;
						
			piClock.rate += K_i*0.5*(float)skew;
		}	
		
				
		if(skew > 1000){
			UInt32 myClock = piClock.getValue(packet.getEventTime());
			piClock.setValue(myClock.add(skew),updateTime);
		}
		else{
			UInt32 myClock = piClock.getValue(packet.getEventTime());
			piClock.setValue(myClock.add(skew/2),updateTime);
		}		
	}

	
	public void receiveMessage(RadioPacket packet){
		PIPayload payload = (PIPayload)packet.getPayload();
//		System.out.println("------------------------------------------------------------");
//		System.out.println("Node:"+getNode().getId() + " receiving from node " + payload.nodeid);
//		System.out.println("r clock value:"+payload.clock.toLong());
//		System.out.println("m clock value:"+piClock.getValue(packet.getEventTime()));
//		System.out.println("------------------------------------------------------------");
		
		algorithmPI(packet);		
	}
	
	private void send(){
		UInt32 localTime, globalTime;

		localTime = getNode().getClock().getValue();
		globalTime = piClock.getValue(localTime);

		PIPayload outgoingMsg = new PIPayload();
		outgoingMsg.nodeid = getNode().getId();
		outgoingMsg.clock = globalTime;

		RadioPacket packet = new RadioPacket(outgoingMsg);
		packet.setEventTime(new UInt32(localTime));
		sendMessage(packet);
	}	
	
	public void sendMessageDone(){
		
	}	
	
	@Override
	public void fireEvent(Timer timer) {
		if(timer == timer0){
			send();
		}
		
	}
	
	public String toString(){
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + getNode().getId();
		s += " " + piClock.getValue(getNode().getClock().getValue()).toString();
		s += " " + Float.floatToIntBits((1.0f+piClock.rate)*(float)(1.0f+getNode().getClock().getDrift()));
		
		System.out.println(s);
		
		return s;
	}
}

