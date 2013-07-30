package sim.jprowler.applications.LowPower;

import sim.jprowler.Mica2NodeNonCSMA;
import sim.jprowler.Protocol;
import sim.jprowler.RadioModel;
import sim.jprowler.RadioPacket;
import sim.jprowler.UInt32;
import sim.jprowler.clock.Clock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class TDMANode extends Mica2NodeNonCSMA implements TimerHandler{
	
	public TimeSync synchronizer = new TimeSync();
	public TDMASchedule4x4Grid schedule = null;
	private Timer transmitTimer = null;
	private Timer receptionTimer = null;
	
	RadioPacket scheduledPacket = null;
	Protocol protocol = null;

	public TDMANode(RadioModel radioModel, Clock clock) {
		super(radioModel, clock);
		
		transmitTimer = new Timer(clock, this);
		receptionTimer = new Timer(clock, this);
		
		schedule = new TDMASchedule4x4Grid(getId());
		transmitTimer.startOneshot(TDMASchedule4x4Grid.EPOCH);		
	}
	
	public boolean sendMessage(RadioPacket packet, Protocol app){
		scheduledPacket = packet.clone();
		protocol = app;
		
		return true;
	}
	
	protected void removeNoise(double level, Object stream) {
		// just do time synchronization before receiving packet
		if((radioState == RadioStates.RECEIVING) &&  (senderNode == stream)){
			if (!corrupted)
				synchronizer.synchronize(receivedPacket);
		}
		
		super.removeNoise(level, stream);
	}
	
	protected void radioOn(){
		if(waitingToTransmit){
			transmitPacket();
			waitingToTransmit = false;
		}
	}
	
	protected void transmissionFinished(){
		int remainingTicks = schedule.getRemainingTicks() - (processingTime+ processingRandomTime);
		
		if(synchronizer.maxError < 100000){
			int receiveTicks = schedule.getNextSchedule();
			receiveTicks -= wakeUpTime;
			receiveTicks -= synchronizer.maxError*2;						
			receptionTimer.startOneshot(receiveTicks);
			
			remainingTicks -= wakeUpTime;
			sleep();
		}
		else{
			receptionTimer.stop();
		}
		
		transmitTimer.startOneshot(remainingTicks);
	}
	
	
	boolean waitingToTransmit = false;

	@Override
	public void fireEvent(Timer timer) {
		if(timer == transmitTimer){
			UInt32 global = synchronizer.logicalClock.getValue(getClock().getValue());
			schedule.calculateNextTransmissionTime(global,synchronizer.logicalClock.rate);
			
			if(radioState == RadioStates.IDLE){
				transmitPacket();
			}
			else{
				waitingToTransmit = true;
				wakeUp();
			}			
		}		
		else if(timer == receptionTimer){
			wakeUp();
		}
	}

	private void transmitPacket() {
		insertLogicalClockAndTimestampPacketToSend();
		super.sendMessage(scheduledPacket, protocol);
	}
	
	private void insertLogicalClockAndTimestampPacketToSend() {
		UInt32 localTime = getClock().getValue();
		scheduledPacket.setTimestamp(localTime);
		scheduledPacket.setEventTime(localTime);

		synchronizer.logicalClock.update(localTime);
		UInt32 globalTime = synchronizer.logicalClock.getValue(localTime);
		scheduledPacket.setClock(globalTime);
	}
}
