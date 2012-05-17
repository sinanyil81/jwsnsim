package application.appTheoric;

public class LogicalClock {
	SimTime value = new SimTime();
	double mult = 1;
	
	private HardwareClock clock;	
	SimTime lastReadTime = new SimTime();
				
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
		double diff = clock.read().sub(lastReadTime).toDouble();

		value = value.add(new SimTime(diff*mult));		
		lastReadTime = clock.read();
	}		
	
	public SimTime getValue(){
		update();
		return value;
	}
	
	public void setValue(SimTime val){
		value = val;
	}
}