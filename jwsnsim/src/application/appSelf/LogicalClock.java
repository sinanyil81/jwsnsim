package application.appSelf;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import sim.type.UInt32;

public class LogicalClock {

	private UInt32 value = new UInt32();

	public AVT rate = new AVTBuilder().upperBound(0.000100).lowerBound(0.000030).deltaMin(0.000000001).isDeterministicDelta(true).deltaMax(0.0001).startValue(0.000070).build();
	public AVT offset = new AVTBuilder().upperBound(1000).lowerBound(-1000).deltaMin(1).isDeterministicDelta(true).deltaMax(100).startValue(0).build();
			
	UInt32 updateLocalTime = new UInt32();
	
	public void setValue(UInt32 value,UInt32 local) {
		this.value = new UInt32(value);
		this.updateLocalTime = new UInt32(local);
	}
	
	public void update(UInt32 local){
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		value = value.add(timePassed);
		this.updateLocalTime = new UInt32(local);
	}

	public UInt32 getValue(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		UInt32 val = value.add(new UInt32((int)offset.getValue()));
		return val.add(new UInt32(timePassed));
	}
}
