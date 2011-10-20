package test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sim.type.UInt32;


public class UInt32Test {

	@Test
	public void test(){
		UInt32 unsigned;
		UInt32 unsigned1;
		
		int x = 5;
		
		unsigned = new UInt32(x);
		assertTrue(unsigned.getValue() == 5);		
		
		x = -5;
		unsigned = new UInt32(x);
		assertTrue(unsigned.getValue() == 0xFFFFFFFBL);
		assertTrue(unsigned.toInteger()==-5);
		
		unsigned = new UInt32(0xFFFFFFFFL);
		unsigned1 = new UInt32(0xFFFFFFF0L);
		unsigned = unsigned.add(unsigned1);
		assertTrue(unsigned.getValue() == 0xFFFFFFF0L);
		
		unsigned = new UInt32(0xFFFFFFFFL);
		unsigned1 = new UInt32(0xFFFFFFF0L);
		unsigned = unsigned.subtract(unsigned1);
		assertTrue(unsigned.getValue() == 0x10L);
		
		unsigned = new UInt32(0xFFFFFFF0L);
		unsigned1 = new UInt32(0xFFFFFFFFL);
		unsigned = unsigned.subtract(unsigned1);
		assertTrue(unsigned.getValue() == 0xFFFFFFF1L);
	}
}
