package sim.simulator;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

import sim.clock.Clock;
import sim.node.Node;

public class Simulator {
	
	private static Simulator simulator = null;
	private SimTime simTime = new SimTime();
	
	private Vector<Event> events = null;
	
	protected Simulator(){
		events = new Vector<Event>();
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
	
	public void tick() {
		Event eventToFire;
		
		try{
			eventToFire = events.remove(0);	
		}
		catch (NoSuchElementException e) {
			eventToFire = null;
		}
		
		if(eventToFire != null){
			simTime = eventToFire.getEventTime();						
			eventToFire.signalEvent();
		}			
	}
	
	
	public void reset(){
		events.removeAllElements();		
		events = new Vector<Event>();		
		simTime = new SimTime();
//		random.setSeed(SIMULATOR_SEED);
	}
	
	public SimTime getTime(){
		return simTime;
	}
	
	public long getSecond(){
		return getTime().getTimeHigh()/1024/1024;
	}
}
