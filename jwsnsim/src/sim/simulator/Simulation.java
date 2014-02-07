package sim.simulator;

public abstract class Simulation {
	protected long MAXSECOND =20000;
	
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
