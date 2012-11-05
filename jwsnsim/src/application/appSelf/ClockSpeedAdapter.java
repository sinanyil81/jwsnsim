//package application.appSelf;
//
//import java.util.Hashtable;
//
//import fr.irit.smac.util.avt.AVT;
//import fr.irit.smac.util.avt.AVTBuilder;
//import fr.irit.smac.util.avt.Feedback;
//import sim.type.UInt32;
//
//public class ClockSpeedAdapter {
//	
//	private static final int TOLERANCE = 1;
//
//	class NeighborData {
//		public UInt32 value;
//		public UInt32 timestamp;
//
//		public NeighborData(UInt32 value, UInt32 timestamp) {
//			this.value = new UInt32(value);
//			this.timestamp = new UInt32(timestamp);
//		}
//	}
//	
//	public AVT rate = new AVTBuilder().upperBound(0.0001f).lowerBound(-0.0001f).deltaMin(0.000000001f).isDeterministicDelta(true).deltaMax(0.0001f).startValue(0.0).build();
//	
//	private UInt32 value = new UInt32();
//	private UInt32 lastUpdate = new UInt32();
//	
//	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();
//
//	private int decide(int nodeid,UInt32 neighborProgress,UInt32 timestamp) {
//
//		int decision = 0;
//		
//		NeighborData neighbor = neighbors.get(nodeid);
//
//		if (neighbor != null) {
//			UInt32 myProgress = getValue(timestamp).subtract(neighbor.value);	
//			decision = neighborProgress.subtract(myProgress).toInteger();
//		}
//			
//		return decision;
//	}
//	
//	public void adjust(int nodeid, UInt32 progress,UInt32 timestamp) {
//		
//		updateValue(timestamp);
//		
//		int decision = decide(nodeid,progress,timestamp);
//		System.out.println(decision);
//
////		if (decision < -TOLERANCE) {
////			this.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
////		} else if (decision > TOLERANCE) {
////			this.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
////		} else {
////			this.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
////		}
//		
//		if (decision < -TOLERANCE) {
//			this.rate.adjustValue(Feedback.LOWER);
//		} else if (decision > TOLERANCE) {
//			this.rate.adjustValue(Feedback.GREATER);
//		} else {
//			this.rate.adjustValue(Feedback.GOOD);
//		}
//		
//		neighbors.remove(nodeid);
//		neighbors.put(nodeid, new NeighborData(getValue(timestamp),timestamp));
//	}
//
//
//	public float getSpeed() {
//		
////		return this.rate.getValue();
//		return (float) this.rate.getValue();
//	}
//	
//	public void updateValue(UInt32 localTime){
//		int timePassed = localTime.subtract(lastUpdate).toInteger();
//		int progress = timePassed + (int)(((float)timePassed)*(float)this.rate.getValue());
//		
//		value = value.add(new UInt32(progress));
//		lastUpdate = new UInt32(localTime);
//	}
//	
//	public UInt32 getValue(UInt32 localTime){
//		int timePassed = localTime.subtract(lastUpdate).toInteger();
//		int progress = timePassed + (int)(((float)timePassed)*(float)this.rate.getValue());
//		
//		return value.add(new UInt32(progress));
//	}
//}

package application.appSelf;

import java.util.Hashtable;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;
import sim.type.UInt32;

public class ClockSpeedAdapter {
	
	private static final int TOLERANCE = 1;

	class NeighborData {
		public UInt32 clock;
		public UInt32 timestamp;

		public NeighborData(UInt32 clock, UInt32 timestamp) {
			this.clock = new UInt32(clock);
			this.timestamp = new UInt32(timestamp);
		}
	}
	
//	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.000000001f, 0.0001f);
	public AVT rate = new AVTBuilder().upperBound(0.0001f).lowerBound(-0.0001f).deltaMin(0.000000001f).isDeterministicDelta(true).deltaMax(0.00001f).startValue(0.0).build();
	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();

	private int decide(int nodeid,UInt32 clock,UInt32 timestamp,float rate) {

		int decision = 0;
		
		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			
			int timePassed = clock.subtract(neighbor.clock).toInteger();
			int neighborProgress = timePassed + (int)(((float)timePassed)*rate);
			
			timePassed = timestamp.subtract(neighbor.timestamp).toInteger();
			int myProgress = timePassed + (int)(((float)timePassed)*(float)this.rate.getValue());

			decision = neighborProgress-myProgress;
//			System.out.println(decision);
		}

		return decision;
	}
	
//	private float decide(int nodeid,UInt32 clock,UInt32 timestamp,float rate) {
//
//		float decision = 0.0f;
//		
//		NeighborData neighbor = neighbors.get(nodeid);
//
//		if (neighbor != null) {
//			UInt32 neighborDifference, myDifference;
//			
//			neighborDifference = clock.subtract(neighbor.clock);
//			myDifference = timestamp.subtract(neighbor.timestamp);
//			float relativeHardwareClockRate = (float) (neighborDifference.toDouble()/myDifference.toDouble());
//			relativeHardwareClockRate -= 1.0f;
//			
//			decision =   (1.0f+rate)/(1.0f + (float)this.rate.getValue());
//			decision += decision*relativeHardwareClockRate;
//			decision -= 1.0f;
//		}
//
//		return decision;
//	}

	public void adjust(int nodeid, UInt32 clock,UInt32 timestamp,float rate) {
		
		int decision = decide(nodeid,clock,timestamp,rate);

//		if (decision < -TOLERANCE) {
//			this.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
//		} else if (decision > TOLERANCE) {
//			this.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
//		} else {
//			this.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
//		}
		
		if (decision < -TOLERANCE) {
			this.rate.adjustValue(Feedback.LOWER);
		} else if (decision > TOLERANCE) {
			this.rate.adjustValue(Feedback.GREATER);
		} else {
			this.rate.adjustValue(Feedback.GOOD);
		}
		
		neighbors.remove(nodeid);
		neighbors.put(nodeid, new NeighborData(clock,timestamp));
	}

	public float getSpeed() {
		
//		return this.rate.getValue();
		return (float) this.rate.getValue();
	}
}
