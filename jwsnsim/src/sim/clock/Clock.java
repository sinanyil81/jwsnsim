package sim.clock;

import sim.type.UInt32;

public interface Clock {
	public void start();
	public UInt32 getValue();
	public double getDrift();
	public void setDrift(double drift);
}
