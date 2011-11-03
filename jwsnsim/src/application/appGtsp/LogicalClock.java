package application.appGtsp;

import sim.type.UInt32;

public class LogicalClock {
	public UInt32 value = new UInt32();
    public long offset = 0;
    public double rate = 1;
    
    UInt32 updateLocalTime = new UInt32();
    
	public UInt32 getValue(UInt32 currentTime){
		long timePassed = currentTime.subtract(updateLocalTime).getValue();
		long progress = (long)((double)timePassed*rate) + offset;
		
		return value.add(new UInt32(progress));
	}
}
