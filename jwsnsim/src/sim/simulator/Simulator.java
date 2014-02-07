package sim.simulator;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Vector;


public class Simulator {
	
	private static Simulator simulator = null;
	private SimTime simTime = new SimTime();
	
	private Vector<Event> events = null;
	
	private Simulation simulation = null;
	
	protected Simulator(){
		events = new Vector<Event>();
	}

	public static Simulator getInstance() {
		if(simulator == null){
			simulator = new Simulator();
		}

		return simulator;
	}
	
	public void startSimulation(Simulation simulation){
		this.simulation = simulation;
		simulation.run();
	}
	
	public void stopSimulation(){
		if(this.simulation != null){
			simulation.exit();
		}
	}
	
	public Simulation getSimulation(){
		return simulation;
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
	}
	
	public SimTime getTime(){
		return simTime;
	}
	
	public long getSecond(){
		return getTime().getTimeHigh()/1024/1024;
	}
}
