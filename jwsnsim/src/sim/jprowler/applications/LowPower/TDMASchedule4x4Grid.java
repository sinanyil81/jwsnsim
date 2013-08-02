package sim.jprowler.applications.LowPower;

import java.util.Vector;

import sim.jprowler.Node;
import sim.jprowler.UInt32;

public class TDMASchedule4x4Grid {
	
	public enum ScheduleType  { RECEIVE,SEND};
	
	class Schedule{
		ScheduleType type;
		UInt32 time = null;
		
		public Schedule(ScheduleType type,UInt32 time){
			this.type = type;
			this.time = time;
		}
	}
	
	private LogicalClock logical = new LogicalClock();
	private Node node = null;
	
	Vector<Schedule> schedules = null;
	
	int[] neighborIds = null;
	
	public final static int EPOCH = 0xFFFFFF;
	
	public TDMASchedule4x4Grid(Node node){
		this.node = node;
		
//		int numNeighbors = 0;
//	
//		if((node.getId()-4) > 0) numNeighbors++;
//		if((node.getId()-1) > 0) numNeighbors++;
//		if((node.getId()+1) < 17) numNeighbors++;
//		if((node.getId()+4) > 17) numNeighbors++;
//		
//		neighborIds = new int[numNeighbors];		
//		
//		int i = 0;
//		
//		if((node.getId()-4) > 0) neighborIds[i++] = node.getId()-4;
//		if((node.getId()-1) > 0) neighborIds[i++] = node.getId()-1;
//		if((node.getId()+1) < 17) neighborIds[i++] = node.getId()+1;
//		if((node.getId()+4) > 17) neighborIds[i++] = node.getId()+4;
//			
		
		neighborIds = new int[1];		
		
		if(node.getId() == 1) neighborIds[0] = 2;
		if(node.getId() == 2) neighborIds[0] = 1;
		
	}
	
	public void reschedule(UInt32 globalTime,float rate){
		
		schedules = new Vector<Schedule>();
		
		logical.setValue(globalTime, node.getClock().getValue());
		logical.rate = rate;
		
		UInt32 currentEpoch = globalTime.shiftRight(24);
		currentEpoch = currentEpoch.shiftLeft(24);
		
		UInt32 nextEpoch = globalTime.shiftRight(24);
		nextEpoch = nextEpoch.increment();
		nextEpoch = nextEpoch.shiftLeft(24);
		
		UInt32 nextTransmission = nextEpoch.add(0xFFFFF*(node.getId()-1));
		
		Schedule s;
		
		for (int i = 0; i < neighborIds.length; i++) {	
			if(neighborIds[i] > node.getId()){
				s = new Schedule(ScheduleType.RECEIVE, currentEpoch.add(0xFFFFF*(neighborIds[i]-1)));
			}
			else{
				s = new Schedule(ScheduleType.RECEIVE, nextEpoch.add(0xFFFFF*(neighborIds[i]-1)));
			}
			
			schedules.add(s);
		}
		
		schedules.add(new Schedule(ScheduleType.SEND, nextTransmission));		
	}
	
	public Schedule getTransmissionSchedule(){
		logical.update(node.getClock().getValue());
		UInt32 globalTime = logical.getValue(node.getClock().getValue());		
	
		Schedule s = schedules.lastElement();
		schedules.remove(s);
		
		int remaining = (s.time.subtract(globalTime)).toInteger();
		remaining = (int) ((double)remaining/(1.0f+logical.rate));
		s.time = new UInt32(remaining);
		
		return s;
	}

	
	public Schedule getNextSchedule(){
		logical.update(node.getClock().getValue());
		UInt32 globalTime = logical.getValue(node.getClock().getValue());		
	
		Schedule s = schedules.remove(0);
		
		if(s !=null){
			int remaining = (s.time.subtract(globalTime)).toInteger();
			remaining = (int) ((double)remaining/(1.0f+logical.rate));
			s.time = new UInt32(remaining);
			
			return s;
		}
		
		return null;
	}
}
