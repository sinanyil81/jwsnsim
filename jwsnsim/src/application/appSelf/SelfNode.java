package application.appSelf;

import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

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
import fr.irit.smac.util.adaptivevaluetracker.Feedback;

public class SelfNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final double TOLERANCE = 30.0;
	private static final double OFFSET_TOLERANCE = 60.0;

	LogicalClock logicalClock = new LogicalClock();
	double criticality = 0.0;
	Timer timer0;

	RadioPacket processedMsg = null;
	SelfMessage outgoingMsg = new SelfMessage();
	Hashtable<Integer, RadioPacket> packets = new Hashtable<Integer, RadioPacket>();
	private Feedback lastFeedback = Feedback.INCREASE;

	public SelfNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":" + CLOCK.getDrift());
	}

	private void computeCriticality() {

		double totalSkew = 0.0;
		int num = 0;

		for (Iterator<RadioPacket> iterator = packets.values().iterator(); iterator
				.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			UInt32 neighborClock = msg.clock;

			UInt32 myClock = logicalClock.getValue(packet.getEventTime());

			totalSkew += myClock.subtract(neighborClock).toDouble();

			num += 1;
		}

		double delta = 1.0;// logicalClock.rate.getCurrentDelta();
		if (num > 0) {
			this.criticality = (totalSkew / (double) num) * delta;
		}
	}

	private int findMostCriticalNeighbor() {
		int mostCriticalNeighbor = NODE_ID;
		double maxCriticality = criticality;

		for (Iterator<RadioPacket> iterator = packets.values().iterator(); iterator
				.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			if (Math.abs(maxCriticality) <= Math.abs(msg.criticality)) {
				maxCriticality = msg.criticality;
				mostCriticalNeighbor = msg.nodeid;
			}
		}

		return mostCriticalNeighbor;
	}

	private int findLeastCriticalNeighbor() {
		int leastCriticalNeighbor = NODE_ID;
		double minCriticality = criticality;

		for (Iterator<RadioPacket> iterator = packets.values().iterator(); iterator
				.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			if (Math.abs(minCriticality) >= Math.abs(msg.criticality)) {
				minCriticality = msg.criticality;
				leastCriticalNeighbor = msg.nodeid;
			}
		}

		return leastCriticalNeighbor;
	}

	void decide() {
		computeCriticality();
		int leastCriticalNeighbor = findLeastCriticalNeighbor();
		if (leastCriticalNeighbor != NODE_ID) {
			RadioPacket p = packets.get(leastCriticalNeighbor);
			SelfMessage msg = (SelfMessage) p.getPayload();

			UInt32 neighborClock = msg.clock;
			UInt32 myClock = logicalClock.getValue(p.getEventTime());
			double skew = myClock.subtract(neighborClock).toInteger();

			double control_skew = this.criticality;

			if (control_skew > TOLERANCE) {
				double exValue = logicalClock.rate.getCurrentValue();
				logicalClock.rate.adjustValue(Feedback.DECREASE);
				this.lastFeedback = Feedback.DECREASE;
				double newValue = logicalClock.rate.getCurrentValue();
				// if (newValue == exValue) {
				if (skew > (OFFSET_TOLERANCE)) {
					System.out.println("NODE:" + this.NODE_ID
							+ "***********************************");
					logicalClock.addOffset(computeBestOffset());
					//logicalClock.addOffset(computeAverageOffset());
				}
			} else if (control_skew < (-1.0) * TOLERANCE) {
				double exValue = logicalClock.rate.getCurrentValue();
				logicalClock.rate.adjustValue(Feedback.INCREASE);
				this.lastFeedback = Feedback.INCREASE;
				double newValue = logicalClock.rate.getCurrentValue();
				// if (newValue == exValue) {
				if (skew < (OFFSET_TOLERANCE)) {
					System.out.println("NODE:" + this.NODE_ID
							+ "***********************************");
					logicalClock.addOffset(computeBestOffset());
					//logicalClock.addOffset(computeAverageOffset());
				}
			} else {
				if (!this.lastFeedback.equals(Feedback.GOOD)) {
					logicalClock.rate.adjustValue(Feedback.GOOD);
					this.lastFeedback = Feedback.GOOD;
					System.out.println("GOOD GOOD GOOD");
				}
			}
		}
	}

	private int computeBestOffset() {
		int skew = 0;

		int leastCriticalNeighbor = findLeastCriticalNeighbor();
		RadioPacket packet = this.packets.get(leastCriticalNeighbor);
		SelfMessage msg = (SelfMessage) (packet.getPayload());
		UInt32 neighborClock = msg.clock;
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		skew = -myClock.subtract(neighborClock).toInteger();

		return skew;
	}
	
	private int computeAverageOffset() {
		double totalSkew = 0.0;
		int num = 0;

		for (Iterator<RadioPacket> iterator = packets.values().iterator(); iterator
				.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			UInt32 neighborClock = msg.clock;

			UInt32 myClock = logicalClock.getValue(packet.getEventTime());

			totalSkew += myClock.subtract(neighborClock).toDouble();

			num += 1;
		}

		if (num > 0) {
			totalSkew /= (double) num;
		}
		
		return (int)totalSkew; 
	}

	@Override
	public void receiveMessage(RadioPacket packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();
		packets.put(msg.nodeid, packet);

		UInt32 local = CLOCK.getValue();
		logicalClock.setValue(logicalClock.getValue(local));
		logicalClock.updateLocalTime = local;
	}

	@Override
	public void fireEvent(Timer timer) {
		decide();
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
		outgoingMsg.criticality = this.criticality;

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
						.getCurrentValue()) * (1.0 + CLOCK.getDrift())));
		if (NODE_ID < 10) {
			System.out.println("0" + NODE_ID + " t:"
					+ local2Global().toString());
		} else {
			System.out.println(NODE_ID + " t:" + local2Global().toString());
		}

		return s;
	}
}
