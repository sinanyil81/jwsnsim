package application.regression;

import sim.type.Register;

public class MinimumVarianceSlopeRegression {

	private float slope = 0.0f;
	private Register meanX = new Register();
	private int meanY = 0;

	public void calculate(RegressionEntry table[], int tableEntries) {
		float newSlope = slope;

		Register newMeanX;
		int newMeanY;
		int meanXRest;
		int meanYRest;

		long xSum;
		long ySum;

		int i;
		
		if(tableEntries < 2) return;

		i = 0; 
		newMeanX = table[i].x;
		newMeanY = table[i].y;

		xSum = 0;
		meanXRest = 0;
		ySum = 0;
		meanYRest = 0;

		while (++i < tableEntries)
			if (!table[i].free) {
				Register diff = table[i].x.subtract(newMeanX);
				xSum += diff.toInteger() / tableEntries;
				meanXRest += diff.toInteger() % tableEntries;

				ySum += (table[i].y - newMeanY) / tableEntries;
				meanYRest += (table[i].y - newMeanY) % tableEntries;
			}

		xSum = (new Register(xSum).add(new Register(meanXRest / tableEntries)))
				.toLong();
		newMeanX = newMeanX.add(new Register(xSum));

		newMeanY += ySum + meanYRest / tableEntries;

		xSum = ySum = 0;

		newSlope = 0;

		int a = table[tableEntries-1].y - table[0].y;
		int b = (table[tableEntries-1].x.subtract(table[0].x)).toInteger();
		if (b != 0)
			newSlope = (float) a / (float) b;


//		float[] slopes = new float[tableEntries - 1];
//		
//		int j = 0;				
//		for (i = 1; i < tableEntries; i++) {
//			/* compute consecutive slopes */
//			{
//				int a = table[i].y - table[i - 1].y;
//				int b = (table[i].x.subtract(table[i - 1].x)).toInteger();
//
//				if (b != 0) {
//					slopes[j++] = (float) a / (float) b;
//				}
//				
//			}
//		}

//		Arrays.sort(slopes);
//
//		i = j >> 1;
//		if ((j & 0x1) == 1) {
//			newSlope = slopes[i];			
//		} else {
//			newSlope = (slopes[i] + slopes[i - 1]) / 2.0f;
//		}

		slope = newSlope;
		meanY = newMeanY;
		meanX = new Register(newMeanX);
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

	public Register calculateY(Register x) {
		Register diff = new Register(x);
		diff = diff.subtract(meanX);

		int mult = (int) (slope * (float) (diff.toInteger()));
		mult += meanY;

		Register result = x.add(new Register(mult));
		return result;
	}
	
	public Register calculateY(Register x,float slope) {
		Register diff = new Register(x);
		diff = diff.subtract(meanX);

		int mult = (int) (slope * (float) (diff.toInteger()));
		mult += meanY;

		Register result = x.add(new Register(mult));
		return result;
	}

}
