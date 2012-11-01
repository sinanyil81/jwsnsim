package application.appSelf;

import java.util.Hashtable;
import sim.type.UInt32;

public class ClockSpeedAdapter {
	
	private static final int TOLERANCE = 1;

	class NeighborData {
		public UInt32 clock;
		public UInt32 timestamp;
		public float rate;

		public NeighborData(UInt32 clock, UInt32 timestamp) {
			this.clock = new UInt32(clock);
			this.timestamp = new UInt32(timestamp);
		}
	}
	
	private AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.0000000001f, 0.00001f);
	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();

	int calculateProgressDifference(int nodeid,UInt32 clock,UInt32 timestamp,float rate) {

		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {
			UInt32 previousClock = neighbor.clock;
			UInt32 difference = clock.subtract(previousClock);
			UInt32 neighborProgress = difference.add(difference.multiply(neighbor.rate));

			UInt32 previousTimestamp = neighbor.timestamp;
			difference = timestamp.subtract(previousTimestamp);
			UInt32 myProgress = difference.add(difference.multiply(this.rate.getValue()));

			return myProgress.subtract(neighborProgress).toInteger();
		}

		return 0;
	}

	public void adjust(int nodeid, UInt32 clock,UInt32 timestamp,float rate) {

		int difference = calculateProgressDifference(nodeid,clock,timestamp,rate);

		if (difference > TOLERANCE) {
			this.rate.adjustValue(AvtSimple.FEEDBACK_LOWER);
		} else if (difference < -TOLERANCE) {
			this.rate.adjustValue(AvtSimple.FEEDBACK_GREATER);
		} else {
			this.rate.adjustValue(AvtSimple.FEEDBACK_GOOD);
		}
		
		neighbors.remove(nodeid);
		neighbors.put(nodeid, new NeighborData(clock,timestamp));
	}

	public float getSpeed() {
		
		return this.rate.getValue();
	}
}
