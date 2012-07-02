package sim.radio;
/**
 * 
 * @author K. Sinan YILDIRIM
 *
 *Do not call sendPacket within receivePacket or vice versa.
 */
public abstract class MacLayer implements RadioListener{
	public abstract boolean sendPacket(RadioPacket packet);
	public abstract void receivePacket(RadioPacket packet);
}
