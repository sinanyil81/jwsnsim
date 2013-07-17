package sim.jprowler;

public class SimTime {
	
	private long timeHigh = 0;
	private double timeLow  = 0.0;
	
	public SimTime(){
		timeHigh = 0;
		timeLow = 0.0;
	}
	
	public SimTime(SimTime s){
		timeHigh = s.timeHigh;
		timeLow = s.timeLow;
	}
	
	public SimTime(double value){
		timeHigh = (long)value;
		timeLow = value - (long)value;
	}
	
	public SimTime(long high,double low){
		timeHigh = high;
		timeLow = low;
	}
	
	public long getTimeHigh() {
		return timeHigh;
	}
	
	public void setTimeHigh(long timeHigh) {
		this.timeHigh = timeHigh;
	}
	
	public double getTimeLow() {
		return timeLow;
	}
	
	public void setTimeLow(double timeLow) {
		this.timeLow = timeLow;
	}
	
	public SimTime add(SimTime time){
		double lowSum = time.getTimeLow() + timeLow;
		long highSum = timeHigh +(long)lowSum + time.getTimeHigh();
		lowSum -= (double)((long)lowSum);
		
		if((lowSum < 0) && (highSum > 0)){
			highSum--;
			lowSum += 1.0;
		}
		else if((lowSum > 0) && (highSum < 0)){
			highSum++;
			lowSum -= 1.0;
		}
		
		SimTime ret = new SimTime(highSum,lowSum);
		
		return ret;
	}
	
	public SimTime sub(SimTime time){
		
		if(timeLow < time.getTimeLow()){
			timeHigh--;
			timeLow += 1.0;
		}
		
		double lowDiff = timeLow - time.getTimeLow();
		long highDiff = timeHigh - time.getTimeHigh();
		
		if(highDiff <0){
			highDiff++;
			lowDiff -= 1.0;
		}
		
		SimTime ret = new SimTime(highDiff,lowDiff);
		
		return ret;
	}
		
	public int compareTo(SimTime time){
		
		if(this.timeHigh > time.getTimeHigh()){
			return 1;
		}
		else if(this.timeHigh == time.getTimeHigh()){
			if(this.timeLow > time.getTimeLow())
				return 1;
			else if(this.timeLow == time.getTimeLow())
				return 0;
			else
				return -1;
		}
		else
			return -1;
	}
	
	public double toDouble(){
		
		return (double)timeHigh + timeLow; 
	}
	
	public String toString(){
		return String.valueOf(timeHigh);
	}
}