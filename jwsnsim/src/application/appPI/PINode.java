package application.appPI;

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
import sim.type.Register;

public class PINode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;
	private static final float MAX_PPM = 0.0001f;

	static int decreaseCount = 0;
	static int increaseCount = 0;

	LogicalClock logicalClock = new LogicalClock();

	Timer timer0;

	PIMessage outgoingMsg = new PIMessage();

	// public AvtSimple alpha = new AvtSimple(-0.0001f, 0.0001f, 0.0f,
	// 0.000000001f, 0.00001f);
	// public float alpha = 1.0f/(float)BEACON_RATE;

	public PINode(int id, Position position) {
		super(id, position);

//		CLOCK = new ConstantDriftClock();
		CLOCK = new DynamicDriftClock();

		/* to start clock with a random value */
		CLOCK.setValue(new Register(Math.abs(Distribution.getRandom().nextInt())));
		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

//		System.out.println("Node:" + this.NODE_ID + ":"
//				+ (int) (CLOCK.getDrift() * 1000000.0));
	}

	int calculateSkew(RadioPacket packet) {
		PIMessage msg = (PIMessage) packet.getPayload();

		Register neighborClock = msg.clock;
		Register myClock = logicalClock.getValue(packet.getEventTime());

		return neighborClock.subtract(myClock).toInteger();
	}
	
	private static final float BOUNDARY = 2.0f * MAX_PPM * (float) BEACON_RATE;
	float K_max = 1.0f / (float) (BEACON_RATE);
	float K_i = K_max;
	
	int previousSkew = Integer.MAX_VALUE;
	
	void updateClock(int skew,Register updateTime){
		float newK_i = K_i;
		
		if((previousSkew-skew) != 0 && previousSkew != 0.0f)
			newK_i = K_i * (float)previousSkew/(float)(previousSkew - skew);
		
		K_i = Math.abs(newK_i);
		if (K_i > K_max) K_i = K_max;

		previousSkew = skew;

		logicalClock.rate += K_i * (float) skew;
		
		int addedValue = (int) ((float) skew);
		logicalClock.setValue(logicalClock.getValue(updateTime).add(addedValue), updateTime);
	}
	
	int avgSkew = 0;
	int num = 0;
	
	private void algorithmPI(RadioPacket packet) {
		Register updateTime = packet.getEventTime();
		logicalClock.update(updateTime);

		int skew = calculateSkew(packet);
		
		/*  initial offset compensation */ 
		if(Math.abs(skew) <= BOUNDARY){	
			avgSkew += skew;
			num++;
		}
		else{
			if(skew > BOUNDARY){
				Register myClock = logicalClock.getValue(packet.getEventTime());
				logicalClock.setValue(myClock.add(skew),updateTime);
				previousSkew = 0;
				avgSkew = 0;
				num = 0;
			}			
		}
	}

	private void adjustClock(RadioPacket packet) {
		algorithmPI(packet);
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
		Register localTime, globalTime;

		localTime = CLOCK.getValue();
		if(num>0)
			updateClock(avgSkew/num, localTime);
		previousSkew = avgSkew;
		avgSkew = 0;
		num = 0;
		globalTime = logicalClock.getValue(localTime);

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = globalTime;

		RadioPacket packet = new RadioPacket(new PIMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register(localTime));
		MAC.sendPacket(packet);
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE
				+ ((Distribution.getRandom().nextInt() % 100) + 1) * 10000);
	}

	public Register local2Global() {
		return logicalClock.getValue(CLOCK.getValue());
	}

	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " "
				// + Float.floatToIntBits((float) ((1.0 + logicalClock.rate
				// .getValue()) * (1.0 + CLOCK.getDrift())));
				// System.out.println(""
				// + NODE_ID
				// + " "
				// + (1.0 + (double) logicalClock.rate.getValue())
				// * (1.0 + CLOCK.getDrift()));
				+ Float.floatToIntBits((float)logicalClock.rate);
//				+ Float.floatToIntBits((float) ((1.0 + logicalClock.rate) * (1.0 + CLOCK
//						.getDrift())));
		// + Float.floatToIntBits(K_i);
		// System.out.println(""
		// + NODE_ID
		// + " "
		// + (1.0 + (double) logicalClock.rate)
		// * (1.0 + CLOCK.getDrift()));

		return s;
	}
}
