package nodes;

import hardware.transceiver.TransceiverListener;
import hardware.transceiver.Packet;

/**
 * 
 * @author K. Sinan YILDIRIM
 *
 *Do not call sendPacket within receivePacket or vice versa.
 */
public abstract class MacLayer implements TransceiverListener{
	public abstract boolean sendPacket(Packet packet);
	public abstract void receivePacket(Packet packet);
}
