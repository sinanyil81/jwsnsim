package application.appFcsa;

import hardware.Register;

public class LogicalClock {
	
	public Register value = new Register();
    public float rate = 0;
    
    Register updateLocalTime = new Register();
    
    public void setValue(Register currentTime){
		value = new Register(currentTime);
	}
    
	public Register getValue(Register currentTime){
		int timePassed = currentTime.subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		
		return value.add(new Register(progress));
	}
}
