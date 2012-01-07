package application.appGradient;

import sim.type.UInt32;

public class GradientClock {
	
	private UInt32 value = new UInt32();
    private UInt32 offset = new UInt32();
    private float rate = 0;    
  	private float rootRate = 0;
    private UInt32 rootOffset = new UInt32();
    private UInt32 updateLocalTime = new UInt32();    
               
	public UInt32 getOffset() {
		return new UInt32(offset);
	}

	public void setOffset(UInt32 offset) {
		this.offset = new UInt32(offset);
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
	
    public UInt32 getRootOffset() {
    	return new UInt32(rootOffset);
	}

	public void setRootOffset(UInt32 rootOffset) {
		this.rootOffset = new UInt32(rootOffset);
	}

	public void setUpdateLocalTime(UInt32 updateLocalTime) {
		this.updateLocalTime = new UInt32(updateLocalTime);
	}

	public void setValue(UInt32 value) {
		this.value = new UInt32(value);
	}	

	public UInt32 getValue(UInt32 local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		float r = (rate -rootRate)/(1.0f + rootRate);
		
		timePassed  += (int)(((float)timePassed)*r);

		UInt32 val = value.add(offset);
		return val.add(new UInt32(timePassed));
	}
	
	public UInt32 getRTValue(UInt32 local){
		UInt32 time = getValue(local);
		return time.subtract(rootOffset);
	}
}
