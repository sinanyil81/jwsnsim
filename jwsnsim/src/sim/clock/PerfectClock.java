package sim.clock;

import sim.simulator.SimTime;
import sim.simulator.Simulator;
import sim.statistics.Distribution;
import sim.statistics.GaussianDistribution;
import sim.type.UInt32;
/**
 * 
 * @author K. Sinan YILDIRIM
 * 
 * Simulates 32 bit 1 MHz hardware clock which has constant 
 * drift. 
 *
 */

public class PerfectClock implements Clock {

	private static double MAX_CLOCK =  4294967295.0;
	
	/** Constant drift of the hardware clock */
	private double drift = 0.0;
	
	/** Value of the clock register */
	private double clock = 0.0;
	
	/** is started? */
	private boolean started = false;
	
	/* last read time */
	private SimTime lastRead = new SimTime();
	
	public PerfectClock(){
	}
	
	private void progress(double amount){
		
		if(!started)
			return;
		
		/* Progress clock by considering the constant drift. */
		clock += amount;		
		
		/* Check if wraparound has occured. */
		if(clock > MAX_CLOCK){
			clock -= MAX_CLOCK;
		}
	}
	
	public UInt32 getValue(){
		SimTime currentTime = Simulator.getInstance().getTime();
		progress(currentTime.sub(lastRead).toDouble());
		lastRead = currentTime;
		
		return new UInt32((long)clock);
	}

	public double getDrift() {
		return drift;
	}
	
	public void setDrift(double drift){

	}

	@Override
	public void start() {
		started = true;	
		lastRead = Simulator.getInstance().getTime();
	}

	@Override
	public void setValue(UInt32 value) {
		this.clock = value.toDouble();		
	}
}