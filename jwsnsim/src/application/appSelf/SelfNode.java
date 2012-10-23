package application.appSelf;

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

public class SelfNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final double TOLERANCE = 1.0;

	LogicalClock logicalClock = new LogicalClock();
	Timer timer0;

//	public AVT skew_multiplier = new AVTBuilder().upperBound(1.00)
//			.lowerBound(0.50).startValue(0.95).isDeterministicDelta(true)
//			.deltaMin(0.05)
//			.deltaMax(0.20)
//			.build();
	
	public AvtSimple skew_multiplier = new AvtSimple(0.50f, 1.0f, 0.95f, 0.05f, 0.20f); 

	private int previousSkewPositive = 0;	

	SelfMessage outgoingMsg = new SelfMessage();

	public SelfNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		
		/* to start clock with a random value */
		CLOCK.setValue(new UInt32(Math.abs(Simulator.random.nextInt())));
		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":" + CLOCK.getDrift());
	}

	double calculateSkew(RadioPacket packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();

		UInt32 neighborClock = msg.clock;
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toDouble();
	}

	private void adjustClock(RadioPacket packet) {

		double skew = calculateSkew(packet);	
		
		logicalClock.update(packet.getEventTime());

		if (skew > TOLERANCE) {
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
			adjustOffset(skew);
		} else if (skew < (-1.0) * TOLERANCE) {
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
			adjustOffset(skew);
		} else {
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
		}
	}
	
//	private void adjustOffset(double skew) {
//		UInt32 offset = logicalClock.getOffset();
//		offset = offset.add((int) -(skew *0.5));
//		logicalClock.setOffset(offset);	
//	}

	private void adjustOffset(double skew) {
									
		UInt32 offset = logicalClock.getOffset();
		offset = offset.add((int) -(skew * skew_multiplier.getValue()));
		logicalClock.setOffset(offset);
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
	public void receiveMessage(RadioPacket packet) {
		adjustClock(packet);		
	}

	@Override
	public void fireEvent(Timer timer) {
		sendMsg();
	}

	private void sendMsg() {
		UInt32 localTime, globalTime;

		localTime = CLOCK.getValue();
		globalTime = logicalClock.getValue(localTime);

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = globalTime;
		outgoingMsg.offset = logicalClock.getOffset();
		outgoingMsg.sequence++;

		RadioPacket packet = new RadioPacket(new SelfMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new UInt32(localTime));
		MAC.sendPacket(packet);
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
		
		

		return s;
	}
}
