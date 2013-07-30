package sim.jprowler.applications.LowPower;

import sim.jprowler.RadioPacket;
import sim.jprowler.UInt32;

public class TimeSync {
	
	private static final int BEACON_RATE = TDMASchedule4x4Grid.EPOCH;
	private static final float MAX_PPM = 0.0001f;
	private static final float BOUNDARY = 2.0f * MAX_PPM * (float) BEACON_RATE;
	float K_max = 0.000004f / BOUNDARY;

	public LogicalClock logicalClock = new LogicalClock();
	
	int maxError = 0;

	public int calculateSkew(RadioPacket packet) {
		UInt32 neighborClock = packet.getClock();
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		return neighborClock.subtract(myClock).toInteger();
	}

	public void synchronize(RadioPacket packet) {
		logicalClock.update(packet.getEventTime());
		int skew = calculateSkew(packet);
		
		if(Math.abs(skew)> maxError)
			maxError = Math.abs(skew); 

		/* initial offset compensation */
		if (Math.abs(skew) <= BOUNDARY) {

			float x = BOUNDARY - Math.abs(skew);
			float K_i = x * K_max / BOUNDARY;

			logicalClock.rate += K_i * 0.5 * (float) skew;
		}

		if (skew > 1000) {
			UInt32 myClock = logicalClock.getValue(packet.getTimestamp());
			logicalClock.setValue(myClock.add(skew),packet.getTimestamp());
		} else {
			UInt32 myClock = logicalClock.getValue(packet.getTimestamp());
			logicalClock.setValue(myClock.add(skew / 2),packet.getTimestamp());
		}
	}
	
	public boolean isSynced() {

		return false;
	}

}
