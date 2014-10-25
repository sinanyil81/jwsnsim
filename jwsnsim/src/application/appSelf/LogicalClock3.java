package application.appSelf;

import hardware.Register32;

public class LogicalClock3 {

	private Register32 value = new Register32();
	public float rate = 0.0f;
	public Register32 offset = new Register32();
			
	Register32 updateLocalTime = new Register32();
		
	public void setOffset(Register32 offset) {
		this.offset = new Register32(offset);
	}
	
	public Register32 getOffset() {
		return new Register32(offset);
	}
	
	public void update(Register32 local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		value = value.add(timePassed);
		this.updateLocalTime = new Register32(local);
	}

	public Register32 getValue(Register32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		Register32 val = value.add(offset);
		return val.add(new Register32(timePassed));
	}
	
	public void setValue(Register32 time,Register32 local) {
		value = new Register32(time);
		offset = new Register32();
		this.updateLocalTime = new Register32(local);
	}	
}
