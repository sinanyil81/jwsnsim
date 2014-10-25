package application.appEgtsp;

import hardware.Register;

public class GradientClock {
	
	private Register value = new Register();
    private Register offset = new Register();
    private float rate = 0;    
  	private float rootRate = 0;
    private Register rootOffset = new Register();
    private Register updateLocalTime = new Register();    
               
	public Register getOffset() {
		return new Register(offset);
	}

	public void setOffset(Register offset) {
		this.offset = new Register(offset);
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
	
    public Register getRootOffset() {
    	return new Register(rootOffset);
	}

	public void setRootOffset(Register rootOffset) {
		this.rootOffset = new Register(rootOffset);
	}

	public void setUpdateLocalTime(Register updateLocalTime) {
		this.updateLocalTime = new Register(updateLocalTime);
	}

	public void setValue(Register value) {
		this.value = new Register(value);
	}
	
	public void update(Register local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		float r = (rate -rootRate)/(1.0f + rootRate);
		
		timePassed  += (int)(((float)timePassed)*r);

		value = value.add(timePassed);
		this.updateLocalTime = new Register(local);
	}

	public Register getValue(Register local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		float r = (rate -rootRate)/(1.0f + rootRate);	
		
		timePassed  += (int)(((float)timePassed)*r);

		Register val = value.add(offset);
		return val.add(new Register(timePassed));
	}
	
	public Register getRTValue(Register local){
		Register time = getValue(local);
		return time.subtract(rootOffset);
	}
}
