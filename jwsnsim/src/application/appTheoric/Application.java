package application.appTheoric;

public abstract class Application {
	public static long MAXSECOND = 150000;
	
	public void run(){
		
		while(Simulator.getInstance().getTime().getTimeHigh()*32 < MAXSECOND){
			Simulator.getInstance().tick();
		}
		
		Simulator.getInstance().reset();
		
		exit();
	}
	
	public abstract void exit();
}