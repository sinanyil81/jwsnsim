package sim.clock;

import sim.type.UInt32;

public interface Clock {
	public void start();
	public void progress(double amount);	
	public UInt32 getValue();
	public double getDrift();
	public void setDrift(double drift);
}
