package application.appGradient;

import sim.clock.Clock;
import sim.type.UInt32;

public class LogicalClock {
	
	private boolean isReference = false;
	private UInt32 value = new UInt32();    
	private float rate = 0;
	private float rootRate = 0;
    private UInt32 updateLocalTime = new UInt32();
	private Clock clock;
    
    public LogicalClock(Clock clock){
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
	
	public void setUpdateLocalTime(UInt32 updateLocalTime) {
		this.updateLocalTime = new UInt32(updateLocalTime);
	}
    
    public void setValue(UInt32 currentTime){
		value = new UInt32(currentTime);
	}
    
    public UInt32 getOffset(){
		return new UInt32(value);
	}
    
	public UInt32 getValue(UInt32 local){
		if(isReference){
			return local;
		}
		
		int timePassed = local.subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		progress = (int) (((float)progress)/(1.0+rootRate));
		
		return value.add(new UInt32(progress));
	}
    
	public UInt32 getValue(){
		if(isReference){
			return clock.getValue();
		}
		
		int timePassed = clock.getValue().subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		progress = (int) (((float)progress)/(1.0+rootRate));
		
		return value.add(new UInt32(progress));
	}
}
