package sim.simulator;

import java.math.BigInteger;

public class Event implements Comparable<Event> {

	private BigInteger eventTime = BigInteger.ZERO;
	private EventObserver observer = null;
	
	public Event(EventObserver observer){
		this.observer = observer;
	}
	
	public void register(int numTicks){
		eventTime = BigInteger.valueOf(numTicks);
		eventTime = eventTime.add(Simulator.getInstance().getRealTime());
		Simulator.getInstance().register(this);		
	}
	
	public void unregister(){
		Simulator.getInstance().unregister(this);		
	}
	
	public BigInteger getEventTime(){
		return eventTime;
	}
	
	@Override
	public int compareTo(Event arg0) {		
		return (eventTime.subtract(arg0.getEventTime())).intValue();	
	}

	public void signalEvent() {
		observer.signal(this);
	}
}
