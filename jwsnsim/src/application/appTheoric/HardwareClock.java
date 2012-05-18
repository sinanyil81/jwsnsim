package application.appTheoric;

public class HardwareClock implements Clock {
	
	/** Clock specific constants */
	private static final int MEAN_DRIFT = 50;
	private static final int DRIFT_VARIANCE = 100;
		
	/** Constant drift of the hardware clock */
	private double drift = 0.0;
	
	/** Value of the clock register */
	private SimTime clock = new SimTime();
	
	/** is started? */
	private boolean started = false;
	
	public HardwareClock(){
		drift = MEAN_DRIFT + Simulator.random.nextGaussian() * Math.sqrt(DRIFT_VARIANCE);  
		drift /= 1000000.0;
		System.out.println("HW: "+ drift);
		Simulator.getInstance().register(this);
	}
	
	public HardwareClock(double drift){
		this.drift = drift;
		Simulator.getInstance().register(this);
	}
		
	public void progress(double amount){
		
		if(!started)
			return;
		
		/* Progress clock by considering the constant drift. */
		clock = clock.add(new SimTime(amount));
		clock = clock.add(new SimTime(amount*drift));	
	}
	
	public SimTime read(){
		return clock;
	}
	
	public void start() {
		started = true;		
	}

	@Override
	public double getDrift() {
		
		return drift;
	}
}
