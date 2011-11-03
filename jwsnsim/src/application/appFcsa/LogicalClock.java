package application.appFcsa;

import sim.type.UInt32;

public class LogicalClock {
	
	public UInt32 value = new UInt32();
    public float rate = 0;
    
    UInt32 updateLocalTime = new UInt32();
    
    public void setValue(UInt32 currentTime){
		value = new UInt32(currentTime);
	}
    
	public UInt32 getValue(UInt32 currentTime){
		int timePassed = currentTime.subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		
		return value.add(new UInt32(progress));
	}
}
