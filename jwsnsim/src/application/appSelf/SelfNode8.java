package application.appSelf;

import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import hardware.transceiver.Packet;
import hardware.transceiver.Transceiver;

import java.util.Hashtable;
import java.util.Iterator;

import application.appSelf.ClockSpeedAdapter7.NeighborData;
import sim.clock.ConstantDriftClock;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class SelfNode8 extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final double TOLERANCE = 1.0;

	LogicalClock8 logicalClock = new LogicalClock8();
	Timer timer0;
	
	class NeighborData {
		public Register32 clock;
		public Register32 hwclock;		
		public float speed;
		public Register32 timestamp;

		public NeighborData( Register32 clock,Register32 hwclock,float speed,Register32 timestamp) {
			this.timestamp = new Register32(timestamp);
			this.speed = speed;
			this.clock = new Register32(clock);
			this.hwclock = new Register32(hwclock);
		}
	}
	
	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();

//	public AVT skew_multiplier = new AVTBuilder().upperBound(1.00)
//			.lowerBound(0.50).startValue(0.95).isDeterministicDelta(true)
//			.deltaMin(0.05)
//			.deltaMax(0.20)
//			.build();
	
	public AvtSimple skew_multiplier = new AvtSimple(0.50f, 1.0f, 0.95f, 0.05f, 0.20f); 
	
	public OffsetAvt offsetAvt = new OffsetAvt(1.0f,200000.0f);

	private int previousSkewPositive = 0;	

	SelfMessage outgoingMsg = new SelfMessage();
	Averager averager = new Averager();

	public SelfNode8(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		
		/* to start clock with a random value */
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));
		
		MAC = new MicaMac(this);
		RADIO = new Transceiver(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":" + (int)(CLOCK.getDrift()*1000000.0));
	}

	double calculateSkew(Packet packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();

		Register32 neighborClock = msg.clock;
		Register32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toDouble();
	}

	private float getNeighborSpeed(int nodeid,Register32 neighborTimestamp, Register32 timestamp,float rate) {

		float speed = 0.0f;

		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			
			int neighborHardwareProgress = neighbor.hwclock.subtract(neighborTimestamp).toInteger();			
			int myHardwareProgress = timestamp.subtract(neighbor.timestamp).toInteger();
			
			float relativeHardwareClockRate = (float)(neighborHardwareProgress - myHardwareProgress)/(float)(myHardwareProgress);
			
			speed = relativeHardwareClockRate + relativeHardwareClockRate*rate + rate;
		}
		
		return speed;
	}
	
	private void updateNeighbor(Packet packet){
		SelfMessage msg = (SelfMessage)packet.getPayload();
		
		float neighborSpeed = getNeighborSpeed(msg.nodeid, msg.hardwareClock, packet.getEventTime(), msg.rateMultiplier);
		
		neighbors.remove(msg.nodeid);
		neighbors.put(msg.nodeid, new NeighborData(msg.clock,msg.hardwareClock,neighborSpeed,packet.getEventTime()));
	}
	
	public Register32 getNeighborClock(int id,Register32 currentTime){
		NeighborData n = neighbors.get(id);	
		
		int timePassed = currentTime.subtract(n.timestamp).toInteger();	
		int  progress = timePassed +  (int) (n.speed * (float)timePassed);
		
		return n.clock.add(new Register32(progress));
	}
	
	public Register32 getOffset(Register32 time,Register32 localTime){
		//UInt32 offset = logicalClock.getOffset();
		Register32 offset = new Register32();
		
		int diff = 0;
		
		for (Iterator<Integer> iterator = neighbors.keySet().iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			
			Register32 nclock = getNeighborClock(id,localTime);
			diff = nclock.subtract(time).toInteger();
			if(Math.abs(diff) <= 500)
				offset = offset.add(diff/2);					
		}		
		
		return offset.add(logicalClock.getOffset());
	}
	
	private void adjustClock(Packet packet) {

		logicalClock.update(packet.getEventTime());
		updateNeighbor(packet);	

		double skew = calculateSkew(packet);
		
		Register32 time = logicalClock.getValue(packet.getEventTime());			
		Register32 offset = getOffset(time,packet.getEventTime());

		if (skew > TOLERANCE) {
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
//			adjustOffset(skew);
		} else if (skew < (-1.0) * TOLERANCE) {
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
//			adjustOffset(skew);
		} else {
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
		}
		
		logicalClock.setOffset(offset);	

		// }
	}
	
