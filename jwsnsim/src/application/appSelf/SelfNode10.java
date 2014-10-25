package application.appSelf;

import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import hardware.transceiver.Packet;
import hardware.transceiver.Transceiver;
import fr.irit.smac.util.avt.Feedback;
import sim.clock.ConstantDriftClock;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class SelfNode10 extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	
	private static final int TOLERANCE = 0;

	LogicalClock9 logicalClock = new LogicalClock9();
	Timer timer0;
	
	SelfMessage9 outgoingMsg = new SelfMessage9();

	public SelfNode10(int id, Position position) {
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

	int calculateSkew(Packet packet) {
		SelfMessage9 msg = (SelfMessage9) packet.getPayload();

		Register32 neighborClock = msg.clock;
		Register32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toInteger();
	}

	private void adjustClock(Packet packet) {
		SelfMessage9 msg = (SelfMessage9) packet.getPayload();
		
		int skew = calculateSkew(packet);
		
		if(skew < -1000){
			logicalClock.setValue(msg.clock, packet.getEventTime());
			
			return;
		}
		else{
			skew /= 2;
			int skewRest = skew % 2;
			skew += skewRest/2;
			Register32 offset = logicalClock.getOffset();
			offset = offset.add(-skew);
			logicalClock.setOffset(offset);
		}
		
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

	@Override
	public void receiveMessage(Packet packet) {
		logicalClock.update(packet.getEventTime());	
		adjustClock(packet);
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
		
//		adjustClock();

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = globalTime;
		outgoingMsg.offset = logicalClock.getOffset();
		outgoingMsg.sequence++;		
		
		Packet packet = new Packet(new SelfMessage9(outgoingMsg));
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
//		System.out.println(rootRate);

		

		return s;
	}
}
