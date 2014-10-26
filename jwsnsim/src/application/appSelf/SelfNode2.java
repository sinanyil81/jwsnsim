package application.appSelf;

import nodes.CSMA;
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

public class SelfNode2 extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;

	LogicalClock2 logicalClock = new LogicalClock2();
	Timer timer0;

	SelfMessage outgoingMsg = new SelfMessage();
	ClockSpeedAdapter speedAdapter = new ClockSpeedAdapter();

	public SelfNode2(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();

		/* to start clock with a random value */
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));

		MAC = new CSMA(this);
		RADIO = new Transceiver(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

		System.out.println("Node:" + this.NODE_ID + ":" + (int)(CLOCK.getDrift()*1000000.0));
	}

	private void adjustClockSpeed(Packet packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();
		speedAdapter.adjust(msg.nodeid, msg.hardwareClock, packet.getEventTime(), msg.rateMultiplier);
		logicalClock.rate = speedAdapter.getSpeed();
	}
	
	private void adjustClockOffset(Packet packet) {
		SelfMessage msg = (SelfMessage) packet.getPayload();
		
		if(msg.sequence > outgoingMsg.sequence){
			
			logicalClock.setValue(msg.clock, packet.getEventTime());
			outgoingMsg.sequence = msg.sequence;
		}		
	}

	@Override
	public void receiveMessage(Packet packet) {
		adjustClockSpeed(packet);
		adjustClockOffset(packet);
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
		
		if(this.NODE_ID == 1){
			logicalClock.setValue(globalTime, localTime);
			outgoingMsg.sequence++;
		}

		outgoingMsg.hardwareClock = new Register32(localTime);
		outgoingMsg.rateMultiplier = logicalClock.rate;

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

	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " "
				+ Float.floatToIntBits((float) ((1.0 + logicalClock.rate) * (1.0 + CLOCK.getDrift())));
		
//		s += " "
//				+ Float.floatToIntBits((float) speedAdapter.rate.getAdvancedAVT().getDeltaManager().getDelta());
		
		return s;
	}
}
