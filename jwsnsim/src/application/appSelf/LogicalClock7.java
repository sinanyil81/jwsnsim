package application.appSelf;

import sim.type.UInt32;

public class LogicalClock7 {

	private UInt32 value = new UInt32();
	public float rate = 0.0f;
			
	UInt32 updateLocalTime = new UInt32();
		
	public void update(UInt32 local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		value = value.add(timePassed);
		this.updateLocalTime = new UInt32(local);
	}

	public UInt32 getValue(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		return value.add(new UInt32(timePassed));
	}
}
