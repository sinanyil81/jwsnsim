package sim.radio;



import core.SimulationEvent;
import core.SimulationEventObserver;
import hardware.transceiver.Packet;
import sim.node.Node;
import sim.statistics.Distribution;

public class MicaMac extends MacLayer implements SimulationEventObserver {

	/**
	 * This is the message being sent, on reception it is extracted and the
	 * message part is forwarded to the appropriate application, see
	 * {@link SynchronizationSimulation#receiveMessage}.
	 */
	protected Packet sendingPacket = null;

	/**
	 * State variable, true if radio failed to transmit a message do to high
	 * radio traffic, this means it has to retry it later, which is done using
	 * the {@link Mica2Node#generateBackOffTime} function.
	 */

	protected boolean sendingPostponed = false;

	protected boolean sending = false;
	protected boolean receiving = false;

	// //////////////////////////////
	// MAC layer specific constants
	// //////////////////////////////

	/** The constant component of the time spent waiting before a transmission. */
	public static int sendMinWaitingTime = 200;

	/** The variable component of the time spent waiting before a transmission. */
	public static int sendRandomWaitingTime = 5000;

	/** The constant component of the backoff time. */
	public static int sendMinBackOffTime = 100;

	/** The variable component of the backoff time. */
	public static int sendRandomBackOffTime = 30;

	// //////////////////////////////
	// EVENTS
	// //////////////////////////////

	/**
	 * Every mote has to test the radio traffic before transmitting a message,
	 * if there is to much traffic this event remains a test and the mote
	 * repeats it later, if there is no significant traffic this event initiates
	 * message transmission and posts a {@link Mica2Node#EndTransmissionEvent}
	 * event.
	 */
	private SimulationEvent testChannelEvent = new SimulationEvent(this);

	private Node node;

	public MicaMac(Node node) {
		this.node = node;
	}

	@Override
	public void signal(SimulationEvent event) {
		if (event == testChannelEvent) {
			if (node.getRadio().isChannelFree()) {
				node.getRadio().beginTransmission(sendingPacket);
			} else {
				testChannelEvent.register(generateBackOffTime());
			}
		} else {
			sendingPostponed = true;
		}
	}

	/**
	 * Sends out a radio message. If the node is in receiving mode the sending
	 * is postponed until the receive is finished. This method behaves exactly
	 * like the SendMsg.send command in TinyOS.
	 * 
	 * @param packet
	 *            the message to be sent
	 * @param app
	 *            the application sending the message
	 * @return If the node is in sending state it returns false otherwise true.
	 */
	public boolean sendPacket(Packet packet) {
		if (sending)
			return false;
		else {
			sending = true;
			this.sendingPacket = packet;

			if (receiving) {
				sendingPostponed = true;
			} else {
				sendingPostponed = false;
				testChannelEvent.register(generateWaitingTime());
			}
			return true;
		}
	}

	@Override
	public void receivePacket(Packet packet) {
		node.receiveMessage(packet);
	}

	/**
	 * Generates a waiting time, adding a random variable time to a constant
	 * minimum.
	 * 
	 * @return returns the waiting time in milliseconds
	 */
	public static int generateWaitingTime() {
		return sendMinWaitingTime
				+ (int) (Distribution.getRandom().nextDouble() * sendRandomWaitingTime);
	}

	/**
	 * Generates a backoff time, adding a random variable time to a constant
	 * minimum.
	 * 
	 * @return returns the backoff time in milliseconds
	 */
	protected static int generateBackOffTime() {
		return sendMinBackOffTime
				+ (int) (Distribution.getRandom().nextDouble() * sendRandomBackOffTime);
	}

	@Override
	public void transmissionBegin() {

	}

	@Override
	public void transmissionEnd() {
		sending = false;
	}

	@Override
	public void receptionBegin() {
		receiving = true;
	}

	@Override
	public void receptionEnd() {
		receiving = false;

		if (sendingPostponed) {
			sendingPostponed = false;
			testChannelEvent.register(generateWaitingTime());
		}
	}
}
