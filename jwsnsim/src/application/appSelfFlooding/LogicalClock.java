package application.appSelfFlooding;

import sim.type.UInt32;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;

public class LogicalClock {

	public UInt32 value = new UInt32();

//	public AVT rate = new AVTBuilder()
//							.upperBound(0.0001)
//							.lowerBound(-0.0001)
//							.deltaMin(0.0000000001)
//							.deltaMax(0.00001)
//							.build();	
	
	public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f, 0.000000001f, 0.00001f);
	UInt32 updateLocalTime = new UInt32();
	
	UInt32 meanX = new UInt32();
	int meanY = 0;
	
	
	public LogicalClock(){
//		double deltaMax = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMax();
//		double deltaMin = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMin();
//		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().setDelta(deltaMax);
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

		return value.add(new UInt32(timePassed));
	}

	public void setValue(UInt32 time, UInt32 local) {
		value = new UInt32(time);
		this.updateLocalTime = new UInt32(local);
	}
	
	public UInt32 calculateY(UInt32 x) {
		UInt32 diff = new UInt32(x);
		diff = diff.subtract(meanX);

		int mult = (int) (rate.getValue() * (float) (diff.toInteger()));
		mult += meanY;

		UInt32 result = x.add(new UInt32(mult));
		return result;
	}
}
