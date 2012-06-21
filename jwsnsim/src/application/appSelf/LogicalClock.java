package application.appSelf;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import sim.type.UInt32;

public class LogicalClock {

	public UInt32 value = new UInt32();

	public AVT rate = new AVTBuilder().upperBound(0.000100).lowerBound(0.000030).deltaMin(0.000000001).isDeterministicDelta(true).deltaMax(0.000060).startValue(0.000070).build();
			
	UInt32 updateLocalTime = new UInt32();
	
	public void update(UInt32 localTime) {
		setValue(getValue(localTime));
		updateLocalTime = localTime;
	}

	public void setValue(UInt32 currentTime) {
		value = new UInt32(currentTime);
	}
	
	public void addOffset(int offset) {
		value = value.add(offset);
	}

	public UInt32 getValue(UInt32 currentTime) {
		int timePassed = currentTime.subtract(updateLocalTime).toInteger();
		int progress = timePassed
				+ (int) (((float) timePassed) * rate.getValue());

		return value.add(new UInt32(progress));
	}
}
