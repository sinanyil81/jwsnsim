package test;

import static org.junit.Assert.*;

import org.junit.Test;

import application.appTheoric.SimTime;

public class SimTest {

	@Test
	public void test() {
		SimTime s1 = new SimTime(1.5);
		SimTime s2 = new SimTime(3.3);		
		System.out.println(s1.add(s2).toString());
		
		s1 = new SimTime(1.5);
		s2 = new SimTime(-3.3);
		System.out.println(s1.add(s2).toString());
		
		s1 = new SimTime(-1.5);
		s2 = new SimTime(3.3);
		System.out.println(s1.add(s2).toString());
		
		s1 = new SimTime(-1.3);
		s2 = new SimTime(3.5);
		System.out.println(s1.add(s2).toString());
		
		s1 = new SimTime(1.5);
		s2 = new SimTime(3.3);		
		System.out.println(s1.sub(s2).toString());
		
		s1 = new SimTime(1.5);
		s2 = new SimTime(-3.3);
		System.out.println(s1.sub(s2).toString());
		
		s1 = new SimTime(-1.5);
		s2 = new SimTime(3.3);
		System.out.println(s1.sub(s2).toString());
		
		s1 = new SimTime(-1.3);
		s2 = new SimTime(3.5);
		System.out.println(s1.sub(s2).toString());
	}
}
