package application.regression;

import hardware.Register32;

public class MinimumVarianceSlopeRegression {

	private float slope = 0.0f;
	private Register32 meanX = new Register32();
	private int meanY = 0;

	public void calculate(RegressionEntry table[], int tableEntries) {
		float newSlope = slope;

		Register32 newMeanX;
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
				Register32 diff = table[i].x.subtract(newMeanX);
				xSum += diff.toInteger() / tableEntries;
				meanXRest += diff.toInteger() % tableEntries;

				ySum += (table[i].y - newMeanY) / tableEntries;
				meanYRest += (table[i].y - newMeanY) % tableEntries;
			}

		xSum = (new Register32(xSum).add(new Register32(meanXRest / tableEntries)))
				.toLong();
		newMeanX = newMeanX.add(new Register32(xSum));

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
		meanX = new Register32(newMeanX);
	}

	public float getSlope() {
		return slope;
	}

	public void setSlope(float slope) {
		this.slope = slope;
	}

	public Register32 getMeanX() {
		return meanX;
	}

	public void setMeanX(Register32 meanX) {
		this.meanX = meanX;
	}

	public int getMeanY() {
		return meanY;
	}

	public void setMeanY(int meanY) {
		this.meanY = meanY;
	}

	public Register32 calculateY(Register32 x) {
		Register32 diff = new Register32(x);
		diff = diff.subtract(meanX);

		int mult = (int) (slope * (float) (diff.toInteger()));
		mult += meanY;

		Register32 result = x.add(new Register32(mult));
		return result;
	}
	
	public Register32 calculateY(Register32 x,float slope) {
		Register32 diff = new Register32(x);
		diff = diff.subtract(meanX);

		int mult = (int) (slope * (float) (diff.toInteger()));
		mult += meanY;

		Register32 result = x.add(new Register32(mult));
		return result;
	}

}
