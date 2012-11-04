package application.appSelf;

import java.util.Hashtable;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;
import sim.type.UInt32;

public class ClockSpeedAdapter {
	
	private static final float TOLERANCE = 0.00000001f;

	class NeighborData {
		public UInt32 clock;
		public UInt32 timestamp;

		public NeighborData(UInt32 clock, UInt32 timestamp) {
			this.clock = new UInt32(clock);
			this.timestamp = new UInt32(timestamp);
		}
	}
	
//	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.000000001f, 0.0001f);
	public AVT rate = new AVTBuilder().upperBound(0.0001f).lowerBound(-0.0001f).deltaMin(0.000000001f).isDeterministicDelta(true).deltaMax(0.0001f).startValue(0.0).build();
	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();

	private float decide(int nodeid,UInt32 clock,UInt32 timestamp,float rate) {

		float decision = 0.0f;
		
		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			UInt32 neighborProgress = clock.subtract(neighbor.clock);
			neighborProgress = neighborProgress.add(neighborProgress.multiply(rate));

			UInt32 myProgress = timestamp.subtract(neighbor.timestamp);
			myProgress = myProgress.add(myProgress.multiply((float) this.rate.getValue()));

			decision = (float) neighborProgress.subtract(myProgress).toDouble();
			decision /= (float)myProgress.toDouble(); 
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
		
		float decision = decide(nodeid,clock,timestamp,rate);

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
