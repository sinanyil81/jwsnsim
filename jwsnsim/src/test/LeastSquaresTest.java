package test;

import static org.junit.Assert.*;

import org.junit.Test;

import application.regression.LeastSquares;
import application.regression.RegressionEntry;
import sim.type.UInt32;


public class LeastSquaresTest {
	public final static int NUM_ELEMENTS = 8;
	LeastSquares ls = new LeastSquares();
	RegressionEntry table[] = new RegressionEntry[8];
		
	int numElements = 0;
	int index = 0;
	
	void addElement(long x,long y){
		table[index].x = new UInt32(x);
		table[index].y = (int)(y-x);
		table[index].free = false;	
		
		index = (index + 1) % NUM_ELEMENTS;
		
		if (numElements < NUM_ELEMENTS)
			numElements++;
	}
	
	void clear(){
		for (int i = 0; i < table.length; i++) {
			table[i].free = true;
		}
		
		index = 0;
		numElements = 0;
	}
	
	@Test
	public void test(){
		for (int i = 0; i < table.length; i++) {
			table[i] = new RegressionEntry();
		}
		
		int epsilon = 2;
		double error;
		
		addElement(0x7b28be,0x896e7b);
	    addElement(0x1048612,0x112cba8);
	    addElement(0x18de365,0x19c28d5);
	    addElement(0x21740b9,0x2258602);
	    addElement(0x2a09e0c,0x2aee32e);
	    addElement(0x329fb5f,0x338405b);
	    addElement(0x3b358b2,0x3c19d87);
	    addElement(0x43cb605,0x44afab3);

	    ls.calculate(table, numElements);
	    
	    error = (ls.calculateY(new UInt32(0x4c61357))).getValue() - 0x4d457df;
	    assertTrue(Math.abs(error) <= epsilon);
	    
	    addElement(0x4c61357,0x4d457df);
	        
	    ls.calculate(table, numElements);
	    
        System.out.println("slope:"+ls.getSlope());
        System.out.println("meanX:"+ls.getMeanX());
        System.out.println("meanY:"+ls.getMeanY());

	    error = ls.calculateY(new UInt32(0x54f70a9)).getValue()- 0x55db50a;
	    assertTrue(Math.abs(error) <= epsilon);
	    
	    addElement(0x54f70a9,0x55db50a);
	    ls.calculate(table, numElements);
	    error = ls.calculateY(new UInt32(0x5d8cdfb)).getValue() - 0x5e71236;
	    assertTrue(Math.abs(error) <= epsilon);
	    
        addElement(0x5d8cdfb,0x5e71236);
        ls.calculate(table, numElements);
        error = ls.calculateY(new UInt32(0x6622b4e)).getValue() - 0x6706f62;
        assertTrue(Math.abs(error) <= epsilon);
        
	    addElement(0x6622b4e,0x6706f62);
	    ls.calculate(table, numElements);
	    error = ls.calculateY(new UInt32(0x6eb889f)).getValue() - 0x6f9cc8d;
	    assertTrue(Math.abs(error) <= epsilon);

        addElement(0x6eb889f,0x6f9cc8d);
        ls.calculate(table, numElements);
        error = ls.calculateY(new UInt32(0x774e5f1)).getValue() - 0x78329b8;
        assertTrue(Math.abs(error) <= epsilon);

	    addElement(0x774e5f1,0x78329b8);
	    ls.calculate(table, numElements);
	    error = ls.calculateY(new UInt32(0x7fe4343)).getValue() - 0x80c86e3;
	    assertTrue(Math.abs(error) <= epsilon);
	    
	    addElement(0x7fe4343,0x80c86e3);
	    ls.calculate(table, numElements);
	    error = ls.calculateY(new UInt32(0x887a094)).getValue() - 0x895e40e;
	    assertTrue(Math.abs(error) <= epsilon);
	    
        addElement(0x887a094,0x895e40e);
        ls.calculate(table, numElements);
        error = ls.calculateY(new UInt32(0x910fde7)).getValue() - 0x91f413a;
        assertTrue(Math.abs(error) <= epsilon);
        
        addElement(0x910fde7,0x91f413a);
        ls.calculate(table, numElements);
        error = ls.calculateY(new UInt32(0x99a5b37)).getValue() - 0x9a89e64;
        assertTrue(Math.abs(error) <= epsilon);
        
        addElement(0x99a5b37,0x9a89e64);
        ls.calculate(table, numElements);
        error = ls.calculateY(new UInt32(0xa23b889)).getValue()- 0xa31fb8f;
        assertTrue(Math.abs(error) <= epsilon);
        
        clear();
        
        addElement(149998493,150000000);
               
        ls.calculate(table, numElements);
        System.out.println("slope:"+ls.getSlope());
        System.out.println("meanX:"+ls.getMeanX());
        System.out.println("meanY:"+ls.getMeanY());
//        slope =0.0
//        meanX =149998495
//        meanY =1507
        addElement(179998192,180000000);
                
        ls.calculate(table, numElements);
        System.out.println("slope:"+ls.getSlope());
        System.out.println("meanX:"+ls.getMeanX());
        System.out.println("meanY:"+ls.getMeanY());//        slope =-7.057498E-8
//        meanX =2312481992
//        meanY =1658
        
        addElement(209997891,210000000);
               
        ls.calculate(table, numElements);
        System.out.println("slope:"+ls.getSlope());
        System.out.println("meanX:"+ls.getMeanX());
        System.out.println("meanY:"+ls.getMeanY());
//        slope =2.9362146E-9
//        meanX =3043309724
//        meanY =1808
        
        addElement(239997590,240000000);
                
        ls.calculate(table, numElements);
        System.out.println("slope:"+ls.getSlope());
        System.out.println("meanX:"+ls.getMeanX());
        System.out.println("meanY:"+ls.getMeanY());
//        slope =9.315497E-9
//        meanX =3416223515
//        meanY =1959

	}
}
