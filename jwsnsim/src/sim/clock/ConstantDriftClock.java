package sim.clock;

import sim.simulator.SimTime;
import sim.simulator.Simulator;
import sim.statistics.Distribution;
import sim.statistics.GaussianDistribution;
import sim.type.Register;
/**
 * 
 * @author K. Sinan YILDIRIM
 * 
 * Simulates 32 bit 1 MHz hardware clock which has constant 
 * drift. 
 *
 */

public class ConstantDriftClock implements Counter32 {

	/** Clock specific constants */
	private static final int MEAN_DRIFT = 50;
	private static final int DRIFT_VARIANCE = 100;
	private static double MAX_CLOCK =  4294967295.0;	
	
	/** Constant drift of the hardware clock */
	private double drift = 0.0;
	
	/** Value of the clock register */
	private double clock = 0.0;
	
	/** is started? */
	private boolean started = false;
	
	/* last read time */
	private SimTime lastRead = new SimTime();
	
	public ConstantDriftClock(){
		drift = GaussianDistribution.nextGaussian(MEAN_DRIFT, DRIFT_VARIANCE); 
//		if(drift < 0)
//			drift = 0;
		System.out.println(drift);
		
		drift /= 1000000.0;
	}
	
	public ConstantDriftClock(double drift){ 
		this.drift = drift;
	}
	
	private void progress(double amount){
		
		if(!started)
			return;
		
		/* Progress clock by considering the constant drift. */
		clock += amount + amount*drift;		
		
		/* Check if wraparound has occured. */
		if(clock > MAX_CLOCK){
			clock -= MAX_CLOCK;
		}
	}
	
	public Register getValue(){
		SimTime currentTime = Simulator.getInstance().getTime();
		progress(currentTime.sub(lastRead).toDouble());
		lastRead = currentTime;
		
		return new Register((long)clock);
	}

	public double getDrift() {
		return drift;
	}
	
	public void setDrift(double drift){
		this.drift = drift;
	}

	@Override
	public void start() {
		started = true;	
		lastRead = Simulator.getInstance().getTime();
	}

	@Override
	public void setValue(Register value) {
		this.clock = value.toDouble();		
	}
}
