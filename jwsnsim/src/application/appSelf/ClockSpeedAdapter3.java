package application.appSelf;

import java.util.Hashtable;
import java.util.Iterator;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;
import sim.type.UInt32;

public class ClockSpeedAdapter3 {
	
	private static float TOLERANCE = 0.00000001f;

	class NeighborData {
		public UInt32 clock;
		public UInt32 timestamp;
		public double critically;
		public float decision;

		public NeighborData(UInt32 clock, UInt32 timestamp,double criticality,float decision) {
			this.clock = new UInt32(clock);
			this.timestamp = new UInt32(timestamp);
			this.critically = criticality;
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
	
	public ClockSpeedAdapter3(){
//		double deltaMin = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMax();		
//		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().setDelta(0.000001);
	}
	
	private float decide(int nodeid,UInt32 clock,UInt32 timestamp,float rate,double criticality) {

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

		return decision;
	}
	
	public void adjust(int nodeid, UInt32 clock,UInt32 timestamp,float rate,double criticality) {
		float decision = decide(nodeid,clock,timestamp,rate,criticality);
		
		neighbors.remove(nodeid);
		neighbors.put(nodeid, new NeighborData(clock,timestamp,criticality,decision));
	}
	
	public void refresh(){
		
		Integer stableId = getStableNeighborId();
		Integer unstableId = getUnstableNeighborId();
		
		if((stableId.intValue()==-1) ||(unstableId.intValue()==-1)) 
		{
			this.rate.adjustValue(Feedback.GOOD);
			return;
		}	

		if(getCriticality() > neighbors.get(stableId).critically){
			float decision = neighbors.get(stableId).decision;
			if (decision < -TOLERANCE) {
				this.rate.adjustValue(Feedback.LOWER);
			} else if (decision > TOLERANCE) {
				this.rate.adjustValue(Feedback.GREATER);
			} else {
				this.rate.adjustValue(Feedback.GOOD);
			}			
		}
		else{
			this.rate.adjustValue(Feedback.GOOD);
		}
//		else{
//			float decision = getDecision();
//			if (decision < -TOLERANCE) {
//				this.rate.adjustValue(Feedback.LOWER);
//			} else if (decision > TOLERANCE) {
//				this.rate.adjustValue(Feedback.GREATER);
//			} else {
//				this.rate.adjustValue(Feedback.GOOD);
//			}			
//		}
	}
	
	private Integer getStableNeighborId() {
		
		double minCriticality = Double.MAX_VALUE;
		Integer minId = Integer.valueOf(-1);
		
		for (Iterator<Integer> iterator = neighbors.keySet().iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			NeighborData n = neighbors.get(id);	
			if(n.critically < minCriticality){
				minCriticality = n.critically;	
				minId = id;
			}
		}
		
		return minId;
	}
	
	private Integer getUnstableNeighborId() {
		
		double maxCriticality = Double.MIN_VALUE;
		Integer minId = Integer.valueOf(-1);
		
		for (Iterator<Integer> iterator = neighbors.keySet().iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			NeighborData n = neighbors.get(id);	
			if(n.critically > maxCriticality){
				maxCriticality = n.critically;	
				minId = id;
			}
		}
		
		return minId;
	}
	
//	public void refresh(){
//		
//		float decision = getDecision();
//		if (decision < -TOLERANCE) {
//			this.rate.adjustValue(Feedback.LOWER);
//		} else if (decision > TOLERANCE) {
//			this.rate.adjustValue(Feedback.GREATER);
//		} else {
//			this.rate.adjustValue(Feedback.GOOD);
//		}
//	}
	
	public float getDecision(){
		float maxDif = Float.MIN_VALUE;
		float retVal = 0.0f;
		
		for (Iterator<Integer> iterator = neighbors.keySet().iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			NeighborData n = neighbors.get(id);	
			
			if(Math.abs(n.decision)> maxDif){
				maxDif = Math.abs(n.decision);
				retVal = n.decision;
			}			
		}
			
		return retVal;
	}
//	
//	public void refresh() {
//
//		float decision = getDirection();
//		if (decision < -TOLERANCE) {
//			this.rate.adjustValue(Feedback.LOWER);
//		} else if (decision > TOLERANCE) {
//			this.rate.adjustValue(Feedback.GREATER);
//		} else {
//			this.rate.adjustValue(Feedback.GOOD);
//		}
//	}
	
	public float getDirection(){
		float retVal = 0.0f;
		int num = 1;
		
		for (Iterator<Integer> iterator = neighbors.keySet().iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			NeighborData n = neighbors.get(id);	
			
			retVal += n.decision;
			num++;
					
		}
		
		return retVal / (float)num;			
	}

	public float getSpeed() {
		
//		return this.rate.getValue();
		return (float) this.rate.getValue();
	}
	
	public double getCriticality(){
		return this.rate.getAdvancedAVT().getDeltaManager().getDelta();
//		return this.rate.getCriticity();
	}
}
