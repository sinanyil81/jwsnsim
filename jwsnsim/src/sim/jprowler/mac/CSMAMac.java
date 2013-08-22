package sim.jprowler.mac;

import sim.jprowler.Event;
import sim.jprowler.Node;
import sim.jprowler.Simulator;
import sim.jprowler.radio.Noise;
import sim.jprowler.radio.RadioPacket;
import sim.jprowler.radio.Radio.RadioStates;
import sim.jprowler.radio.RadioListener;

public class CSMAMac extends MacLayer implements RadioListener{
		
	RadioPacket sentPacket = null;

	/** The constant component of the backoff time. */
	public static int sendMinBackOffTime = 100 * 25; // 2.5 ms

	/** The variable component of the backoff time. */
	public static int sendRandomBackOffTime = 10 * 25; // 0.25 ms

	/**
	 * Every mote has to test the radio traffic before transmitting a message,
	 * if there is to much traffic this event remains a test and the mote
	 * repeats it later, if there is no significant traffic this event initiates
	 * message transmission and posts a {@link Mica2Node#EndTransmissionEvent}
	 * event.
	 */
	private TestChannelEvent testChannelEvent = new TestChannelEvent();

	/**
	 * Inner class TestChannelEvent. Represents a test event, this happens when
	 * the mote listens for radio traffic to decide about transmission.
	 */
	class TestChannelEvent extends Event {

		/**
		 * If the radio channel is clear it begins the transmission process,
		 * otherwise generates a backoff and restarts testing later. It also
		 * adds noise to the radio channel if the channel is free.
		 */
		public void execute() {
			if (Noise.isChannelFree(node.getRadio().getNoiseStrength())) {
				node.getRadio().transmit(sentPacket);
				sentPacket = null;
			} else {
				this.register(generateBackOffTime());
			}
		}
	}
	
	public CSMAMac(Node node){
		super(node);
	}

	public void sendPacket(RadioPacket packet) {
		
		if(node.getRadio().getState() == RadioStates.IDLE){
			
			if(sentPacket == null){
				sentPacket = packet.clone();
				if (Noise.isChannelFree(node.getRadio().getNoiseStrength())) {
					node.getRadio().transmit(sentPacket);
					sentPacket = null;
				} else {
					testChannelEvent.register(generateBackOffTime());
				}				
			}
			else{
				System.out.println("Cant send "+node.getId());
			}
		}
	}

	/**
	 * Generates a backoff time, adding a random variable time to a constant
	 * minimum.
	 * 
	 * @return returns the backoff time in milliseconds
	 */
	protected static int generateBackOffTime() {
		return sendMinBackOffTime
				+ (int) (Simulator.random.nextDouble() * sendRandomBackOffTime);
	}

	@Override
	public void startedReceiving(RadioPacket packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stoppedReceiving(RadioPacket packet) {
		if(packet != null)
			packetReceipt(packet);
		else
			System.out.println("Packet null " + node.getId());
	}

	@Override
	public void processing(RadioPacket packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startedTransmitting(RadioPacket packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stoppedTransmitting(RadioPacket packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void packetLost(RadioPacket packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void radioWokeUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void radioOff() {
		// TODO Auto-generated method stub
		
	}
}
