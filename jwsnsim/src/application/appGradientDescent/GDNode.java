package application.appGradientDescent;

import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;

import java.security.acl.LastOwnerException;

import sim.clock.ConstantDriftClock;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.radio.RadioPacket;
import sim.radio.SimpleRadio;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class GDNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final float MAX_PPM = 0.0001f;

	LogicalClock logicalClock = new LogicalClock();
	Timer timer0;

	RadioPacket processedMsg = null;
	GDMessage outgoingMsg = new GDMessage();

	public GDNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();

		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		if (this.NODE_ID == 1)
			CLOCK.setDrift(0.0f);
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));
		// System.out.println(CLOCK.getDrift());

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;
		outgoingMsg.rootid = NODE_ID;
		outgoingMsg.nodeid = NODE_ID;
	}

	int calculateSkew(RadioPacket packet) {
		GDMessage msg = (GDMessage) packet.getPayload();

		Register32 neighborClock = msg.clock;
		Register32 myClock = logicalClock.getValue(packet.getEventTime());

		return neighborClock.subtract(myClock).toInteger();
	}

	private static final float BOUNDARY = 2.0f * MAX_PPM * (float) BEACON_RATE;

	Register32 lastEvent; 
	int lastSkew;
	float lastDerivative; 
	float alpha = 1.0f;
	
	private void algorithm(RadioPacket packet) {
		Register32 updateTime = packet.getEventTime();

		GDMessage msg = (GDMessage) packet.getPayload();

		if (msg.rootid < outgoingMsg.rootid) {
			outgoingMsg.rootid = msg.rootid;
			outgoingMsg.sequence = msg.sequence;
		} else if (outgoingMsg.rootid == msg.rootid
				&& (msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;
		} else {
			return;
		}

		int skew = calculateSkew(packet);

		if (Math.abs(skew) > BOUNDARY) {
			logicalClock.setValue(logicalClock.getValue(updateTime).add(skew),
					updateTime);
			logicalClock.rate = 0.0f;
			lastEvent = new Register32(updateTime);
			lastSkew = 0;
			alpha = 1.0f;
			lastDerivative = 0.0f;

			return;
		}
		
		int elapsed = updateTime.subtract(lastEvent).toInteger();
		
//		if(skew != 0 && lastSkew != 0)
//			alpha *= (float)lastSkew/(float)(lastSkew-skew);

		float derivative = (float) (skew) / (float) elapsed;
			
		if(Math.signum(derivative) == Math.signum(lastDerivative)){
			alpha *= 2.0f;			
		}
		else{
			alpha /=3.0f;
		}
		
		if (alpha > 1.0f) alpha = 1.0f;		
		if(alpha < 0.0000000001f) alpha = 0.0000000001f;
		
		lastEvent = new Register32(updateTime);
		lastSkew = skew;
		lastDerivative = derivative;
				
		logicalClock.rate += alpha*derivative;
		logicalClock.setValue(((GDMessage) packet.getPayload()).clock, updateTime);
	}

	void processMsg() {
		algorithm(processedMsg);
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

		if (outgoingMsg.rootid == NODE_ID) {
			outgoingMsg.clock = new Register32(localTime);
		} else {
			outgoingMsg.clock = new Register32(globalTime);
		}

		RadioPacket packet = new RadioPacket(new GDMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register32(localTime));
		MAC.sendPacket(packet);

		if (outgoingMsg.rootid == NODE_ID)
			++outgoingMsg.sequence;
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

	boolean changed = false;

	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " "
//				+ Float.floatToIntBits((float) ((1.0 + logicalClock.rate) * (1.0 + CLOCK
//						.getDrift())));
				+ Float.floatToIntBits((float) logicalClock.rate);
		// + Float.floatToIntBits(alpha);
		// + Float.floatToIntBits((float) (increment));//
//		if (Simulator.getInstance().getSecond() >= 5000) {
//			// /* to start clock with a random value */
//			if (this.NODE_ID == 18) {
//				if (changed == false) {
//					CLOCK.setDrift(-0.00005f);
//					changed = true;
//				}
//			}
//		}
		// }
		// }
		// + Float.floatToIntBits(K_i);
		// System.out.println("" + NODE_ID + " "
		// + (1.0 + (double) logicalClock.rate)
		// * (1.0 + CLOCK.getDrift()));

		return s;
	}
}
