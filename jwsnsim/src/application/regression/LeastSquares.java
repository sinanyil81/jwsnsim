package application.regression;

import hardware.Register;

public class LeastSquares {
	
	private float slope = 0.0f;
	private Register meanX = new Register();
	private int meanY = 0;
	
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
	}
	
	private long getWeightDivisor(RegressionEntry table[], int tableEntries){
		long sum = 0;
		
		for(int i = 0; i <= table.length-2; ++i){
			for(int j = i; j <= table.length-1; ++j){
				int a = (table[i].x.subtract(table[j].x)).toInteger();
				sum += (long)a * a;				
			}			
		}
		
		return sum;		
	}
	
	public String getSlopeWeights(RegressionEntry table[], int tableEntries){
		String s = "";
		
		long divisor = getWeightDivisor(table, tableEntries);
		
		for(int i = 0; i <= table.length-1; ++i){
			s += "("+i+"=" + table[i].x.toLong() + ") ";
		}
		
		s += "\n";
		
		for(int i = 0; i <= table.length-1; ++i){
			s += "("+i+"="  + table[i].y + ") ";
		}
		
		s += "\n";
		
		for(int i = 0; i <= table.length-1; ++i){
			double sum = 0;
			for(int j = 0; j <= table.length-1; ++j){
				int a = (table[i].x.subtract(table[j].x)).toInteger();
				int b = table[i].y - table[j].y;
				long a2 = (long)a * a;
								
				double weight = ((double)a2/(double)divisor);
				double slope = (double)b/(double)a;
				//weight /= (double)a;
					
				s += "("+i+","+j+") " + "weight=" + weight + " slope=" + slope;
				
				if(i!=j){
					sum += weight;
					s += " contribution=" + weight*slope + " ls-slope="+this.slope;
				}				
				
				s += "\n";
			}	
			
			s += sum + "\n";
		}
		
		return s;
	}
	
	public float getSlope() {
		return slope;
	}

	public void setSlope(float slope) {
		this.slope = slope;
	}

	public Register getMeanX() {
		return meanX;
	}

	public void setMeanX(Register meanX) {
		this.meanX = meanX;
	}

	public int getMeanY() {
		return meanY;
	}

	public void setMeanY(int meanY) {
		this.meanY = meanY;
	}	
	
	public void clear(){
		slope = 0.0f;
		meanX = new Register();
		meanY = 0;
	}
	
	public Register calculateY(Register x) {
		Register result = new Register(x);

		result = result.subtract(meanX);		
		result = result.multiply(slope);
		result = result.add(meanY);
		result = result.add(x);	
				
        return result;
	}
	
	public void shift(int shiftVal) {
    	this.meanY += shiftVal;
    	int xOffset = (int) ((float)shiftVal/slope);
    	setMeanX(meanX.subtract(xOffset));
	}
	
	public Register calculateY(Register x,Register meanX,int meanY) {
		Register result = new Register(x);

		result = result.subtract(meanX);		
		result = result.multiply(slope);
		result = result.add(meanY);
		result = result.add(x);
		
        return result;
	}	
}
