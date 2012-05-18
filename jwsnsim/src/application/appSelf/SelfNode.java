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
	private static final double TOLERANCE = 30.0;

	LogicalClock logicalClock = new LogicalClock();
	double criticality = 0.0;
	Timer timer0;

	RadioPacket processedMsg = null;
	SelfMessage outgoingMsg = new SelfMessage();
	Hashtable<Integer, RadioPacket> packets = new Hashtable<Integer, RadioPacket>();

	public SelfNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;
	}
	
	private int computeBestOffset(){
		int skew = 0;
		double minCriticality = criticality;  

		for (Iterator<RadioPacket> iterator = packets.values().iterator(); iterator
				.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			if(minCriticality > msg.criticality){
				UInt32 neighborClock = msg.clock;
				UInt32 myClock = logicalClock.getValue(packet.getEventTime());

				skew = myClock.subtract(neighborClock).toInteger();
				minCriticality = msg.criticality;
			}
		}
		
		return skew;
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
		
		if(num > 0){
			this.criticality =totalSkew/(double)num;
		}
		
		if(NODE_ID == 1) System.out.println("NODE "+ NODE_ID+" c:"+criticality);
	}

	void decide() {
		boolean isMostCritical = true;
		computeCriticality();

		for (Iterator<RadioPacket> iterator = packets.values().iterator(); iterator
				.hasNext();) {
			RadioPacket packet = iterator.next();
			SelfMessage msg = (SelfMessage) packet.getPayload();

			if (Math.abs(criticality) <= Math.abs(msg.criticality)) {
				isMostCritical = false;
				break;
			}
		}

		if (isMostCritical && (Math.abs(criticality) > TOLERANCE)) {
			
			if (criticality > 0.0) {
				logicalClock.rate.adjustValue(Feedback.DECREASE);
			} else if (criticality < 0.0) {
				logicalClock.rate.adjustValue(Feedback.INCREASE);
			}
			
			int skew =computeBestOffset();
			UInt32 local = CLOCK.getValue();
			logicalClock.setValue(logicalClock.getValue(local).add(skew));
			logicalClock.updateLocalTime = local;	
		} else {
			if (Math.abs(criticality) < TOLERANCE) {
				logicalClock.rate.adjustValue(Feedback.GOOD);
			}
			else{
				int skew =computeBestOffset();
				UInt32 local = CLOCK.getValue();
				logicalClock.setValue(logicalClock.getValue(local).add(skew));
				logicalClock.updateLocalTime = local;	
			}
		}
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
		s += " " + Float.floatToIntBits((float) ((1.0+logicalClock.rate.getCurrentValue()) * (1.0+CLOCK.getDrift()))) ;

		return s;
	}
}
