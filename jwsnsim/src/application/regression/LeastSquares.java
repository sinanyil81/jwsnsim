package application.regression;

import sim.type.UInt32;

public class LeastSquares {
	
	private float slope = 0.0f;
	private UInt32 meanX = new UInt32();
	private int meanY = 0;
	
	public void calculate(RegressionEntry table[], int tableEntries){
		float newSlope = slope;
        
		UInt32 newMeanX;
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
            	UInt32 diff = table[i].x.subtract(newMeanX);

//            	xSum += diff.getValue() / tableEntries;
//            	meanXRest += diff.getValue() % tableEntries;
            	
            	xSum += diff.toInteger() / tableEntries;
            	meanXRest += diff.toInteger() % tableEntries;
            	
//            	localSum += (table[i].localTime - newLocalAverage) / tableEntries;
//            	localAverageRest += (table[i].localTime - newLocalAverage) % tableEntries;
            	               
                ySum += (table[i].y - newMeanY) / tableEntries;
                meanYRest += (table[i].y - newMeanY) % tableEntries;
            }
        
        
        xSum = (new UInt32(xSum).add(new UInt32(meanXRest/tableEntries))).getValue();    
        newMeanX =  newMeanX.add(new UInt32(xSum));
        
        newMeanY += ySum + meanYRest / tableEntries;

        xSum = ySum = 0;
        for(i = 0; i < table.length; ++i)
            if( !table[i].free) {
                int a = (table[i].x.subtract(newMeanX)).toInteger();
                //int a = table[i].x.toInteger() - newMeanX.toInteger();
                int b = table[i].y - newMeanY;

                xSum += (long)a * a;
                ySum += (long)a * b;
            }

        if( xSum != 0 )
            newSlope = (float)ySum / (float)xSum;

        slope = newSlope;
        meanY = newMeanY;
        meanX = new UInt32(newMeanX);
	}
	
	public float getSlope() {
		return slope;
	}

	public void setSlope(float slope) {
		this.slope = slope;
	}

	public UInt32 getMeanX() {
		return meanX;
	}

	public void setMeanX(UInt32 meanX) {
		this.meanX = meanX;
	}

	public int getMeanY() {
		return meanY;
	}

	public void setMeanY(int meanY) {
		this.meanY = meanY;
	}
	
	public UInt32 calculateY(UInt32 x) {
		UInt32 result = new UInt32(x);

		result = result.subtract(meanX);		
		result = result.multiply(slope);
		result = result.add(meanY);
		result = result.add(x);
		
        return result;
	}
	
	public UInt32 calculateY(UInt32 x,UInt32 meanX,int meanY) {
		UInt32 result = new UInt32(x);

		result = result.subtract(meanX);		
		result = result.multiply(slope);
		result = result.add(meanY);
		result = result.add(x);
		
        return result;
	}
}
