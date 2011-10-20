package test;


import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import sim.simulator.Event;
import sim.simulator.EventObserver;
import sim.simulator.Simulator;

public class SimulatorTest implements EventObserver{

	private Event firedEvent;	
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void test1(){
		Simulator sim = Simulator.getInstance();		
		sim.setRealTime(BigInteger.ZERO);
		assertTrue(sim.getRealTime().longValue() == 0);
		
		sim.tick();
		assertTrue(sim.getRealTime().longValue() == 0);
	}
	
	@Test
	public void test2(){
		Simulator sim = Simulator.getInstance();			
		Event event = new Event(this);
		
		event.register(Integer.MAX_VALUE);
		sim.tick();
		assertTrue(event.equals(firedEvent));
		assertTrue(sim.getRealTime().longValue() == Integer.MAX_VALUE);
		
		event = new Event(this);
		event.register(Integer.MAX_VALUE);
		sim.tick();
		assertTrue(event.equals(firedEvent));
	}
	
	@Test
	public void test3(){
		Simulator sim = Simulator.getInstance();			
		Event event1 = new Event(this);
		Event event2 = new Event(this);
		Event event3 = new Event(this);
		
		event1.register(Integer.MAX_VALUE-2);
		event2.register(Integer.MAX_VALUE-1);
		event3.register(Integer.MAX_VALUE);
		
		sim.tick();
		assertTrue(event1.equals(firedEvent));
		sim.tick();
		assertTrue(event2.equals(firedEvent));
		sim.tick();
		assertTrue(event3.equals(firedEvent));
	}
	
	@Test
	public void test4(){
		Simulator sim = Simulator.getInstance();			
		Event event1 = new Event(this);
		Event event2 = new Event(this);
		Event event3 = new Event(this);
		sim.setRealTime(BigInteger.valueOf(Long.MAX_VALUE));
		
		event1.register(Integer.MAX_VALUE-2);
		event2.register(Integer.MAX_VALUE-1);
		event3.register(Integer.MAX_VALUE);
		
		sim.tick();
		assertTrue(event1.equals(firedEvent));
		sim.tick();
		assertTrue(event2.equals(firedEvent));
		sim.tick();
		assertTrue(event3.equals(firedEvent));
		
		firedEvent = null;
		sim.tick();
		assertTrue(firedEvent == null);
	}

	@Override
	public void signal(Event event) {
		firedEvent = event;
	}

}
