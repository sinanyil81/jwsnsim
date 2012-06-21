package application.appSelf;

import java.util.Hashtable;
import java.util.Iterator;

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

	RadioPacket processedMsg = null;
	SelfMessage outgoingMsg = new SelfMessage();
	
	double criticality = 0.0;	
	Hashtable<Integer, RadioPacket> packets = new Hashtable<Integer, RadioPacket>();

	public SelfNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":" + CLOCK.getDrift());
	}

	private void computeCriticality(RadioPacket packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();

		UInt32 neighborClock = msg.clock;
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		this.criticality = myClock.subtract(neighborClock).toDouble();
	}

	/**
	 * This method is only called when a message received.
	 */
	void decide(RadioPacket packet) {
		computeCriticality(packet);
		
		/* update logical clock before changing its rate 
		 * or offset!!!
		 */
		logicalClock.update(CLOCK.getValue());
		
		double control_skew = this.criticality;
		if (control_skew > TOLERANCE) {		
			logicalClock.rate.adjustValue(Feedback.LOWER);
			logicalClock.addOffset(computeAverageOffset());
		} else if (control_skew < (-1.0) * TOLERANCE) {
			logicalClock.rate.adjustValue(Feedback.GREATER);
			logicalClock.addOffset(computeAverageOffset());
		} else {
			logicalClock.rate.adjustValue(Feedback.GOOD);
		}
	}

	private int computeAverageOffset() {
		int averageSkew = 0;

		double totalSkew = 0.0;
		int num = 0;

		for (Iterator<RadioPacket> iterator = packets.values()
				.iterator(); iterator.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			UInt32 neighborClock = msg.clock;

			UInt32 myClock = logicalClock.getValue(packet.getEventTime());

			totalSkew += myClock.subtract(neighborClock).toDouble();

			num += 1;
		}

		if (num > 0) {
			averageSkew = (int) (totalSkew / (double) num);
		}

		return -averageSkew;
	}
		
	@Override
	public void receiveMessage(RadioPacket packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();
		packets.put(msg.nodeid, packet);
		
		decide(packet);
	}

	@Override
	public void fireEvent(Timer timer) {
		sendMsg();
		packets.clear();
	}

	private void sendMsg() {
		UInt32 localTime, globalTime;

		localTime = CLOCK.getValue();
		globalTime = logicalClock.getValue(localTime);

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = globalTime;
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
		String s = Simulator.getInstance().getSecond().toString(10);

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " "
				+ Float.floatToIntBits((float) ((1.0 + logicalClock.rate
						.getValue()) * (1.0 + CLOCK.getDrift())));

		return s;
	}
}
