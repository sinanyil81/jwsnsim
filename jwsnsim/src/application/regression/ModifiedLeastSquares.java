package application.regression;

import hardware.Register;

public class ModifiedLeastSquares {
	
	private float slope = 0.0f;
	private Register meanX = new Register();
	private int meanY = 0;
	private Register offset = new Register();
	
	public void calculate(RegressionEntry table[], int tableEntries){
		float newSlope = slope;
        
		Register newMeanX;
        int newMeanY;        
        int meanXRest;
        int meanYRest;

        long xSum;
        long ySum;

        int i;

        for(i = 0; i < table.length && table[i].free; ++i)
            ;

        if( i >= table.length )  // table is empty
            return;
/*
        We use a rough approximation first to avoid time overflow errors. The idea
        is that all times in the table should be relatively close to each other.
*/
        newMeanX = table[i].x;
        newMeanY = table[i].y;

        xSum = 0;
        meanXRest = 0;
        ySum = 0;
        meanYRest = 0;

        while( ++i < table.length )
            if( !table[i].free) {                
            	Register diff = table[i].x.subtract(newMeanX);
            	
            	xSum += diff.toInteger() / tableEntries;
            	meanXRest += diff.toInteger() % tableEntries;
                ySum += (table[i].y - newMeanY) / tableEntries;
                meanYRest += (table[i].y - newMeanY) % tableEntries;
            }
        
        
        xSum = (new Register(xSum).add(new Register(meanXRest/tableEntries))).toLong();    
        newMeanX =  newMeanX.add(new Register(xSum));
        
        newMeanY += ySum + meanYRest / tableEntries;

        xSum = ySum = 0;
        for(i = 0; i < table.length; ++i)
            if( !table[i].free) {
                int a = (table[i].x.subtract(newMeanX)).toInteger();                
                int b = table[i].y - newMeanY;

                xSum += (long)a * a;
                ySum += (long)a * b;
            }

        if( xSum != 0 )
            newSlope = (float)ySum / (float)xSum;

        slope = newSlope;
        meanY = newMeanY;
        meanX = new Register(newMeanX);
        
        offset = new Register(newMeanY);
        newMeanX = newMeanX.multiply(slope);
        offset = offset.subtract(newMeanX);
	}
	
	public float getSlope() {
		return slope;
	}

	public void setSlope(float slope) {
		this.slope = slope;
	}

	public int getOffset() {
		return offset.toInteger();
	}

	public void setOffset(int offset) {
		if(this.offset.toInteger() != offset){
			int val = meanY-offset-meanX.multiply(slope).toInteger();
			val = (int) (((float)val)/slope);
			meanX.add(val); 			
		}
		
		this.offset = new Register(offset);
	}
		
	public Register calculateY(Register x) {
		Register result = new Register(x);

		result = result.subtract(meanX);		
		result = result.multiply(slope);
		result = result.add(meanY);
		result = result.add(x);	
				
        return result;
	}
}
