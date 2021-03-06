package application.appSelf;

import hardware.Register32;

import java.util.Hashtable;
import java.util.Iterator;

import application.appSelf.ClockSpeedAdapter3.NeighborData;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;

public class ClockSpeedAdapter7 {

	private static float TOLERANCE = 0.000000001f;

	class NeighborData {
		public Register32 timestamp;
		public float speed;

		public NeighborData(Register32 timestamp, float speed) {
			this.timestamp = new Register32(timestamp);
			this.speed = speed;
		}
	}

	// public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f,
	// 0.00000000001f, 0.0001f);
	
	
	public AVT rate = new AVTBuilder()
	.upperBound(0.0001)
	.lowerBound(-0.0001)
	.deltaMin(0.00000000001)
	.isDeterministicDelta(true)
	.deltaMax(0.0001)
	.startValue(0.0)
// .deltaDecreaseFactor(30)
// .deltaIncreaseFactor(20)
	.build();

	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();

	public ClockSpeedAdapter7() {
		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM()
				.setDelta(0.00000001f);		
	}

	private float getDecision(int nodeid, Register32 progress,Register32 hardwareProgress, Register32 timestamp,float rate) {

		float decision = 0.0f;

		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			
//			int neighborProgress = progress.toInteger();
			int neighborHardwareProgress = hardwareProgress.toInteger();

//			float meanNeighborMultiplier = (float)(neighborProgress - neighborHardwareProgress)/(float)neighborHardwareProgress;
			float meanNeighborMultiplier = rate; // anlık
			
			int myHardwareProgress = timestamp.subtract(neighbor.timestamp).toInteger();
			
			float relativeHardwareClockRate = (float)(neighborHardwareProgress - myHardwareProgress)/(float)(myHardwareProgress);
			
			decision = relativeHardwareClockRate + relativeHardwareClockRate*meanNeighborMultiplier + meanNeighborMultiplier;
//			decision = relativeHardwareClockRate + relativeHardwareClockRate*meanNeighborMultiplier + meanNeighborMultiplier - (float)this.rate.getValue();
						
//			float multiplierDifference = meanNeighborMultiplier - (float)this.rate.getValue();
			
//			decision = relativeHardwareClockRate*multiplierDifference - multiplierDifference/((float)this.rate.getValue()+1.0f);
			
//			decision = relativeHardwareClockRate*l2 + relativeHardwareClockRate*l1 + l2 + l1;
//			decision -= 1.0f;

		}
		
		return decision;
	}

	public void adjust(int nodeid, Register32 progress,Register32 hardwareProgress, Register32 timestamp,float rate) {
		float neighborDecision = getDecision(nodeid, progress,hardwareProgress, timestamp,rate);

		neighbors.remove(nodeid);
		neighbors.put(nodeid, new NeighborData(timestamp, neighborDecision));
		
//		adjustRate(neighborDecision);
		adjustRate(getAverageDecision());
		
//		neighbors.remove(nodeid);
//		neighbors.put(nodeid, new NeighborData(timestamp, neighborDecision));


	}

	private void adjustRate(float currentDecision) {
		
		currentDecision -= (float)this.rate.getValue();
		
		if (currentDecision < -TOLERANCE) {
			this.rate.adjustValue(Feedback.LOWER);
		} else if (currentDecision > TOLERANCE) {
			this.rate.adjustValue(Feedback.GREATER);
		} else {
			this.rate.adjustValue(Feedback.GOOD);
		}
	}
	
	private float getAverageDecision() {
//		float retVal = (float) this.rate.getValue();
//		int num = 1;
		
		float retVal = 0.0f;
		int num = 0;
		
		for (Iterator<Integer> iterator = neighbors.keySet().iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			NeighborData n = neighbors.get(id);	
			
			retVal += n.speed;
			num++;					
		}
		
		retVal /= (float)num;		
			
		return retVal;
	}

	public float getSpeed() {

		// return this.rate.getValue();
		return (float) this.rate.getValue();
	}
}
