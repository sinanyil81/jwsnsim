package hardware;

import sim.simulator.SimTime;

public abstract class Counter32 {
	
	protected static double MAX_CLOCK =  4294967295.0;
	
	/** Constant drift of the hardware clock */
	protected double drift = 0.0;
	
	/** Value of the clock register */
	protected double clock = 0.0;
	
	/** is started? */
	protected boolean started = false;
	
	/* last read time */
	protected SimTime lastRead = new SimTime();
	
	public abstract void start();
	public abstract Register32 getValue();
	public abstract void setValue(Register32 value);
	public abstract double getDrift();
	public abstract void setDrift(double drift);
}
