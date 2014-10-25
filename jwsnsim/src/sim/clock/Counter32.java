package sim.clock;

import sim.type.Register;

public interface Counter32 {
	public void start();
	public Register getValue();
	public void setValue(Register value);
	public double getDrift();
	public void setDrift(double drift);
}
