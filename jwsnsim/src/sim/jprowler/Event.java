package sim.jprowler;

public class Event implements Comparable<Event> {

	private SimTime eventTime = new SimTime();
	
	public void register(int numTicks){
		eventTime = new SimTime(numTicks);
		eventTime = eventTime.add(Simulator.getInstance().getTime());
		Simulator.getInstance().addEvent(this);		
	}
	
	public void unregister(){
		Simulator.getInstance().removeEvent(this);		
	}
	
	public void execute(){
		
	}
	
	public SimTime getEventTime(){
		return eventTime;
	}
	
	@Override
	public int compareTo(Event arg0) {		
		return eventTime.compareTo(arg0.getEventTime());	
	}
}
