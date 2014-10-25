package application.appSelf;

import sim.type.Register;

public class LogicalClock6 {

	private Register value = new Register();
	public float rate = 0.0f;
			
	Register updateLocalTime = new Register();
		
	public void update(Register local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		value = value.add(timePassed);
		this.updateLocalTime = new Register(local);
	}

	public Register getValue(Register local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		return value.add(new Register(timePassed));
	}
}
