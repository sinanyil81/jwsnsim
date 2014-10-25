package sim.node;

import sim.clock.Counter32;
import sim.radio.MacLayer;
import sim.radio.Radio;
import sim.radio.RadioPacket;
import sim.simulator.Simulator;

public abstract class Node {
	protected int NODE_ID;
	protected MacLayer MAC = null;
	protected Radio RADIO = null;
	protected Counter32 CLOCK = null;

	protected boolean running = false;

	protected Position position = null;

	public Node(int id) {
		this.NODE_ID = id;
	}

	public Node(int id, Position position) {
		this.NODE_ID = id;
		this.position = position;
	}

	public void setClock(Counter32 clock) {
		CLOCK = clock;
	}

	public Counter32 getClock() {
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

	public void sendMessage(RadioPacket packet) {
		MAC.sendPacket(packet);
	}

	public abstract void receiveMessage(RadioPacket packet);

	public String toString() {
		String s = Integer.toString(NODE_ID);

		return s;
	}
}
