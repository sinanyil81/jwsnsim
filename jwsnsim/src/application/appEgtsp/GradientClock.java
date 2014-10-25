package application.appEgtsp;

import hardware.Register32;

public class GradientClock {
	
	private Register32 value = new Register32();
    private Register32 offset = new Register32();
    private float rate = 0;    
  	private float rootRate = 0;
    private Register32 rootOffset = new Register32();
    private Register32 updateLocalTime = new Register32();    
               
	public Register32 getOffset() {
		return new Register32(offset);
	}

	public void setOffset(Register32 offset) {
		this.offset = new Register32(offset);
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public float getRootRate() {
		return rootRate;
	}

	public void setRootRate(float rootRate) {
		this.rootRate = rootRate;
	}
	
    public Register32 getRootOffset() {
    	return new Register32(rootOffset);
	}

	public void setRootOffset(Register32 rootOffset) {
		this.rootOffset = new Register32(rootOffset);
	}

	public void setUpdateLocalTime(Register32 updateLocalTime) {
		this.updateLocalTime = new Register32(updateLocalTime);
	}

	public void setValue(Register32 value) {
		this.value = new Register32(value);
	}
	
	public void update(Register32 local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		float r = (rate -rootRate)/(1.0f + rootRate);
		
		timePassed  += (int)(((float)timePassed)*r);

		value = value.add(timePassed);
		this.updateLocalTime = new Register32(local);
	}

	public Register32 getValue(Register32 local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		float r = (rate -rootRate)/(1.0f + rootRate);	
		
		timePassed  += (int)(((float)timePassed)*r);

		Register32 val = value.add(offset);
		return val.add(new Register32(timePassed));
	}
	
	public Register32 getRTValue(Register32 local){
		Register32 time = getValue(local);
		return time.subtract(rootOffset);
	}
}
