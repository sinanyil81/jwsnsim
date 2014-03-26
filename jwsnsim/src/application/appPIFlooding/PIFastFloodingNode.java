package application.appPIFlooding;

import sim.clock.DynamicDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.radio.RadioPacket;
import sim.radio.SimpleRadio;
import sim.simulator.Simulator;
import sim.statistics.Distribution;
import sim.type.UInt32;

public class PIFastFloodingNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final float MAX_PPM = 0.0001f;
	
	int ROOT_ID = 1; // fixed root

	LogicalClock logicalClock = new LogicalClock();
	Timer timer0;

	RadioPacket processedMsg = null;
	PIFloodingMessage outgoingMsg = new PIFloodingMessage();

	public PIFastFloodingNode(int id, Position position) {
		super(id, position);

		CLOCK = new DynamicDriftClock();

		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		CLOCK.setValue(new UInt32(Math.abs(Distribution.getRandom().nextInt())));
		// System.out.println(CLOCK.getDrift());

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;
		outgoingMsg.rootid = NODE_ID;
		outgoingMsg.nodeid = NODE_ID;
	}

	int calculateSkew(RadioPacket packet) {
		PIFloodingMessage msg = (PIFloodingMessage) packet.getPayload();

		UInt32 neighborClock = msg.clock;
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		return neighborClock.subtract(myClock).toInteger();
	}

	private static final float BOUNDARY = 2.0f * MAX_PPM * (float) BEACON_RATE;
	float beta = 1.0f;
	float K_max = 1.0f / (float) (BEACON_RATE);
	float K_i = 0;

	int previousSkew = Integer.MAX_VALUE;

	private void algorithm(RadioPacket packet) {
		UInt32 updateTime = packet.getEventTime();
		logicalClock.update(updateTime);
		PIFloodingMessage msg = (PIFloodingMessage) packet.getPayload();

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

			previousSkew = 0;
			K_i = K_max;
			logicalClock.rate = 0.0f;

			return;
		}
		
		float newK_i = K_i;
		
		if((previousSkew-skew) != 0 && previousSkew != 0.0f)
			newK_i = K_i * (float)previousSkew/(float)(previousSkew - skew);
		
		K_i = Math.abs(newK_i);
		if (K_i > K_max) K_i = K_max;

		previousSkew = skew;

		logicalClock.rate += K_i * (float) skew;
		int addedValue = (int) (((float) skew) * beta);
		logicalClock.setValue(
				logicalClock.getValue(updateTime).add(addedValue), updateTime);
		
		timer0.startOneshot(1000000);
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
		UInt32 localTime, globalTime;

		localTime = CLOCK.getValue();
		globalTime = logicalClock.getValue(localTime);

		if (outgoingMsg.rootid == NODE_ID) {
			outgoingMsg.clock = new UInt32(localTime);
		} else {
			outgoingMsg.clock = new UInt32(globalTime);
		}

		RadioPacket packet = new RadioPacket(new PIFloodingMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new UInt32(localTime));
		MAC.sendPacket(packet);

		if (outgoingMsg.rootid == NODE_ID)
			++outgoingMsg.sequence;
	}

	@Override
	public void on() throws Exception {
		super.on();
		
		if(NODE_ID == ROOT_ID)
			timer0.startPeriodic(BEACON_RATE);
	}

	public UInt32 local2Global() {
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
				+ Float.floatToIntBits((float) (logicalClock.rate));
//		 + Float.floatToIntBits(K_i*100000000.0f);
		// + Float.floatToIntBits((float) (increment));//
//		if (Simulator.getInstance().getSecond() >= 10000) {
////			/* to start clock with a random value */
//			if (this.NODE_ID == 10) {
//				if (changed == false) {
//					CLOCK.setDrift(0.0001f);
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
