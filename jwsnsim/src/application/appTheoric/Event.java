package application.appTheoric;

public class Event implements Comparable<Event> {

	private SimTime eventTime = new SimTime();
	private EventObserver observer = null;
	
	public Event(EventObserver observer){
		this.observer = observer;
	}
	
	public void register(double numTicks){
		eventTime = new SimTime(numTicks);
		eventTime = eventTime.add(Simulator.getInstance().getTime());
		Simulator.getInstance().register(this);		
	}
	
	public void unregister(){
		Simulator.getInstance().unregister(this);		
	}
	
	public SimTime getEventTime(){
		return eventTime;
	}
	
	@Override
	public int compareTo(Event arg0) {		
		return eventTime.compareTo(arg0.getEventTime());	
	}

	public void signalEvent() {
		observer.signal(this);
	}
}
