package sim.jprowler.applications.LowPower;

import sim.jprowler.Event;
import sim.jprowler.Mica2NodeNonCSMA;
import sim.jprowler.Protocol;
import sim.jprowler.RadioListener;
import sim.jprowler.RadioModel;
import sim.jprowler.RadioPacket;
import sim.jprowler.UInt32;
import sim.jprowler.applications.LowPower.TDMASchedule4x4Grid.Schedule;
import sim.jprowler.applications.LowPower.TDMASchedule4x4Grid.ScheduleType;
import sim.jprowler.clock.Clock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class TDMANode extends Mica2NodeNonCSMA implements TimerHandler,RadioListener{
	
	public TimeSync synchronizer = new TimeSync();
	public TDMASchedule4x4Grid schedule = null;
	private Timer transmitTimer = null;

	boolean isSleepingAllowed = false;
	
	RadioPacket scheduledPacket = null;
	Protocol protocol = null;
	
	Schedule currentSchedule = null;

	public TDMANode(RadioModel radioModel, Clock clock) {
		super(radioModel, clock);
		
		transmitTimer = new Timer(clock, this);
	
	}
	
	public void start(){
		
		schedule = new TDMASchedule4x4Grid(this);
		schedule.reschedule(getClock().getValue(), 0.0f);	
		currentSchedule = schedule.getTransmissionSchedule();		
		transmitTimer.startOneshot(currentSchedule.time.toInteger());	
	}
	
	public boolean sendMessage(RadioPacket packet, Protocol app){
		scheduledPacket = packet.clone();
		protocol = app;
		
		return true;
	}

	@Override
	public void fireEvent(Timer timer) {
		if(timer == transmitTimer){
			switch (currentSchedule.type) {
			case SEND:				
				insertLogicalClockAndTimestampPacketToSend();
				super.sendMessage(scheduledPacket, protocol);							
				break;
				
			case RECEIVE:
				wakeUp();
				startSleepTimer(wakeUpTime + generateGuardTime());
				break;

			default:
				break;
			}									
		}		
	}
	
	private int generateGuardTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void insertLogicalClockAndTimestampPacketToSend() {
		UInt32 localTime = getClock().getValue();
		scheduledPacket.setTimestamp(localTime);
		scheduledPacket.setEventTime(localTime);

		synchronizer.logicalClock.update(localTime);
		UInt32 globalTime = synchronizer.logicalClock.getValue(localTime);
		scheduledPacket.setClock(globalTime);
	}

	@Override
	public void startedReceiving() {
				
	}

	@Override
	public void stoppedReceiving() {
		if (!corrupted)
			synchronizer.synchronize(receivedPacket);	
		
		stopSleepTimer();
		sleep();
		nextAction();
	}

	@Override
	public void startedTransmitting() {
				
	}

	@Override
	public void stoppedTransmitting() {		
		synchronizer.logicalClock.update(getClock().getValue());	
		UInt32 global = synchronizer.logicalClock.getValue(getClock().getValue());
		
		schedule.reschedule(global, synchronizer.logicalClock.rate);	
		
		if(synchronizer.maxError < 10000){
			isSleepingAllowed = true;
		}
		else{
			isSleepingAllowed = false;
		}
		
		synchronizer.maxError = 0;
		
		nextAction();
	}
	

	protected void nextAction() {
		int remainingTime = 0;
		
		if(isSleepingAllowed){
			sleep();
			currentSchedule = schedule.getNextSchedule();
			remainingTime -= wakeUpTime;			
		}
		else{
			currentSchedule = schedule.getTransmissionSchedule();
		}
		
		if(currentSchedule.type == ScheduleType.SEND)
			remainingTime -= processingTime;
		
		transmitTimer.startOneshot(remainingTime);
	}

	@Override
	public void sleepTimerExpired() {
		nextAction();		
	}
}
