package application.appSelfFlooding;

import hardware.Register32;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;

public class LogicalClock {

	public Register32 value = new Register32();

//	public AVT rate = new AVTBuilder()
//							.upperBound(0.0001)
//							.lowerBound(-0.0001)
//							.deltaMin(0.0000000001)
//							.deltaMax(0.00001)
//							.build();	
	
	public AvtSimple rate = new AvtSimple(-0.0002f, 0.0002f, 0.0f, 0.000000000001f, 0.00001f);
	Register32 updateLocalTime = new Register32();
	
	public LogicalClock(){
//		double deltaMax = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMax();
//		double deltaMin = rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().getDeltaMin();
//		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM().setDelta(deltaMax);
	}

	public void update(Register32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate.getValue());

		value = value.add(timePassed);
		this.updateLocalTime = new Register32(local);
	}

	public Register32 getValue(Register32 local) {
		int timePassed = local.subtract(updateLocalTime).toInteger();
		timePassed += (int) (((float) timePassed) * rate.getValue());

		return value.add(new Register32(timePassed));
	}

	public void setValue(Register32 time, Register32 local) {
		value = new Register32(time);
		this.updateLocalTime = new Register32(local);
	}
}
