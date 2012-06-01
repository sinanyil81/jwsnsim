package application.appSelf;

import sim.type.UInt32;
import fr.irit.smac.util.adaptivevaluetracker.AdaptiveValueTracker;
import fr.irit.smac.util.adaptivevaluetracker.factory.StandardAdaptiveValueTrackerFactory;

public class LogicalClock {

	public UInt32 value = new UInt32();

	public AdaptiveValueTracker rate = new StandardAdaptiveValueTrackerFactory()
			.newAdaptiveValueTracker(-0.00005, 0.00005, 0.0, 0.0000001, false);

	UInt32 updateLocalTime = new UInt32();

	public void setValue(UInt32 currentTime) {
		value = new UInt32(currentTime);
	}

	public UInt32 getValue(UInt32 currentTime) {
		int timePassed = currentTime.subtract(updateLocalTime).toInteger();
		int progress = timePassed
				+ (int) (((float) timePassed) * rate.getCurrentValue());

		return value.add(new UInt32(progress));
	}
}
