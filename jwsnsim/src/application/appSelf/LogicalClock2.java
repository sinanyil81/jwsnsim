package application.appSelf;

import hardware.Register32;

public class LogicalClock2 {

	private Register32 value = new Register32();
	public float rate = 0.0f;
	Register32 updateLocalTime = new Register32();
	
	public Register32 getValue(Register32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		return value.add(new Register32(timePassed));
	}
	
	public void setValue(Register32 time,Register32 local) {
		value = new Register32(time);
		this.updateLocalTime = new Register32(local);
	}	
}
