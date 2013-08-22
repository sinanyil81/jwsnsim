package sim.jprowler.mac;

import sim.jprowler.radio.RadioPacket;

public interface MacListener {
	public void receiveMessage(RadioPacket packet);	
	public void on();
	public void off();
	public void packetLost();
}
