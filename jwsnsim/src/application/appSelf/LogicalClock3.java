package application.appSelf;

import hardware.Register;

public class LogicalClock3 {

	private Register value = new Register();
	public float rate = 0.0f;
	public Register offset = new Register();
			
	Register updateLocalTime = new Register();
		
	public void setOffset(Register offset) {
		this.offset = new Register(offset);
	}
	
	public Register getOffset() {
		return new Register(offset);
	}
	
	public void update(Register local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		value = value.add(timePassed);
		this.updateLocalTime = new Register(local);
	}

	public Register getValue(Register local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		Register val = value.add(offset);
		return val.add(new Register(timePassed));
	}
	
	public void setValue(Register time,Register local) {
		value = new Register(time);
		offset = new Register();
		this.updateLocalTime = new Register(local);
	}	
}
