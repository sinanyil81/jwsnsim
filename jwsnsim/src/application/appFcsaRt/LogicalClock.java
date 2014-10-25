package application.appFcsaRt;

import hardware.Register;
import sim.clock.Counter32;

public class LogicalClock {
	
	private boolean isReference = false;
	private Register value = new Register();    
	private float rate = 0;
	private float rootRate = 0;
    private Register updateLocalTime = new Register();
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
	
	public void setUpdateLocalTime(Register updateLocalTime) {
		this.updateLocalTime = new Register(updateLocalTime);
	}
    
    public void setValue(Register currentTime){
		value = new Register(currentTime);
	}
    
    public Register getOffset(){
		return new Register(value);
	}
    
	public Register getValue(Register local){
		if(isReference){
			return local;
		}
		
		int timePassed = local.subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		progress = (int) (((float)progress)/(1.0+rootRate));
		
		return value.add(new Register(progress));
	}
    
	public Register getValue(){
		if(isReference){
			return clock.getValue();
		}
		
		int timePassed = clock.getValue().subtract(updateLocalTime).toInteger();
		int progress = timePassed + (int)(((float)timePassed)*rate);
		progress = (int) (((float)progress)/(1.0+rootRate));
		
		return value.add(new Register(progress));
	}
}