//	private void adjustOffset(){
//		
//		double skew = averager.getAverage();
//
//		/* update logical clock  offset*/
//		UInt32 offset = logicalClock.getOffset();
//		offset = offset.add((int) -skew);
//		logicalClock.setOffset(offset);
//		
//		averager = new Averager();
//	}
		
//	private void adjustOffset(double skew) {
//		averager.update(skew);
//		double newSkew = skew;
//		
//		if(averager.getElementCount()>0){
//			newSkew = averager.getAverage();
//			averager.update(skew);
//			newSkew = averager.getAverage() - newSkew;			
//		}
//		else{
//			averager.update(skew);
//		}
//
//			
////		if (skew < 0){
//			System.out.println(this.NODE_ID + " " +  skew);
//			UInt32 offset = logicalClock.getOffset();
//			offset = offset.add((int)newSkew);		
//			logicalClock.setOffset(offset);
////		}
//	}
	
//	private void adjustOffset(double skew) {
//		averager.update(skew);
//			
//		UInt32 offset = logicalClock.getOffset();
//		offset = offset.add(-(int)(averager.getAverage()*0.5));		
//		logicalClock.setOffset(offset);
//	
//	}
	
//	private void adjustOffset(double skew) {
//		UInt32 offset = logicalClock.getOffset();
//		offset = offset.add((int) -(skew *0.5));
//		logicalClock.setOffset(offset);	
//	}
	
	int errCount = 0;

	private void adjustOffset(double skew) {
		
		if(skew < -100000 && (++errCount == 5)){
			Register32 offset = logicalClock.getOffset();
			offset = offset.add((int) -skew);
			logicalClock.setOffset(offset);
			logicalClock.rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.0000000001f, 0.0001f);
			skew_multiplier = new AvtSimple(0.50f, 1.0f, 0.95f, 0.5f, 0.20f);
			errCount = 0;
			
			return;
		}
		else{
			Register32 offset = logicalClock.getOffset();
			offset = offset.add((int) -(skew * skew_multiplier.getValue()));
//			offset = offset.add((int) -(skew * 0.5));
			logicalClock.setOffset(offset);		
		}
		
		//
		if (previousSkewPositive == 0) {
			if (skew > 0.0) {
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_GREATER);
				previousSkewPositive = 1;
			} else if (skew < 0.0) {
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_GREATER);
				previousSkewPositive = -1;
			} else {
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_GOOD);
				previousSkewPositive = 0;
			}
		} else if (previousSkewPositive == 1) {
			if (skew > 0.0) { // positive
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_GREATER);
				previousSkewPositive = 1;
			} else if (skew < 0.0) {
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_LOWER);
				previousSkewPositive = -1;
			} else {
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_GOOD);
				previousSkewPositive = 0;
			}
		} else if (previousSkewPositive == -1) {
			if (skew > 0.0) { // positive
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_LOWER);
				previousSkewPositive = 1;
			} else if (skew < 0.0) { // negative
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_GREATER);
				previousSkewPositive = -1;
			} else {
				skew_multiplier.adjustValue(AvtSimple.FEEDBACK_GOOD);
				previousSkewPositive = 0;
			}
		}
	}

	@Override
	public void receiveMessage(Packet packet) {
		adjustClock(packet);		
	}

	@Override
	public void fireEvent(Timer timer) {
		sendMsg();
	}

	private void sendMsg() {
		Register32 localTime, globalTime;

//		adjustOffset();
		
		localTime = CLOCK.getValue();
		globalTime = logicalClock.getValue(localTime);

		outgoingMsg.nodeid = NODE_ID;		
		outgoingMsg.clock = globalTime;		
		outgoingMsg.rateMultiplier = logicalClock.rate.getValue();
		outgoingMsg.hardwareClock = localTime;
		
		outgoingMsg.offset = logicalClock.getOffset();
		outgoingMsg.sequence++;

		Packet packet = new Packet(new SelfMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register32(localTime));
		MAC.sendPacket(packet);
		
		averager = new Averager();
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

		

		return s;
	}
}
