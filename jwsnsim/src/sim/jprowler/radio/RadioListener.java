package sim.jprowler.radio;


public interface RadioListener {
	
	public void startedReceiving(RadioPacket packet);
	public void stoppedReceiving(RadioPacket packet);
	
	public void processing(RadioPacket packet);
	public void startedTransmitting(RadioPacket packet);
	public void stoppedTransmitting(RadioPacket packet);
	
	public void packetLost(RadioPacket packet);
	
	public void radioWokeUp();
	public void radioOff();

}
