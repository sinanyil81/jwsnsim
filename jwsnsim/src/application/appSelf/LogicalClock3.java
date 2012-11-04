package application.appSelf;

import sim.type.UInt32;

public class LogicalClock3 {

	private UInt32 value = new UInt32();
	public float rate = 0.0f;
	public UInt32 offset = new UInt32();
			
	UInt32 updateLocalTime = new UInt32();
		
	public void setOffset(UInt32 offset) {
		this.offset = new UInt32(offset);
	}
	
	public UInt32 getOffset() {
		return new UInt32(offset);
	}
	
	public void update(UInt32 local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		value = value.add(timePassed);
		this.updateLocalTime = new UInt32(local);
	}

	public UInt32 getValue(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate);

		UInt32 val = value.add(offset);
		return val.add(new UInt32(timePassed));
	}
	
	public void setValue(UInt32 time,UInt32 local) {
		value = new UInt32(time);
		offset = new UInt32();
		this.updateLocalTime = new UInt32(local);
	}	
}
