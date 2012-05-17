package application.appTheoric;

public abstract class Application {
	public static long MAXSECOND = 500000;
	
	public void run(){
		
		while(true){
			Simulator.getInstance().tick();
			
			SimTime t = Simulator.getInstance().getTime(); 
			long second = t.getTimeHigh()/32/1024; 
			if(second > MAXSECOND)
				break;
		}
		
		Simulator.getInstance().reset();
		
		exit();
	}
	
	public abstract void exit();
}