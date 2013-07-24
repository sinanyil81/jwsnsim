package sim.jprowler.applications.LowPower;

import sim.jprowler.UInt32;

public class LogicalClock {
	
	private static LogicalClock logicalClock = null;

	private UInt32 value = new UInt32();
	public float rate = 0.0f;

	UInt32 updateLocalTime = new UInt32();	
	
	protected LogicalClock(){
		
	}

	public static LogicalClock getInstance() {
		if(logicalClock == null){
			logicalClock = new LogicalClock();
		}

		return logicalClock;
	}

	public void update(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate);

		value = value.add(timePassed);
		this.updateLocalTime = new UInt32(local);
	}

	public UInt32 getValue(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate);

		return value.add(new UInt32(timePassed));
	}

	public void setValue(UInt32 time, UInt32 local) {
		value = new UInt32(time);
		this.updateLocalTime = new UInt32(local);
	}
}
