package application.appSelf;

import hardware.Register;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;

public class LogicalClock {

	private Register value = new Register();

	public AVT rate = new AVTBuilder()
							.upperBound(0.0002)
							.lowerBound(-0.0002)
							.deltaMin(0.000000000001f)			
							.deltaMax(0.00001)
							.startValue(0.00001)
							.build();	
	
//	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.00000001f, 0.00001024f);
	
	public Register offset = new Register();

	Register updateLocalTime = new Register();

	public void setOffset(Register offset) {
		this.offset = new Register(offset);
	}

	public Register getOffset() {
		return new Register(offset);
	}

	public void update(Register local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate.getValue());

		value = value.add(timePassed);
		this.updateLocalTime = new Register(local);
	}

	public Register getValue(Register local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate.getValue());

		Register val = value.add(offset);
		return val.add(new Register(timePassed));
	}

	public void setValue(Register time, Register local) {
		value = new Register(time);
		offset = new Register();
		this.updateLocalTime = new Register(local);
	}
}
