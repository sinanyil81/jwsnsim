package application.appSelfFlooding;

import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import fr.irit.smac.util.avt.Feedback;
import sim.clock.ConstantDriftClock;
import sim.clock.DynamicDriftClock;
import sim.node.Node;
import sim.node.NodeFactory;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.radio.RadioPacket;
import sim.radio.SimpleRadio;
import sim.simulator.Simulator;
import sim.statistics.Distribution;
import sim.topology.Grid2D;

public class SelfFloodingNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;  
	private static final int TOLERANCE = 1;

	LogicalClock logicalClock = new LogicalClock();
	Timer timer0;

	RadioPacket processedMsg = null;
	SelfFloodingMessage outgoingMsg = new SelfFloodingMessage();
    
	public SelfFloodingNode(int id, Position position) {
		super(id, position);

//		CLOCK = new ConstantDriftClock();
		CLOCK = new DynamicDriftClock();
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);
		
		/* to start clock with a random value */
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));
		
	
		outgoingMsg.sequence = 0;
		outgoingMsg.rootid = NODE_ID;
		outgoingMsg.nodeid = NODE_ID;
	}
	
	int calculateSkew(RadioPacket packet) {
		SelfFloodingMessage msg = (SelfFloodingMessage) packet.getPayload();

		Register32 neighborClock = msg.clock;
		Register32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toInteger();
	}
	

	private void adjustClock(RadioPacket packet) {
		logicalClock.update(packet.getEventTime());
		
		SelfFloodingMessage msg = (SelfFloodingMessage)packet.getPayload();

		if( msg.rootid < outgoingMsg.rootid) {
			outgoingMsg.rootid = msg.rootid;
			outgoingMsg.sequence = msg.sequence;
		} else if (outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;
		}
		else {
			return;
		}
	
		int skew = calculateSkew(packet);
		logicalClock.setValue(msg.clock, packet.getEventTime());
		
		if (skew > TOLERANCE) {
//			logicalClock.rate.adjustValue(Feedback.LOWER);
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
		} else if (skew < -TOLERANCE) {
//			logicalClock.rate.adjustValue(Feedback.GREATER);
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
		} else {
//			logicalClock.rate.adjustValue(Feedback.GOOD);
			logicalClock.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
		}
	}
	
	void processMsg() {
		adjustClock(processedMsg);
	}

	@Override
	public void receiveMessage(RadioPacket packet) {
		processedMsg = packet;
		processMsg();
	}

	@Override
	public void fireEvent(Timer timer) {
		sendMsg();
	}

	private void sendMsg() {
		Register32 localTime, globalTime;
		
		localTime = CLOCK.getValue();
		globalTime = logicalClock.getValue(localTime);
		
		if( outgoingMsg.rootid == NODE_ID ) {
			outgoingMsg.clock = new Register32(localTime);
		}
		else{
			outgoingMsg.clock = new Register32(globalTime);	
		}
		
		RadioPacket packet = new RadioPacket(new SelfFloodingMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register32(localTime));
		MAC.sendPacket(packet);	
		
		if (outgoingMsg.rootid == NODE_ID)
			++outgoingMsg.sequence;
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE+((Distribution.getRandom().nextInt() % 100) + 1)*10000);
	}

	public Register32 local2Global() {
		return logicalClock.getValue(CLOCK.getValue());
	}

	boolean changed = false;
	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " "
				+ Float.floatToIntBits((float) logicalClock.rate.getValue());
//				+ Float.floatToIntBits((float) ((1.0 + logicalClock.rate
//						.getValue()) * (1.0 + CLOCK.getDrift())));
				
		
//		if(Simulator.getInstance().getSecond()>=100000)
//		{
//			/* to start clock with a random value */
//			if(this.NODE_ID == 1){
//				if(changed == false){
//					CLOCK.setDrift(0.0001f);
//					changed = true;
//				}				
//			}
//		}
//		System.out.println("" + NODE_ID + " "
//				+ (1.0 + (double) logicalClock.rate.getValue())
//				* (1.0 + CLOCK.getDrift()));

		return s;
	}
}
