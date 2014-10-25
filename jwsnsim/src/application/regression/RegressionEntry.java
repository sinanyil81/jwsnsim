package application.regression;

import hardware.Register32;

public class RegressionEntry {
	public Register32 x = new Register32();
	public int y;
	public boolean free = true;

	public RegressionEntry(RegressionEntry entry) {
		this.x = new Register32(entry.x);
		this.y = entry.y;
		this.free = entry.free;
	}

	public RegressionEntry() {

	}
}