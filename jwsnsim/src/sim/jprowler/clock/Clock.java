package sim.jprowler.clock;

import sim.type.UInt32;

public interface Clock {
	public void start();
	public UInt32 getValue();
	public void setValue(UInt32 value);
	public double getDrift();
	public void setDrift(double drift);
	public void stop();
}
