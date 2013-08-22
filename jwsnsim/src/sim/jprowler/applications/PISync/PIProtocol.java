package sim.jprowler.applications.PISync;

import sim.jprowler.Node;
import sim.jprowler.Simulator;
import sim.jprowler.UInt32;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;
import sim.jprowler.mac.CSMAMac;
import sim.jprowler.mac.MacListener;
import sim.jprowler.radio.RadioPacket;

public class PIProtocol implements TimerHandler,MacListener{
	
	private static final int BEACON_RATE = 30000000;
	private static final float MAX_PPM = 0.0001f;
	
	private static final float BOUNDARY = 2.0f*MAX_PPM*(float)BEACON_RATE;
	float K_max = 0.000004f/BOUNDARY;
	
	Timer timer0 = null;
	PIClock piClock = new PIClock();
	
	Node node;
	CSMAMac mac;
		
	public PIProtocol(Node node,CSMAMac mac){
		this.mac = mac;
		mac.addListener(this);
		
		this.node = node;
		node.turnOn();
		
		timer0 = new Timer(node.getClock(), this);
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
//		System.out.println("Node:"+node.getId() + " receiving from node " + payload.nodeid);
//		System.out.println("r clock value:"+payload.clock.toLong());
//		System.out.println("m clock value:"+piClock.getValue(packet.getEventTime()));
//		System.out.println("------------------------------------------------------------");
		
		algorithmPI(packet);		
	}
	
	private void send(){
		UInt32 localTime, globalTime;

		localTime = node.getClock().getValue();
		globalTime = piClock.getValue(localTime);

		PIPayload outgoingMsg = new PIPayload();
		outgoingMsg.nodeid = node.getId();
		outgoingMsg.clock = globalTime;

		RadioPacket packet = new RadioPacket(outgoingMsg);
		packet.setEventTime(new UInt32(localTime));
		mac.sendPacket(packet);
	}	
	
	@Override
	public void fireEvent(Timer timer) {
		if(timer == timer0){
			send();
		}		
	}
	
	public String toString(){
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + node.getId();
		s += " " + piClock.getValue(node.getClock().getValue()).toString();
		s += " " + Float.floatToIntBits((1.0f+piClock.rate)*(float)(1.0f+node.getClock().getDrift()));
		
		System.out.println(s);
		
		return s;
	}

	@Override
	public void on() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void off() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void packetLost() {
		// TODO Auto-generated method stub
		
	}
}

