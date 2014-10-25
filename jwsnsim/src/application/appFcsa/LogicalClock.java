package application.appFcsa;

import hardware.Register32;

public class LogicalClock {
	
	public Register32 value = new Register32();
    public float rate = 0;
    
    Register32 updateLocalTime = new Register32();
    
    public void setValue(Register32 currentTime){
		value = new Register32(currentTime);
	}
    
	public Register32 getValue(Register32 currentTime){
		int timePassed = currentTime.subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		
		return value.add(new Register32(progress));
	}
}
