package application.appSelf;

import nodes.MicaMac;
import nodes.Node;
import nodes.Position;
import core.Simulator;
import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import hardware.transceiver.Packet;
import hardware.transceiver.Transceiver;
import sim.clock.ConstantDriftClock;
import sim.statistics.Distribution;
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
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));

		MAC = new MicaMac(this);
		RADIO = new Transceiver(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":"
				+ (int) (CLOCK.getDrift() * 1000000.0));
	}

	double calculateSkew(Packet packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();

		Register32 neighborClock = msg.clock;
		Register32 myClock = logicalClock.getValue(packet.getEventTime());

		return myClock.subtract(neighborClock).toDouble();
	}

	private void adjustClock(Packet packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();
		logicalClock.update(packet.getEventTime());

		double skew = calculateSkew(packet);		

		adjustRate(skew);

		adjustOffset(skew / 2);
	}

	private void adjustRate(double skew) {
		// aynı anda başladıklarında bir dahaki mesaja kadar aralarında
		// olabilecek en fazla saat farkını hesapla				
		double threshold = 0.0002 * BEACON_RATE;		

		if (skew < -threshold) {
			// adjustOffset(skew);
		} else if (skew > threshold) {
			// do nothing
		} else if (skew > TOLERANCE) {
			logicalClock.rate.adjustValue(Feedback.LOWER);
		} else if (skew < (-1.0) * TOLERANCE) {
			logicalClock.rate.adjustValue(Feedback.GREATER);
		} else {
			logicalClock.rate.adjustValue(Feedback.GOOD);
		}
	}

	private void adjustOffset(double skew) {
		Register32 offset = logicalClock.getOffset();
		offset = offset.add((int) -(skew * 1.0));
		logicalClock.setOffset(offset);
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

		localTime = CLOCK.getValue();
		globalTime = logicalClock.getValue(localTime);

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = globalTime;
		outgoingMsg.offset = logicalClock.getOffset();
		outgoingMsg.sequence++;

		double delta = logicalClock.rate.getAdvancedAVT().getDeltaManager()
				.getDelta();
		Packet packet = new Packet(new SelfMessage(outgoingMsg));
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

	boolean on = false;
	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();	

		if(Simulator.getInstance().getSecond() > 150000){
			if( (this.NODE_ID == 50) && (on == false)){
				try {
					on = true;
					this.on();					
				} catch (Exception e) {
					
				}
			}
			
		}
		
		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " " + Float.floatToIntBits((float) ((1.0 + logicalClock.rate
						.getValue()) * (1.0 + CLOCK.getDrift())));
//		s += " " + Float.floatToIntBits((float) (logicalClock.rate
//				.getAdvancedAVT().getDeltaManager().getDelta()));
		// System.out.println("" + NODE_ID + " "
		// + (1.0 + (double) logicalClock.rate.getValue())
		// * (1.0 + CLOCK.getDrift()));

		return s;
	}
}
