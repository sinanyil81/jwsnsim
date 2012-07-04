package application.appSelf;

import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import sim.type.UInt32;

public class LogicalClock {

	private UInt32 value = new UInt32();
	
	public AVT rate = new AVTBuilder().upperBound(0.000100).lowerBound(0.000030).deltaMin(0.000000001).isDeterministicDelta(true).deltaMax(0.0001).startValue(0.000070).build();
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
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		value = value.add(timePassed);
		this.updateLocalTime = new UInt32(local);
	}

	public UInt32 getValue(UInt32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed  += (int) (((float) timePassed) * rate.getValue());

		UInt32 val = value.add(offset);
		return val.add(new UInt32(timePassed));
	}
}
