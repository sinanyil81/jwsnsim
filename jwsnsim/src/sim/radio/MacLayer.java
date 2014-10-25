package sim.radio;

import hardware.transceiver.RadioListener;
import hardware.transceiver.RadioPacket;

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
