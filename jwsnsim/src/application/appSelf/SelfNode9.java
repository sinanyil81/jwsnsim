package application.appSelf;

import hardware.Register32;
import fr.irit.smac.util.avt.Feedback;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.radio.RadioPacket;
import sim.radio.SimpleRadio;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class SelfNode9 extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	
	private static final int TOLERANCE = 0;

	LogicalClock9 logicalClock = new LogicalClock9();
	Timer timer0;
	
	SelfMessage9 outgoingMsg = new SelfMessage9();
	
	class NeighborData {
		public int id = -1;		
		int skew;
		float delta;
	}
	
	NeighborData fastestNeighbor = new NeighborData();
	NeighborData slowestNeighbor = new NeighborData();

	public SelfNode9(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		
		/* to start clock with a random value */
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));
		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":" + (int)(CLOCK.getDrift()*1000000.0));
	}

	int calculateSkew(RadioPacket packet) {
		SelfMessage9 msg = (SelfMessage9) packet.getPayload();

		Register32 neighborClock = msg.clock;
		Register32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toInteger();
	}

	private void adjustClock() {
		
		if(update == true){
			adjustOffset();
			adjustSpeed();
		}
		else{

		}
		
		fastestNeighbor = new NeighborData();
		slowestNeighbor = new NeighborData();
		
		update = true;
	}
	
	private void adjustSpeed(){

		int skew = slowestNeighbor.skew/2;
		int skewRest = slowestNeighbor.skew %2;
		skew += fastestNeighbor.skew/2;
		skewRest += fastestNeighbor.skew%2;
		skew += skewRest/2;
			
		if (skew > TOLERANCE) {
//			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
			logicalClock.rate.adjustValue(Feedback.LOWER);
		} else if (skew < (-1.0) * TOLERANCE) {
//			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
			logicalClock.rate.adjustValue(Feedback.GREATER);
		} else {
//			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
			logicalClock.rate.adjustValue(Feedback.GOOD);
		}		
	}

	private void adjustOffset() {	
				
		int skew = slowestNeighbor.skew/2;
		int skewRest = slowestNeighbor.skew %2;
		skew += fastestNeighbor.skew/2;
		skewRest += fastestNeighbor.skew%2;
		skew += skewRest/2;
		
		Register32 offset = logicalClock.getOffset();
		offset = offset.add(-skew);
		logicalClock.setOffset(offset);
	}
	
	
	boolean update = true;

	private void updateLocalInfo(RadioPacket packet) {
		SelfMessage9 msg = (SelfMessage9) packet.getPayload();
		
		int skew = calculateSkew(packet);
		
		if(skew > -5000){
			if(fastestNeighbor.id == -1){
				fastestNeighbor.id = msg.nodeid;
				fastestNeighbor.skew = skew;
				fastestNeighbor.delta = msg.rateMultiplier;
			}
			else if(skew < fastestNeighbor.skew || fastestNeighbor.id == msg.nodeid){
				fastestNeighbor.id = msg.nodeid;
				fastestNeighbor.skew = skew;
				fastestNeighbor.delta = msg.rateMultiplier;
			}
			
			if(slowestNeighbor.id == -1){
				slowestNeighbor.id = msg.nodeid;
				slowestNeighbor.skew = skew;
				slowestNeighbor.delta = msg.rateMultiplier;
			}
			else if(skew > slowestNeighbor.skew || slowestNeighbor.id == msg.nodeid){
				slowestNeighbor.id = msg.nodeid;
				slowestNeighbor.skew = skew;
				slowestNeighbor.delta = msg.rateMultiplier;
			}			
		}
		else{			
			logicalClock.setValue(msg.clock, packet.getEventTime());
//			if(update == true)
				
//			logicalClock.resetRate();
			update = false;
		}		
	}

	@Override
	public void receiveMessage(RadioPacket packet) {
		logicalClock.update(packet.getEventTime());
		updateLocalInfo(packet);		
	}

	@Override
	public void fireEvent(Timer timer) {
		sendMsg();
	}

	private void sendMsg() {
		Register32 localTime, globalTime;
		
		localTime = CLOCK.getValue();
		
		logicalClock.update(localTime);		
		globalTime = logicalClock.getValue(localTime);
		
		adjustClock();

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = globalTime;
		outgoingMsg.offset = logicalClock.getOffset();
		outgoingMsg.sequence++;		
		
		outgoingMsg.rateMultiplier = (float) logicalClock.rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDelta();
		
		RadioPacket packet = new RadioPacket(new SelfMessage9(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register32(localTime));
		MAC.sendPacket(packet);
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE
				+ ((Distribution.getRandom().nextInt() % 100) + 1) * 10000);
	}

	public Register32 local2Global() {
		return logicalClock.getValue(CLOCK.getValue());
	}

	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " "
				+ Float.floatToIntBits((float) ((1.0 + logicalClock.rate
						.getValue()) * (1.0 + CLOCK.getDrift())));
		System.out.println(""+NODE_ID+" "+(1.0+(double)logicalClock.rate.getValue())*(1.0+CLOCK.getDrift()));
//		System.out.println(""+NODE_ID+" "+logicalClock.rate.getValue());
//		
//		System.out.println(""+NODE_ID+" "+logicalClock.rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDelta());

		

		return s;
	}
}
