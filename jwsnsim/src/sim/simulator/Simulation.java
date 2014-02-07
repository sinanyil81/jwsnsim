package sim.simulator;

public abstract class Simulation {
	public long MAXSECOND = 0;
	
	protected Simulation(int seconds){
		this.MAXSECOND = seconds;
	}
	
	public void run(){
		
		while(Simulator.getInstance().getSecond() < MAXSECOND){
			Simulator.getInstance().tick();
		}
		
		Simulator.getInstance().reset();
		
		exit();
	}
	
	public abstract void exit();
}
