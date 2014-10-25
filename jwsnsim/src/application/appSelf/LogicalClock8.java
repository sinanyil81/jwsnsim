package application.appSelf;

import hardware.Register;

public class LogicalClock8 {

	private Register value = new Register();
	
	//public AVT rate = new AVTBuilder().upperBound(0.000100).lowerBound(0.000030).deltaMin(0.000000001).isDeterministicDelta(true).deltaMax(0.0001).startValue(0.000070).build();
//	public AvtSimple rate = new AvtSimple(0.000030f, 0.000100f, 0.000070f, 0.000000001f, 0.0001f);
	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.0000000001f, 0.0001f);
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
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		value = value.add(timePassed);
		this.updateLocalTime = new Register(local);
	}

	public Register getValue(Register local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		Register val = value.add(offset);
		return val.add(new Register(timePassed));
	}
	
	public void setValue(Register time,Register local) {
		value = new Register(time);
		offset = new Register();
		this.updateLocalTime = new Register(local);
	}	
}
