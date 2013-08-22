package sim.jprowler.radio;

import sim.jprowler.Event;

public class RadioProcessor {

	class WakeUpEvent extends Event {
	
		public void execute() {
			radio.wakeUpFinished();
		}
	}
	
	class ProcessingEvent extends Event {
	
		public void execute() {
			radio.processingFinished();
		}
	}

	class EndTransmissionEvent extends Event {
		public void execute() {
			radio.transmissionFinished();
		}
	}
	
	/** For CC2420 250 kbps -> approximately 32 microseconds per byte. */
	/** In TinyOS, default packet size is 11  byte header, 28 byte payload, 7 byte meta */
	/** 46 * 32 microsec = approximately 1.5 ms */
	public static int transmissionTime = 1500;
	public static final int wakeUpTime = 180; 
	public static final int processingTime = 180;
	
	WakeUpEvent wakeUpEvent = new WakeUpEvent();
	ProcessingEvent processingEvent = new ProcessingEvent();
	EndTransmissionEvent endTransmissionEvent = new EndTransmissionEvent();
	
	private Radio radio;

	public RadioProcessor(Radio radio){
		this.radio = radio;
	}
	
	public void wakeUp(){
		wakeUpEvent.register(wakeUpTime);
	}
	
	public void process(){
		processingEvent.register(processingTime);
	}
	
	public void transmit(){
		endTransmissionEvent.register(transmissionTime);
	}
}
