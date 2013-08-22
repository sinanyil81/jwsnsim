package sim.jprowler.applications.LowPower;

import java.util.Collections;
import java.util.Vector;

import sim.jprowler.Node;
import sim.jprowler.UInt32;

public class TDMASchedule4x4Grid {
	
	public static final int TRANSMIT_GUARD = 3000;

	public enum ScheduleType {
		RECEIVE, SEND
	};

	class Schedule implements Comparable<Schedule>{
		ScheduleType type;
		UInt32 time = null;

		public Schedule(ScheduleType type, UInt32 time) {
			this.type = type;
			this.time = time;
		}

		@Override
		public int compareTo(Schedule o) {
			if((time.subtract(o.time)).toInteger() > 0){
				return 1;
			}
			else if((time.subtract(o.time)).toInteger()<0){
				return -1;
			}
			
			return 0;
		}
	}

	private LogicalClock logical = new LogicalClock();
	private Node node = null;

	Vector<Schedule> schedules = null;

	int[] neighborIds = null;

	public final static int EPOCH = 0xFFFFFF;

	public TDMASchedule4x4Grid(Node node) {
		this.node = node;

		 int numNeighbors = 0;
		 
		 if ((node.getId() % 4) == 1){
			 
			 if((node.getId()-4) > 0) numNeighbors++;
			 if((node.getId()+1) < 17) numNeighbors++;
			 if((node.getId()+4) < 17) numNeighbors++;
		 }
		 else  if ((node.getId() % 4) == 0){
			 if((node.getId()-4) > 0) numNeighbors++;
			 if((node.getId()-1) > 0) numNeighbors++;
			 if((node.getId()+4) < 17) numNeighbors++;
		 }
		 else{
			 if((node.getId()-4) > 0) numNeighbors++;
			 if((node.getId()-1) > 0) numNeighbors++;
			 if((node.getId()+1) < 17) numNeighbors++;
			 if((node.getId()+4) < 17) numNeighbors++;
		 }
				
		 neighborIds = new int[numNeighbors];
		
		 int i = 0;
		 
		 if ((node.getId() % 4) == 1){
			 
			 if((node.getId()-4) > 0) neighborIds[i++] = node.getId()-4;
			 if((node.getId()+1) < 17) neighborIds[i++] = node.getId()+1;
			 if((node.getId()+4) < 17) neighborIds[i++] = node.getId()+4;
		 }
		 else  if ((node.getId() % 4) == 0){
			 if((node.getId()-4) > 0) neighborIds[i++] = node.getId()-4;
			 if((node.getId()-1) > 0) neighborIds[i++] = node.getId()-1;
			 if((node.getId()+4) < 17) neighborIds[i++] = node.getId()+4;
		 }
		 else{
			 if((node.getId()-4) > 0) neighborIds[i++] = node.getId()-4;
			 if((node.getId()-1) > 0) neighborIds[i++] = node.getId()-1;
			 if((node.getId()+1) < 17) neighborIds[i++] = node.getId()+1;
			 if((node.getId()+4) < 17) neighborIds[i++] = node.getId()+4;
		 }
	}

	private UInt32 getNextSchedule(int id,UInt32 time) {
		UInt32 currentEpoch = time.shiftRight(24);
		currentEpoch = currentEpoch.shiftLeft(24);
		UInt32 nextTransmission = currentEpoch.add(0xFFFFF * (id - 1));
		
		int remainingTime =nextTransmission.subtract(time).toInteger(); 

		if(remainingTime <= 0){
			currentEpoch = time.shiftRight(24);
			currentEpoch = currentEpoch.increment();
			currentEpoch = currentEpoch.shiftLeft(24);
			
			nextTransmission = currentEpoch.add(0xFFFFF * (id - 1));
		}		
		
		return nextTransmission;
	}		

	public void reschedule(UInt32 globalTime, float rate) {

		schedules = new Vector<Schedule>();

		logical.setValue(globalTime, node.getClock().getValue());
		logical.rate = rate;

		UInt32 nextTransmission = getNextSchedule(node.getId(), globalTime.add(TRANSMIT_GUARD));
		schedules.add(new Schedule(ScheduleType.SEND, nextTransmission));
		

		for (int i = 0; i < neighborIds.length; i++) {
			UInt32 nextReceipt = getNextSchedule(neighborIds[i], globalTime);
			
			if((nextTransmission.subtract(nextReceipt)).toInteger() > 0){
				Schedule s = new Schedule(ScheduleType.RECEIVE,nextReceipt);
				schedules.add(s);
			}						
		}

		Collections.sort(schedules);
	}

	public Schedule getTransmissionSchedule() {
		logical.update(node.getClock().getValue());
		UInt32 globalTime = logical.getValue(node.getClock().getValue());

		Schedule s = schedules.lastElement();
		schedules.remove(s);

		int remaining = (s.time.subtract(globalTime)).toInteger();
		remaining = (int) ((double) remaining / (1.0f + logical.rate));
		s.time = new UInt32(remaining);

		return s;
	}

	public Schedule getNextSchedule() {
		logical.update(node.getClock().getValue());
		UInt32 globalTime = logical.getValue(node.getClock().getValue());

		Schedule s = schedules.remove(0);
		
		if (s != null) {
			int remaining = (s.time.subtract(globalTime)).toInteger();
			remaining = (int) ((double) remaining / (1.0f + logical.rate));
			s.time = new UInt32(remaining);

			return s;
		}

		return null;
	}
}
