package sim.radio;

import sim.node.Node;

public abstract class Radio {

	/** The current sending intensity of this radio. The intensity lies between 
	 * 0.0 and 1.0. */
	public double intensity = 1.0;
	
	public double getIntensity(){
		return intensity;
	}

	public abstract void on();

	public abstract void beginTransmission(RadioPacket stream);

	public abstract void endTransmission();

	public abstract void receptionBegin(RadioPacket stream);

	public abstract void receptionEnd(RadioPacket stream);

	protected abstract boolean isChannelFree();
	
	public abstract Node[] getNeighbors();
	
	public abstract void updateNeighborhood();
}
