package application.regression;

import hardware.Register;

public class RegressionEntry {
	public Register x = new Register();
	public int y;
	public boolean free = true;

	public RegressionEntry(RegressionEntry entry) {
		this.x = new Register(entry.x);
		this.y = entry.y;
		this.free = entry.free;
	}

	public RegressionEntry() {

	}
}