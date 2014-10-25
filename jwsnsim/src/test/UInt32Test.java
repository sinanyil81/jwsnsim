package test;

import static org.junit.Assert.assertTrue;
import hardware.Register32;

import org.junit.Test;


public class UInt32Test {

	@Test
	public void test(){
		Register32 unsigned;
		Register32 unsigned1;
		
		int x = 5;
		
		unsigned = new Register32(x);
		assertTrue(unsigned.toLong() == 5);		
		
		x = -5;
		unsigned = new Register32(x);
		assertTrue(unsigned.toLong() == 0xFFFFFFFBL);
		assertTrue(unsigned.toInteger()==-5);
		
		unsigned = new Register32(0xFFFFFFFFL);
		unsigned1 = new Register32(0xFFFFFFF0L);
		unsigned = unsigned.add(unsigned1);
		assertTrue(unsigned.toLong() == 0xFFFFFFF0L);
		
		unsigned = new Register32(0xFFFFFFFFL);
		unsigned1 = new Register32(0xFFFFFFF0L);
		unsigned = unsigned.subtract(unsigned1);
		assertTrue(unsigned.toLong() == 0x10L);
		
		unsigned = new Register32(0xFFFFFFF0L);
		unsigned1 = new Register32(0xFFFFFFFFL);
		unsigned = unsigned.subtract(unsigned1);
		assertTrue(unsigned.toLong() == 0xFFFFFFF1L);
	}
}
