package application.appSelf;

import java.util.Vector;

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
import fr.irit.smac.util.avt.Feedback;

public class SelfNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final double TOLERANCE = 1.0;

	LogicalClock logicalClock = new LogicalClock();

	Timer timer0;

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

		System.out.println("Node:" + this.NODE_ID + ":"
				+ (int) (CLOCK.getDrift() * 1000000.0));
	}

	double calculateSkew(RadioPacket packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();

		UInt32 neighborClock = msg.clock;
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toDouble();
	}

	private void adjustClock(RadioPacket packet) {
		SelfMessage msg = (SelfMessage)packet.getPayload();
		logicalClock.update(packet.getEventTime());

		double skew = calculateSkew(packet);

		// aynı anda başladıklarında bir dahaki mesaja kadar aralarında
		// olabilecek en fazla saat farkını hesapla
		double threshold = 0.0002 * BEACON_RATE;

		if (skew < -threshold) {
			logicalClock.setValue(msg.clock, packet.getEventTime());
		} else if (skew > threshold) {
			// do nothing
		} else if (skew > TOLERANCE) {
			 logicalClock.rate.adjustValue(Feedback.LOWER);
//			 logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
		} else if (skew < (-1.0) * TOLERANCE) {
			logicalClock.rate.adjustValue(Feedback.GREATER);
//			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
			logicalClock.setValue(msg.clock, packet.getEventTime());
		} else {
			logicalClock.rate.adjustValue(Feedback.GOOD);
//			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
		}
	}

	private void adjustOffset(double skew) {
		UInt32 offset = logicalClock.getOffset();
		offset = offset.add((int) -(skew * 1.0));
		logicalClock.setOffset(offset);
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
//		System.out.println("" + NODE_ID + " "
//				+ (1.0 + (double) logicalClock.rate.getValue())
//				* (1.0 + CLOCK.getDrift()));

		return s;
	}
}
