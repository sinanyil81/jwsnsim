package nodes;

import hardware.clock.Clock32;
import hardware.transceiver.Packet;
import sim.radio.MacLayer;
import sim.radio.Radio;

public abstract class Node {
	protected int NODE_ID;
	protected MacLayer MAC = null;
	protected Radio RADIO = null;
	protected Clock32 CLOCK = null;

	protected boolean running = false;

	protected Position position = null;

	public Node(int id) {
		this.NODE_ID = id;
	}

	public Node(int id, Position position) {
		this.NODE_ID = id;
		this.position = position;
	}

	public void setClock(Clock32 clock) {
		CLOCK = clock;
	}

	public Clock32 getClock() {
		return CLOCK;
	}

	public void setMAC(MacLayer mac) {
		MAC = mac;
	}

	public MacLayer getMAC() {
		return MAC;
	}

	public void setRadio(Radio radio) {
		RADIO = radio;
	}

	public Radio getRadio() {
		return RADIO;
	}

	public double getDistance(Node other) {
		return position.distanceTo(other.getPosition());
	}

	public double getDistanceSquare(Node other) {
		return position.squareDistanceTo(other.getPosition());
	}

	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position p){
		position.set(p);
	}

	public int getID() {
		return NODE_ID;
	}

	public void on() throws Exception {

		if (running)
			throw new Exception("Node started previously");

		if (CLOCK == null)
			throw new Exception("Clock object must be assigned");

		if (RADIO == null)
			throw new Exception("Radio object must be assigned");

		if (MAC == null)
			throw new Exception("MAC object must be assigned");

		running = true;
		CLOCK.start();
		RADIO.on();
	}

	public boolean isRunning() {
		return running;
	}

	public void sendMessage(Packet packet) {
		MAC.sendPacket(packet);
	}

	public abstract void receiveMessage(Packet packet);

	public String toString() {
		String s = Integer.toString(NODE_ID);

		return s;
	}
}
