package application.appTheoric;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

public class Simulator {
	static private long SIMULATOR_SEED = 0x123456L;	
	static public Random random = new Random(SIMULATOR_SEED);
//	static public Random random = new Random();
	
	private static Simulator simulator = null;
	private SimTime simTime = new SimTime();
		
	private Vector<Event> events = null;
	private Vector<Clock> clocks = null;
	
	protected Simulator(){
		events = new Vector<Event>();
		clocks = new Vector<Clock>();
	}

	public static Simulator getInstance() {
		if(simulator == null){
			simulator = new Simulator();
		}

		return simulator;
	}
	
	public void register(Event event) {
		events.add(event);
		Collections.sort(events);		
	}
	
	public void unregister(Event event) {
		events.removeElement(event);	
		Collections.sort(events);
	}
	
	public void register(Clock clock){
		clocks.add(clock);
	}
	
	public void unregister(Clock clock){
		clocks.removeElement(clock);
	}
	
	public void tick() {
		Event eventToFire;
		
		try{
			eventToFire = events.remove(0);	
		}
		catch (NoSuchElementException e) {
			eventToFire = null;
		}
		
		if(eventToFire != null){
			
			SimTime future = eventToFire.getEventTime();
			progressClocks(future.sub(simTime).toDouble());
			simTime = future;		
			eventToFire.signalEvent();
		}			
	}
	
	private void progressClocks(double d) {
		for (Iterator<Clock> iterator = clocks.iterator(); iterator.hasNext();) {
			Clock clock = (Clock) iterator.next();
			clock.progress(d);
		}		
	}

	public void reset(){
		events.removeAllElements();	
		clocks.removeAllElements();
		
		events = new Vector<Event>();
		clocks = new Vector<Clock>();
		simTime = new SimTime();
	}
	
	public SimTime getTime(){
		return simTime;
	}
}
