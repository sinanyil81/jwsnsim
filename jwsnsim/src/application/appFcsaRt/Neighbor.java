package application.appFcsaRt;

import hardware.Register32;
import application.regression.RegressionEntry;

public class Neighbor {
	
	private static final int MAX_ENTRIES = 8;
	
	public int id;
	public float rate;
	public float rootRate;
	public float relativeRate;
	
	public Register32 rootClock = new Register32();
	


	public Register32 timestamp = new Register32();
	public boolean free = true;

	public RegressionEntry table[] = new RegressionEntry[MAX_ENTRIES];
    int tableEnd = -1;
	public int tableEntries = 0;

	public Neighbor() {
		for (int i = 0; i < table.length; i++) {
			table[i] = new RegressionEntry();
		}

		tableEntries = 0;
		tableEnd = -1;
	}
	
	public float getRate() {
		return rate;
	}

	public float getRelativeRate() {
		return relativeRate;
	}

	public Register32 getRootClock() {
		return rootClock;
	}

	public Register32 getTimestamp() {
		return timestamp;
	}

	public void clearTable() {
		int i;

		for (i = 0; i < MAX_ENTRIES; ++i)
			table[i].free = true;
		
		tableEntries = 0;
		tableEnd = -1;

	}

	public void addNewEntry(Register32 neighborTime, Register32 localTime) {
		int i;

		if (tableEntries == MAX_ENTRIES) {
			for (i = 0; i < MAX_ENTRIES - 1; i++) {
				table[i] = new RegressionEntry(table[i + 1]);
			}
		} else {
			tableEnd++;
			tableEntries++;
		}

		table[tableEnd].free = false;
		table[tableEnd].x = new Register32(localTime);
		table[tableEnd].y = neighborTime.toInteger() - localTime.toInteger();
	}
	
	public Register32 getClock(Register32 currentTime){
		int timePassed = currentTime.subtract(timestamp).toInteger();
		float r = relativeRate + rate + relativeRate * rate;
		r -= rootRate;
		 r /= (1.0 + rootRate);
		
		
//		timePassed += (int) (((float)timePassed)*relativeRate);
//		int progress = timePassed + (int) (((float)timePassed)*rate);
//		progress = (int) (((float)progress)/(1.0+rootRate));
		
		int  progress = timePassed +  (int) (r * (float)timePassed);
		
		return rootClock.add(new Register32(progress));
	}
}
