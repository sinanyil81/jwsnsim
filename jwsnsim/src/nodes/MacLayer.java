package nodes;

import hardware.transceiver.PacketListener;
import hardware.transceiver.Packet;

/**
 * 
 * @author K. Sinan YILDIRIM
 *
 *Do not call sendPacket within receivePacket or vice versa.
 */
public abstract class MacLayer implements PacketListener{
	public abstract boolean sendPacket(Packet packet);
	public abstract void receivePacket(Packet packet);
}
