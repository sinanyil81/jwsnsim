package application.appSelf;

import java.util.Hashtable;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;
import sim.type.UInt32;

public class ClockSpeedAdapter5 {
	
	private static float TOLERANCE = 0.00000001f;

	class NeighborData {
		public UInt32 timestamp;

		public NeighborData(UInt32 timestamp) {
			this.timestamp = new UInt32(timestamp);
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

	public ClockSpeedAdapter5(){
		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().setDelta(0.000001f);
	}
	
	private float getDecision(int nodeid,UInt32 progress,UInt32 timestamp) {

		float decision = 0.0f;
		
		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			
			int neighborProgress = progress.toInteger();
			
			int timePassed = timestamp.subtract(neighbor.timestamp).toInteger();
			int myProgress = timePassed + (int)(((float)timePassed)*(float)this.rate.getValue());

			decision = (float)(neighborProgress-myProgress);
			decision /= (float)myProgress;
		}	
		
		return decision;
	}
	
	public void adjust(int nodeid, UInt32 progress,UInt32 timestamp) {
		float neighborDecision = getDecision(nodeid,progress,timestamp);		

		adjustRate(neighborDecision);
		
		neighbors.remove(nodeid);
		neighbors.put(nodeid, new NeighborData(timestamp));
	}
	
	private void adjustRate(float currentDecision){
		if (currentDecision < -TOLERANCE) {
			this.rate.adjustValue(Feedback.LOWER);
		} else if (currentDecision > TOLERANCE) {
			this.rate.adjustValue(Feedback.GREATER);
		} else {
			this.rate.adjustValue(Feedback.GOOD);
		}
	}

	public float getSpeed() {
		
//		return this.rate.getValue();
		return (float) this.rate.getValue();
	}
}
