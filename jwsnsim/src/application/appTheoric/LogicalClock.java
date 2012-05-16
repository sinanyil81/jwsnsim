package application.appTheoric;

public class LogicalClock {
	double value = 0;
	double mult = 1;
	
	private HardwareClock clock;	
	double lastReadTime = 0;
				
	public LogicalClock(HardwareClock clock){
		this.clock= clock;
	}
	
	public void setMult(double mult){
		update();
		this.mult = mult;
	}
	
	public double getMult(){
		return mult;
	}
	
	private void update(){
		double readTime = clock.read();
		double diff = readTime - lastReadTime;

		value += diff*mult;
		
		lastReadTime = readTime;
	}		
	
	public double getValue(){
		update();
		return value;
	}
}