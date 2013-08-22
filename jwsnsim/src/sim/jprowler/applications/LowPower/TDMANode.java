package sim.jprowler.applications.LowPower;

import sim.jprowler.Node;
import sim.jprowler.UInt32;
import sim.jprowler.applications.LowPower.TDMASchedule4x4Grid.Schedule;
import sim.jprowler.applications.LowPower.TDMASchedule4x4Grid.ScheduleType;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;
import sim.jprowler.mac.MacListener;
import sim.jprowler.mac.NonCSMAMac;
import sim.jprowler.radio.RadioPacket;
import sim.jprowler.radio.RadioProcessor;

public class TDMANode implements TimerHandler,MacListener {

	public TimeSync synchronizer = new TimeSync(4);
	public TDMASchedule4x4Grid schedule = null;
	private Timer transmitTimer = null;

	boolean isSleepingAllowed = false;

	RadioPacket scheduledPacket = null;
	Schedule currentSchedule = null;
	
	Node node;
	NonCSMAMac mac;
	RadioStats stats;

	public TDMANode(Node node,NonCSMAMac mac) {
		this.mac = mac;
		mac.addListener(this);
		
		this.node = node;
		node.turnOn();
		transmitTimer = new Timer(node.getClock(), this);
		start();
		
		stats = new RadioStats(this);
		stats.on();
	}

	public void start() {

		schedule = new TDMASchedule4x4Grid(node);
		schedule.reschedule(node.getClock().getValue(), 0.0f);
		currentSchedule = schedule.getTransmissionSchedule();
		transmitTimer.startOneshot(currentSchedule.time.toInteger());
	}

	@Override
	public void fireEvent(Timer timer) {
		if (timer == transmitTimer) {
			
			switch (currentSchedule.type) {
			case SEND:
				scheduledPacket = new RadioPacket(null); 
				insertLogicalClockAndTimestampPacketToSend();
				mac.sendPacket(scheduledPacket, isSleepingAllowed);

				nextAction();
				stats.startEpoch();
				break;

			case RECEIVE:
				mac.wakeUp(generateGuardTime());
				nextAction();
				break;

			default:
				break;
			}
		}
	}

	private final static int FIXED_GUARD = 2;
	
	private int generateGuardTime() {
		return 2 * synchronizer.getMaxError() + FIXED_GUARD;
	}

	private void insertLogicalClockAndTimestampPacketToSend() {
		UInt32 localTime = node.getClock().getValue();
		scheduledPacket.setTimestamp(localTime);
		scheduledPacket.setEventTime(localTime);

		synchronizer.logicalClock.update(localTime);
		UInt32 globalTime = synchronizer.logicalClock.getValue(localTime);
		scheduledPacket.setClock(globalTime);
	}

	protected void nextAction() {
		
		if(currentSchedule.type == ScheduleType.SEND){
			synchronizer.logicalClock.update(node.getClock().getValue());
			UInt32 global = synchronizer.logicalClock.getValue(node.getClock()
					.getValue());

			schedule.reschedule(global, synchronizer.logicalClock.rate);

			synchronizer.nextHistorySlot();
			
			if (synchronizer.getMaxError() < 1000) {
				isSleepingAllowed = true;
			} else {
				isSleepingAllowed = false;
			}
		}
		
		int remainingTime = 0;

		if (isSleepingAllowed) {
			currentSchedule = schedule.getNextSchedule();
			remainingTime -= RadioProcessor.wakeUpTime;
		} else {
			currentSchedule = schedule.getTransmissionSchedule();
		}

		if (currentSchedule.type == ScheduleType.SEND)
			remainingTime -= RadioProcessor.processingTime;
		else if (currentSchedule.type == ScheduleType.RECEIVE) {
			if (isSleepingAllowed) {
				remainingTime -= generateGuardTime();
			}
		}

		remainingTime += currentSchedule.time.toInteger();
		if (remainingTime > 0) {
			transmitTimer.startOneshot(remainingTime);
		} else {
			System.out.println("ERORR");
		}
	}

	public void destroy(){
		new DutyCycleGraph(node.getId(), stats.getDutyCycle());
		new LocalSkewGraph(node.getId(), stats.getLocalSkew());
		new PacketLossGraph(node.getId(), stats.getLostPackets());
	}

	@Override
	public void receiveMessage(RadioPacket packet) {
		if (packet != null)
			synchronizer.synchronize(packet);		
	}

	@Override
	public void on() {
		stats.on();
		
	}

	@Override
	public void off() {
		stats.off();
		
	}

	@Override
	public void packetLost() {
		stats.incrementPacketLoss();
		
	}
}
