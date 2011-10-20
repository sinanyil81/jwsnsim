package sim.radio;

public abstract class MacLayer implements RadioListener{
	public abstract boolean sendPacket(RadioPacket packet);
	public abstract void receivePacket(RadioPacket packet);
}
