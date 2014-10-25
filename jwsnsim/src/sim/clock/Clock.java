package sim.clock;

import sim.type.Register;

public interface Clock {
	public void start();
	public Register getValue();
	public void setValue(Register value);
	public double getDrift();
	public void setDrift(double drift);
}
