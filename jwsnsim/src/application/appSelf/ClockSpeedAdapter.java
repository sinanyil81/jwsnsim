package application.appSelf;

import hardware.Register32;

import java.util.Hashtable;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;

public class ClockSpeedAdapter {
	
	private static float TOLERANCE = 0.00000001f;

	class NeighborData {
		public Register32 clock;
		public Register32 timestamp;

		public NeighborData(Register32 clock, Register32 timestamp) {
			this.clock = new Register32(clock);
			this.timestamp = new Register32(timestamp);
		}
	}
	
//	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.00000000001f, 0.0001f);
	public AVT rate = new AVTBuilder()
	.upperBound(0.0001)
	.lowerBound(-0.0001)
	.deltaMin(0.000000001)
	.isDeterministicDelta(true)
	.deltaMax(0.0001)
	.startValue(0.0)
//	.deltaDecreaseFactor(2)
//	.deltaIncreaseFactor(2)
	.build();
	
	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();
	
	private Register32 value = new Register32();
	private Register32 lastUpdate = new Register32();

	public ClockSpeedAdapter(){
		double deltaMin = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMax();		
//		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().setDelta(deltaMin);
	}
	
	private float decide(int nodeid,Register32 clock,Register32 timestamp,float rate) {

		float decision = 0.0f;
		
		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			
			int timePassed = clock.subtract(neighbor.clock).toInteger();
			int neighborProgress = timePassed + (int)(((float)timePassed)*rate);
			
			timePassed = timestamp.subtract(neighbor.timestamp).toInteger();
			int myProgress = timePassed + (int)(((float)timePassed)*(float)this.rate.getValue());

			decision = (float)(neighborProgress-myProgress);
			decision /= (float)myProgress;
			
//			decision = (double)neighborProgress/(double)myProgress - 1.0;
		}

		return decision;
	}
	
//	private int decide(int nodeid,UInt32 clock,UInt32 timestamp,float rate) {
//
//		int decision = 0;
//		
//		NeighborData neighbor = neighbors.get(nodeid);
//
//		if (neighbor != null) {
//			
//			int timePassed = clock.subtract(neighbor.clock).toInteger();
//			int neighborProgress = timePassed + (int)(((float)timePassed)*rate);
//			
//			timePassed = timestamp.subtract(neighbor.timestamp).toInteger();
//			int myProgress = timePassed + (int)(((float)timePassed)*(float)this.rate.getValue());
//
//			decision = neighborProgress-myProgress;
//		}
//
//		return decision;
//	}
	
	public void adjust(int nodeid, Register32 clock,Register32 timestamp,float rate) {
		update(timestamp);
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
	
	public void update(Register32 timestamp){
		int timePassed = timestamp.subtract(lastUpdate).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*(float)rate.getValue());
		value = value.add(progress);
		
		lastUpdate = new Register32(timestamp);
	}
	
	public Register32 getValue(Register32 timestamp){
		int timePassed = timestamp.subtract(lastUpdate).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*(float)rate.getValue());

		return value.add(progress);	
	}
}
