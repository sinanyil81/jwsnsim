package application.appSelf;

import hardware.Register32;

import java.util.Hashtable;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;

public class ClockSpeedAdapter2 {
	
	private static float TOLERANCE = 2f;

	class NeighborData {
		public Register32 val;
		public Register32 timestamp;

		public NeighborData(Register32 val, Register32 timestamp) {
			this.val = new Register32(val);
			this.timestamp = new Register32(timestamp);
		}
	}
	
//	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.00000000001f, 0.0001f);
	public AVT rate = new AVTBuilder()
	.upperBound(0.0001)
	.lowerBound(-0.0001)
	.deltaMin(0.00000001)
	.isDeterministicDelta(true)
	.deltaMax(0.0001)
	.startValue(0.0)
	.deltaDecreaseFactor(3)
	.deltaIncreaseFactor(2)
	.build();
	
	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();
	
	private Register32 value = new Register32();
	private Register32 lastUpdate = new Register32();

	public ClockSpeedAdapter2(){
//		double deltaMin = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMax();		
//		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().setDelta(deltaMin);
	}
	
	private float decide(int nodeid,int neighborProgress,Register32 timestamp) {

		float decision = 0.0f;
		
		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			
			int myProgress = getValue(timestamp).subtract(neighbor.val).toInteger();				

			decision = (float)(neighborProgress-myProgress);
			//decision /= (float)myProgress;
		}

		return decision;
	}
	
	public void adjust(int nodeid, int neighborProgress,Register32 timestamp) {
		
		update(timestamp);
		float decision = decide(nodeid,neighborProgress,timestamp);

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
		neighbors.put(nodeid, new NeighborData(getValue(timestamp),timestamp));
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

	public float getSpeed() {
		
//		return this.rate.getValue();
		return (float) this.rate.getValue();
	}
}
