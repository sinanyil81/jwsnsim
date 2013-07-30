package sim.jprowler.clock;
import sim.jprowler.SimTime;
import sim.jprowler.Simulator;
import sim.jprowler.UInt32;
/**
 * 
 * @author K. Sinan YILDIRIM
 * 
 * Simulates 32 bit 1 MHz hardware clock which has constant 
 * drift. 
 *
 */

public class ConstantDriftClock implements Clock {

	/** Clock specific constants */
	private static final int MEAN_DRIFT = 50;
	private static final int DRIFT_VARIANCE = 300;
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
		drift = MEAN_DRIFT + Simulator.random.nextGaussian() * Math.sqrt(DRIFT_VARIANCE);
		
		drift /= 1000000.0;
		
		/* to start clock with a random value */
		setValue(new UInt32(Math.abs(Simulator.random.nextInt())));
		
		System.out.println(drift);
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
		this.drift = drift;
	}

	@Override
	public void start() {
		started = true;	
		lastRead = Simulator.getInstance().getTime();
	}
	
	public void stop(){
		started = false;
		this.clock = 0.0;
	}

	@Override
	public void setValue(UInt32 value) {
		this.clock = value.toDouble();		
	}
}
