package sim.jprowler.applications.LowPower;

import sim.jprowler.UInt32;

public class TDMASchedule4x4Grid {
	
	class Schedule{
		int id;
		int time = 0;
		boolean expired = false;
		
		public Schedule(int id) {
			this.id = id;
		}
	}
	
	Schedule[] schedules = null;
	int myId;
	int nextIndex = 0;
		
	public final static int EPOCH = 30000000;
	
	int remainingTicks = EPOCH;
			
	public int getRemainingTicks() {
		return remainingTicks;
	}

	public TDMASchedule4x4Grid(int id){
		int numNeighbors = 0;
	
		if((id-4) > 0) numNeighbors++;
		if((id-1) > 0) numNeighbors++;
		if((id+1) < 17) numNeighbors++;
		if((id+4) > 17) numNeighbors++;
		
		schedules = new Schedule[numNeighbors];
		
		int i = 0;
		
		if((id-4) > 0) schedules[i++] = new Schedule(id-4);
		if((id-1) > 0) schedules[i++] = new Schedule(id-1);
		if((id+1) < 17) schedules[i++] = new Schedule(id+1);
		if((id+4) > 17) schedules[i++] = new Schedule(id+4);
	}
	
	public void setSchedules(float logicalRate){
		
		int  remaining = remainingTicks - (EPOCH + myId*1000000);
		
		for (int i = 0; i < schedules.length; i++) {
			schedules[i].time = remaining + schedules[i].id*1000000;						
			schedules[i].time = (int) ((float) schedules[i].time / (1.0+logicalRate));
			if(schedules[i].time > 0)
				schedules[i].expired = false;
			else
				schedules[i].expired = true;
		}
	}
	
	void calculateNextTransmissionTime(UInt32 globalTime,float rate){

		int mod = globalTime.modulus(TDMASchedule4x4Grid.EPOCH);		
		
		if(mod < myId*1000000){
			remainingTicks = TDMASchedule4x4Grid.EPOCH + myId*1000000 - mod;
		}
		else {
			remainingTicks = TDMASchedule4x4Grid.EPOCH + - mod + myId*1000000;
		}
		
		remainingTicks = (int) ((double)remainingTicks/(1.0f+rate));
		
		setSchedules(rate);
	}

	
	public int getNextSchedule(){
		
		int nextSchedule = 0;
	
		for (int i = 0; i < schedules.length; i++) {
			if(!schedules[i].expired ){
				nextSchedule = schedules[i].time;
				schedules[i].expired = true;
				break;
			}
		}
		
		return nextSchedule;
	}
}
