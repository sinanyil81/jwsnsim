package application.appSelf;

import java.util.Hashtable;

import fr.irit.smac.util.avt.Feedback;

import application.appSelf.SelfNode8.NeighborData;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.radio.RadioPacket;
import sim.radio.SimpleRadio;
import sim.simulator.Simulator;
import sim.type.UInt32;

public class SelfNode9 extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	
	private static final int TOLERANCE = 1;

	LogicalClock9 logicalClock = new LogicalClock9();
	Timer timer0;
	
	SelfMessage9 outgoingMsg = new SelfMessage9();
	Averager averager = new Averager();
	
	class NeighborData {
		public int id = -1;
		int skew;
	}
	
	NeighborData fastestNeighbor = new NeighborData();
	NeighborData slowestNeighbor = new NeighborData();

	public SelfNode9(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		
		/* to start clock with a random value */
		CLOCK.setValue(new UInt32(Math.abs(Simulator.random.nextInt())));
		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":" + (int)(CLOCK.getDrift()*1000000.0));
	}

	int calculateSkew(RadioPacket packet) {
		SelfMessage9 msg = (SelfMessage9) packet.getPayload();

		UInt32 neighborClock = msg.clock;
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toInteger();
	}

	private void adjustClock() {
		
		if(update == true){
			adjustOffset();
			adjustSpeed();
		}
		else{
//			logicalClock.resetRate();
		}
		
		fastestNeighbor = new NeighborData();
		slowestNeighbor = new NeighborData();
		
		update = true;
	}
	
	private void adjustSpeed(){

		int skew = (slowestNeighbor.skew + fastestNeighbor.skew)/2;
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

	private int adjustOffset() {		
		int skew = (slowestNeighbor.skew + fastestNeighbor.skew)/2;
		
		UInt32 offset = logicalClock.getOffset();
		offset = offset.add(-skew);
		logicalClock.setOffset(offset);

		return skew;
	}
	
	
	boolean update = true;

	private void updateLocalInfo(RadioPacket packet) {
		SelfMessage9 msg = (SelfMessage9) packet.getPayload();
		
		int skew = calculateSkew(packet);
		
		if(skew > -1000){
			if(fastestNeighbor.id == -1){
				fastestNeighbor.id = msg.nodeid;
				fastestNeighbor.skew = skew;
			}
			else if(skew < fastestNeighbor.skew || fastestNeighbor.id == msg.nodeid){
				fastestNeighbor.id = msg.nodeid;
				fastestNeighbor.skew = skew;
			}
			
			if(slowestNeighbor.id == -1){
				slowestNeighbor.id = msg.nodeid;
				slowestNeighbor.skew = skew;
			}
			else if(skew > slowestNeighbor.skew || slowestNeighbor.id == msg.nodeid){
				slowestNeighbor.id = msg.nodeid;
				slowestNeighbor.skew = skew;
			}			
		}
		else{			
			logicalClock.setValue(msg.clock, packet.getEventTime());
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
		UInt32 localTime, globalTime;
		
		localTime = CLOCK.getValue();
		
		logicalClock.update(localTime);		
		globalTime = logicalClock.getValue(localTime);
		
		adjustClock();

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = globalTime;
		outgoingMsg.offset = logicalClock.getOffset();
		outgoingMsg.sequence++;

		RadioPacket packet = new RadioPacket(new SelfMessage9(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new UInt32(localTime));
		MAC.sendPacket(packet);
		
		averager = new Averager();
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE
				+ ((Simulator.random.nextInt() % 100) + 1) * 10000);
	}

	public UInt32 local2Global() {
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

		

		return s;
	}
}
