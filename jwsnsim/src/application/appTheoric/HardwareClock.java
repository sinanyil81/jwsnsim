package application.appTheoric;

import sim.clock.Clock;
import sim.simulator.Simulator;
import sim.type.UInt32;


public class HardwareClock implements Clock {
	
	/** Value of the clock register */
	private double clock = 0.0;
	
	/** is started? */
	private boolean started = false;
	
	public HardwareClock(){
		Simulator.getInstance().register(this);
	}
	
	public HardwareClock(double drift){ 
		Simulator.getInstance().register(this);
	}
	
	public void progress(double amount){
		
		if(!started)
			return;
		
		clock += amount;		
	}
	
	public UInt32 getValue(){
		return new UInt32();
	}
	
	public double read(){
		return clock;
	}

	public double getDrift() {
		return 0;
	}
	
	public void setDrift(double drift){
		
	}

	@Override
	public void start() {
		started = true;		
	}
}
