package application.appFcsaRt;

import hardware.Counter32;
import hardware.Register32;

public class LogicalClock {
	
	private boolean isReference = false;
	private Register32 value = new Register32();    
	private float rate = 0;
	private float rootRate = 0;
    private Register32 updateLocalTime = new Register32();
	private Counter32 clock;
    
    public LogicalClock(Counter32 clock){
    	this.clock = clock;
    }
    
    public void setReference(){
    	isReference = true;
    }
    
    public float getRate() {
		return rate;
	}

	public void setRate(float rate) {		
		this.rate = rate;
		
		if(isReference)
			this.rootRate = this.rate;
	}

	public float getRootRate() {
		return rootRate;
	}

	public void setRootRate(float rootRate) {
		if(!isReference)
			this.rootRate = rootRate;
	}
	
	public void setUpdateLocalTime(Register32 updateLocalTime) {
		this.updateLocalTime = new Register32(updateLocalTime);
	}
    
    public void setValue(Register32 currentTime){
		value = new Register32(currentTime);
	}
    
    public Register32 getOffset(){
		return new Register32(value);
	}
    
	public Register32 getValue(Register32 local){
		if(isReference){
			return local;
		}
		
		int timePassed = local.subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		progress = (int) (((float)progress)/(1.0+rootRate));
		
		return value.add(new Register32(progress));
	}
    
	public Register32 getValue(){
		if(isReference){
			return clock.getValue();
		}
		
		int timePassed = clock.getValue().subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		progress = (int) (((float)progress)/(1.0+rootRate));
		
		return value.add(new Register32(progress));
	}
}
