package application.appSelf;

import sim.type.UInt32;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;

public class LogicalClock {

	private UInt32 value = new UInt32();

	public AVT rate = new AVTBuilder()
							.upperBound(0.0002)
							.lowerBound(-0.0002)
							.deltaMin(0.000000000001f)			
							.deltaMax(0.00001)
							.startValue(0.00001)
							.build();	
	
//	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.00000001f, 0.00001024f);
	
	public UInt32 offset = new UInt32();

	UInt32 updateLocalTime = new UInt32();

	public void setOffset(UInt32 offset) {
		this.offset = new UInt32(offset);
	}

	public UInt32 getOffset() {
		return new UInt32(offset);
	}

	public void update(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate.getValue());

		value = value.add(timePassed);
		this.updateLocalTime = new UInt32(local);
	}

	public UInt32 getValue(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate.getValue());

		UInt32 val = value.add(offset);
		return val.add(new UInt32(timePassed));
	}

	public void setValue(UInt32 time, UInt32 local) {
		value = new UInt32(time);
		offset = new UInt32();
		this.updateLocalTime = new UInt32(local);
	}
}
