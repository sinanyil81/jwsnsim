package sim.jprowler.mac;

import sim.jprowler.Event;
import sim.jprowler.Node;
import sim.jprowler.radio.RadioPacket;
import sim.jprowler.radio.RadioListener;

public class NonCSMAMac extends MacLayer implements RadioListener {

	RadioPacket sentPacket = null;
	boolean sleep = false;
	int onDuration = 0;
	
	class OnTimer extends Event {
		
		public void execute() {
			node.getRadio().sleep();
			onDuration = 0;
		}
	}
	
	OnTimer onTimer = new OnTimer();

	public NonCSMAMac(Node node) {
		super(node);
	}

	public boolean sendPacket(RadioPacket packet, boolean sleep) {
		
		boolean result = false;
		
		switch (node.getRadio().getState()) {

		case IDLE:
			onTimer.unregister();
			sentPacket = packet;
			this.sleep = sleep;
			node.getRadio().transmit(sentPacket);
			
			result = true;
			break;

		case WAKEUP:
			sentPacket = packet;
			this.sleep = sleep;
			result = true;
			break;

		case OFF:
			node.getRadio().wakeUp();
			sentPacket = packet;
			this.sleep = sleep;
			result = true;
			break;

		default:
			break;

		}
		
		return result;
	}

	public boolean wakeUp(int numTicks) {
		
		boolean result = false;
		
		switch (node.getRadio().getState()) {

		case IDLE:
			onDuration = numTicks;
			onTimer.unregister();
			if(onDuration>0)
				onTimer.register(onDuration);
			result = true;
			break;

		case WAKEUP:
			onDuration = numTicks;
			result = true;
			break;

		case OFF:
			node.getRadio().wakeUp();
			onDuration = numTicks;
			result = true;
			break;

		default:
			break;

		}

		return result;
	}

	@Override
	public void startedReceiving(RadioPacket packet) {
		onTimer.unregister();
	}

	@Override
	public void stoppedReceiving(RadioPacket packet) {
		if (packet != null){
			packetReceipt(packet);
		}
		
		if(onDuration>0){
			node.getRadio().sleep();
			onDuration = 0;
		}		
	}

	@Override
	public void processing(RadioPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startedTransmitting(RadioPacket packet) {

	}

	@Override
	public void stoppedTransmitting(RadioPacket packet) {
		if (sleep) {
			node.getRadio().sleep();
		}

		sentPacket = null;
		sleep = false;
	}

	@Override
	public void packetLost(RadioPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void radioWokeUp() {
		if (sentPacket != null) {
			onDuration = 0;
			node.getRadio().transmit(sentPacket);
		}
		else if(onDuration > 0){
			onTimer.unregister();
			onTimer.register(onDuration);
		}
	}

	@Override
	public void radioOff() {
		// TODO Auto-generated method stub

	}
}
