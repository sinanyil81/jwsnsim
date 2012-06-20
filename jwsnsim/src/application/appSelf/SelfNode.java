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
import fr.irit.smac.util.adaptivevaluetracker.Feedback;

public class SelfNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final double TOLERANCE = 10;

	LogicalClock logicalClock = new LogicalClock();	
	Timer timer0;

	RadioPacket processedMsg = null;
	SelfMessage outgoingMsg = new SelfMessage();
	
	Hashtable<Integer, RadioPacket> packets = new Hashtable<Integer, RadioPacket>();
	double criticality = 0.0;

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
		double skewToTheFastestNeighbor = 0.0;
		double skewToTheSlowestNeighbor = 0.0;		
		
		for (Iterator<RadioPacket> iterator = packets.values()
				.iterator(); iterator.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			UInt32 neighborClock = msg.clock;
			UInt32 myClock = logicalClock.getValue(packet.getEventTime());
			
			double skew = myClock.subtract(neighborClock).toDouble();
			
			if(skew > 0.0 && skew > skewToTheSlowestNeighbor){
				skewToTheSlowestNeighbor = skew;
			}
			
			if(skew < 0.0 && skew < skewToTheFastestNeighbor){
				skewToTheFastestNeighbor = skew;
			}
		}
		
		this.criticality = skewToTheFastestNeighbor + skewToTheSlowestNeighbor;
	}
	
	void adjustRate() {
		computeCriticality();

		if (this.criticality > TOLERANCE) { /* we are too much ahead from the slowest */
			logicalClock.rate.adjustValue(Feedback.DECREASE);
		} else if (this.criticality < (-1.0) * TOLERANCE) { /* we are too much behind from the fastest */
			logicalClock.rate.adjustValue(Feedback.INCREASE);
		} else {
			logicalClock.rate.adjustValue(Feedback.GOOD);
		}		
	}
	

	private int computeAverageOffset(){
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

	private void adjustOffset() {
		int averageOffset = computeAverageOffset();
		logicalClock.addOffset(averageOffset);		
	}

	private void updateLogicalClock() {
		UInt32 local = CLOCK.getValue();
		logicalClock.setValue(logicalClock.getValue(local));
		logicalClock.updateLocalTime = local;
	}

	@Override
	public void receiveMessage(RadioPacket packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();
		packets.put(msg.nodeid, packet);

		updateLogicalClock();
	}

	@Override
	public void fireEvent(Timer timer) {		
		updateLogicalClock();
		adjustRate();
		adjustOffset();
		packets.clear();
		
		sendMsg();
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
						.getCurrentValue()) * (1.0 + CLOCK.getDrift())));
//		if (NODE_ID < 10) {
//			System.out.println("0" + NODE_ID + " t:"
//					+ local2Global().toString());
//		} else {
//			System.out.println(NODE_ID + " t:" + local2Global().toString());
//		}

		return s;
	}
}
