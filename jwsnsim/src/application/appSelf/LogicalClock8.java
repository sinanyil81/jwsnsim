package application.appSelf;

import hardware.Register32;

public class LogicalClock8 {

	private Register32 value = new Register32();
	
	//public AVT rate = new AVTBuilder().upperBound(0.000100).lowerBound(0.000030).deltaMin(0.000000001).isDeterministicDelta(true).deltaMax(0.0001).startValue(0.000070).build();
//	public AvtSimple rate = new AvtSimple(0.000030f, 0.000100f, 0.000070f, 0.000000001f, 0.0001f);
	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.0000000001f, 0.0001f);
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
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		value = value.add(timePassed);
		this.updateLocalTime = new Register32(local);
	}

	public Register32 getValue(Register32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		Register32 val = value.add(offset);
		return val.add(new Register32(timePassed));
	}
	
	public void setValue(Register32 time,Register32 local) {
		value = new Register32(time);
		offset = new Register32();
		this.updateLocalTime = new Register32(local);
	}	
}
