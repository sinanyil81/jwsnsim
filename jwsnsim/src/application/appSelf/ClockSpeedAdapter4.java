package application.appSelf;

import hardware.Register32;

import java.util.Hashtable;
import java.util.Iterator;

import application.appSelf.ClockSpeedAdapter3.NeighborData;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;

public class ClockSpeedAdapter4 {
	
	private static float TOLERANCE = 0.00000001f;

	class NeighborData {
		public Register32 clock;
		public Register32 timestamp;
		public float decision = 0.0f;

		public NeighborData(Register32 clock, Register32 timestamp,float decision) {
			this.clock = new Register32(clock);
			this.timestamp = new Register32(timestamp);
			this.decision = decision;
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
//	
//	private UInt32 value = new UInt32();
//	private UInt32 lastUpdate = new UInt32();
//
//
//	public ClockSpeedAdapter4(){
//		double deltaMin = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMax();		
////		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().setDelta(deltaMin);
//	}
	
	private void updateNeighbor(int nodeid,Register32 clock,Register32 timestamp,float rate) {

		float decision = 0.0f;
		
		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			
			int timePassed = clock.subtract(neighbor.clock).toInteger();
			int neighborProgress = timePassed + (int)(((float)timePassed)*rate);
			
			timePassed = timestamp.subtract(neighbor.timestamp).toInteger();
			int myProgress = timePassed + (int)(((float)timePassed)*(float)this.rate.getValue());

			decision = (float)(neighborProgress-myProgress);
			decision /= (float)myProgress;
		}
		
		neighbors.remove(nodeid);
		neighbors.put(nodeid, new NeighborData(clock,timestamp,decision));
	}
	
	public void adjust(int nodeid, Register32 clock,Register32 timestamp,float rate) {
		updateNeighbor(nodeid,clock,timestamp,rate);
		
		float average = getAverageDecision();

		if (average < -TOLERANCE) {
			this.rate.adjustValue(Feedback.LOWER);
		} else if (average > TOLERANCE) {
			this.rate.adjustValue(Feedback.GREATER);
		} else {
			this.rate.adjustValue(Feedback.GOOD);
		}
	}

	private float getAverageDecision() {
		float retVal = 0.0f;
		int num = 0;
		
		for (Iterator<Integer> iterator = neighbors.keySet().iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			NeighborData n = neighbors.get(id);	
			
			retVal += n.decision;
			num++;					
		}
		
		if(num > 0)
			retVal /= (float)num;
		
			
		return retVal;
	}

	public float getSpeed() {
		
//		return this.rate.getValue();
		return (float) this.rate.getValue();
	}
}
