package application.regression;

import sim.type.UInt32;

public class RegressionEntry {
	public UInt32 x = new UInt32();
	public int y;
	public boolean free = true;

	public RegressionEntry(RegressionEntry entry) {
		this.x = new UInt32(entry.x);
		this.y = entry.y;
		this.free = entry.free;
	}

	public RegressionEntry() {

	}
}