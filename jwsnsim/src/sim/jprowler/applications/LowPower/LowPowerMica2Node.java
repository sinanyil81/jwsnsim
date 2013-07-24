/*
 * Copyright (c) 2002, Vanderbilt University
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE VANDERBILT UNIVERSITY BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE VANDERBILT
 * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE VANDERBILT UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE VANDERBILT UNIVERSITY HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Author: Gyorgy Balogh, Gabor Pap, Miklos Maroti
 * Date last modified: 02/09/04
 */
package sim.jprowler.applications.LowPower;

import sim.jprowler.Event;
import sim.jprowler.Node;
import sim.jprowler.Protocol;
import sim.jprowler.RadioModel;
import sim.jprowler.RadioPacket;
import sim.jprowler.Simulator;
import sim.jprowler.UInt32;
import sim.jprowler.applications.PISync.PIPayload;
import sim.jprowler.clock.Clock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

/**
 * This class represents a mote and all its properties important from the
 * simulation point of view. The MAC layer specific constant are all defined and
 * used here.
 * 
 * @author Gyorgy Balogh, Gabor Pap, Miklos Maroti
 */
public class LowPowerMica2Node extends Node implements TimerHandler {

	/** ----------------- LOW POWER Parameters -------------------- **/
	private final static int BEACON_RATE = 30000000;
	/** Communication Period **/
	private double alpha = 1.0;

	Timer sendTimer = null;
	Timer receiveTimer = null;

	public enum RadioState {
		OFF, IDLE, 
		WAKEUP_TO_TRANSMIT, PROCESSING_TO_TRANSMIT, TRANSMITTING, 
		WAKEUP_TO_RECEIVE, WAITING_TO_RECEIVE, RECEIVING
	}

	RadioState radioState = RadioState.IDLE;
	/** -------------------------------------------------------------- **/
	
	/** --------- Time Synchronization Parameters -------------------- **/
	private static final float MAX_PPM = 0.0001f;	
	private static final float BOUNDARY = 2.0f*MAX_PPM*(float)BEACON_RATE;
	float K_max = 0.000004f/BOUNDARY;
		
	int calculateSkew(RadioPacket packet) {
		UInt32 neighborClock = packet.getEventTime();
		UInt32 myClock = LogicalClock.getInstance().getValue(packet.getTimestamp());

		return neighborClock.subtract(myClock).toInteger();
	}

	private void synchronize(RadioPacket packet) {
		LogicalClock.getInstance().update(packet.getTimestamp());
		int skew = calculateSkew(packet);
		
		/*  initial offset compensation */ 
		if(Math.abs(skew) <= BOUNDARY){	
					
			float x = BOUNDARY - Math.abs(skew);					
			float K_i = x*K_max/BOUNDARY;
						
			LogicalClock.getInstance().rate += K_i*0.5*(float)skew;
		}			
				
		if(skew > 1000){
			UInt32 myClock = LogicalClock.getInstance().getValue(packet.getEventTime());
			LogicalClock.getInstance().setValue(myClock.add(skew),packet.getTimestamp());
		}
		else{
			UInt32 myClock = LogicalClock.getInstance().getValue(packet.getEventTime());
			LogicalClock.getInstance().setValue(myClock.add(skew/2),packet.getTimestamp());
		}			
	}
	/** -------------------------------------------------------------- **/

	/**
	 * In this simulation not messages but references to motes are passed. All
	 * this means is that the Mica2Node has to hold the information on the
	 * sender application which runs on this very mote.
	 */
	protected Protocol senderApplication = null;	

	/**
	 * This is the message being sent, on reception it is extracted and the
	 * message part is forwarded to the appropriate application, see
	 * {@link Protocol#receiveMessage}.
	 */
	protected boolean hasPacketToSend = false;
	protected RadioPacket packetToSend = new RadioPacket(null);
	
	protected Node senderNode = null;
	protected RadioPacket packetToReceive = null;

	/** State variable, true if the last received message got corrupted by noise */
	protected boolean corrupted = false;

	// //////////////////////////////
	// MAC layer specific constants
	// //////////////////////////////

	/**
	 * The constant amount of time spent to send time information after logical
	 * clock is calculated
	 */
	public static int processingTime = 100;
	public static int processingRandomTime = 25;

	/** The amount of time spent for waking up radio */
	public static int wakeUpTime = 180;

	/** For CC2420 250 kbps -> approximately 32 microseconds per byte.
	 * In TinyOS, default packet size is 11 byte header, 28 byte payload, 7 byte
	 * meta= 46 * 32 microsec = approximately 1.5 ms */
	public static int sendTransmissionTime = 1500;

	// //////////////////////////////
	// EVENTS
	// //////////////////////////////
	private RadioWakeUpEvent wakeUpEvent = new RadioWakeUpEvent();
	private ProcessingEvent processingEvent = new ProcessingEvent();
	private SleepEvent sleepEvent = new SleepEvent();
	private EndTransmissionEvent endTransmissionEvent = new EndTransmissionEvent();

	// //////////////////////////////
	// Noise and signal
	// //////////////////////////////
	/** Signal stregth of transmitting or parent node. */
	private double signalStrength = 0;

	/** Noise generated by other nodes. */
	private double noiseStrength = 0;

	/**
	 * The constant self noise level. See either the
	 * {@link LowPowerMica2Node#calcSNR} or the
	 * {@link LowPowerMica2Node#isChannelFree} function.
	 */
	public double noiseVariance = 0.025;

	/**
	 * The maximum noise level that is allowed on sending. This is actually a
	 * multiplicator of the {@link LowPowerMica2Node#noiseVariance}.
	 */
	public double maxAllowedNoiseOnSending = 5;

	/** The minimum signal to noise ratio required to spot a message in the air. */
	public double receivingStartSNR = 4.0;

	/**
	 * The maximum signal to noise ratio below which a message is marked
	 * corrupted.
	 */
	public double corruptionSNR = 2.0;

	/** The event signaled when the radio wake-up is ended **/
	class RadioWakeUpEvent extends Event {

		public void execute() {
			
			switch (radioState) {
			
			case WAKEUP_TO_TRANSMIT:
				setEventTime();
				processingEvent.register(generateProcessingTime());
				radioState = RadioState.PROCESSING_TO_TRANSMIT;
				break;
				
			case WAKEUP_TO_RECEIVE:
				sleepEvent.unregister();
				sleepEvent.register(generateGuardTime());
				radioState = RadioState.WAITING_TO_RECEIVE;
				break;

			default:
				break;
			}
		}
	}

	/**
	 * When CPU processing is ended, this event is signalled and transmission
	 * starts
	 */
	class ProcessingEvent extends Event {

		public void execute() {
			
			switch (radioState) {
			case PROCESSING_TO_TRANSMIT:
				radioState = RadioState.TRANSMITTING;				
				beginTransmission(1, LowPowerMica2Node.this);
				endTransmissionEvent.register(sendTransmissionTime);
				break;

			default:
				break;
			}

		}
	}

	/**
	 * Represents the end of a transmission.
	 */
	class EndTransmissionEvent extends Event {
		/**
		 * Removes the noise generated by the transmission and sets the state
		 * variables accordingly.
		 */
		public void execute() {
			endTransmission();
			hasPacketToSend = false;
			if(senderApplication!=null){
				senderApplication.sendMessageDone(true);
				senderApplication = null;				
			}
			radioState = RadioState.IDLE;
			sleepEvent.execute();			
		}
	}

	class SleepEvent extends Event {
		public void execute() {
			switch (radioState) {
			case IDLE:
			case WAITING_TO_RECEIVE:
				if(allowedToTurnOffRadio())
					radioState = RadioState.OFF;
				else
					radioState = RadioState.IDLE;
				break;

			default:
				break;
			}			
		}
	}

	protected boolean allowedToTurnOffRadio() {
		
		/* TODO */
		return false;
	}

	/**
	 * Parameterized constructor, it set both the {@link Simulator} in which
	 * this mote exists and the {@link RadioModel} which is used by this mote.
	 * 
	 * @param sim
	 *            the Simulator in which the mote exists
	 * @param radioModel
	 *            the RadioModel used on this mote
	 */
	public LowPowerMica2Node(Simulator sim, RadioModel radioModel, Clock clock) {
		super(sim, radioModel, clock);
		
		sendTimer = new Timer(getClock(), this);
		receiveTimer = new Timer(getClock(), this);
		
		sendTimer.startOneshot(1000);
	}

	/**
	 * Calls the {@link LowPowerMica2Node#addNoise} method. See also
	 * {@link Node#receptionBegin} for more information.
	 */
	protected void receptionBegin(double strength, Object stream) {
		addNoise(strength, stream);
	}

	/**
	 * Calls the {@link LowPowerMica2Node#removeNoise} method. See also
	 * {@link Node#receptionEnd} for more information.
	 */
	protected void receptionEnd(double strength, Object stream) {
		removeNoise(strength, stream);
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
	public boolean sendMessage(RadioPacket packet, Protocol app) {
		if (hasPacketToSend) {
			System.out.println("FALSE " + LowPowerMica2Node.this.id);
			return false;
		} else {
			hasPacketToSend = true;
			packetToSend.setPayload(packet.getPayload());
			senderApplication = app;
			return true;
		}
	}

	/**
	 * Generates a random processing time
	 */
	protected static int generateProcessingTime() {
		return processingTime
				+ (int) (Simulator.random.nextDouble() * processingRandomTime);
	}

	/**
	 * Tells if the transmitting media is free of transmissions based on the
	 * noise level.
	 * 
	 * @param noiseStrength
	 *            the level of noise right before transmission
	 * @return returns true if the channel is free
	 */
	protected boolean isChannelFree(double noiseStrength) {
		return noiseStrength < maxAllowedNoiseOnSending * noiseVariance;
	}

	/**
	 * Tells if the transmitting media is free of transmissions based on the
	 * noise level.
	 * 
	 * @param signal
	 *            the signal strength
	 * @param noise
	 *            the noise level
	 * @return returns true if the message is corrupted
	 */
	public boolean isMessageCorrupted(double signal, double noise) {
		return calcSNR(signal, noise) < corruptionSNR;
	}

	/**
	 * Inner function for calculating the signal noise ratio the following way: <br>
	 * signal / (noiseVariance + noise).
	 * 
	 * @param signal
	 *            the signal strength
	 * @param noise
	 *            the noise level
	 * @return returns the SNR
	 */
	protected double calcSNR(double signal, double noise) {
		return signal / (noiseVariance + noise);
	}

	/**
	 * Tells if the incomming message signal is corrupted by another signal.
	 * 
	 * @param signal
	 *            the signal strength of the incomming message
	 * @param noise
	 *            the noise level
	 * @return returns true if the message is corrupted
	 */
	public boolean isReceivable(double signal, double noise) {
		return calcSNR(signal, noise) > receivingStartSNR;
	}

	/**
	 * Adds the noice generated by other motes, and breaks up a transmission if
	 * the noise level is too high. Also checks if the noise is low enough to
	 * hear incomming messages or not.
	 * 
	 * @param level
	 *            the level of noise
	 * @param stream
	 *            a reference to the incomming message
	 */
	protected void addNoise(double level, Object stream) {
		
		switch (radioState) {
		
		case RECEIVING:
			noiseStrength += level;
			if (isMessageCorrupted(signalStrength, noiseStrength))
				corrupted = true;
			break;
			
		case WAITING_TO_RECEIVE:
		case IDLE:
			if (isReceivable(level, noiseStrength)){
				// start receiving
				radioState = RadioState.RECEIVING;

				// stop sleeping
				sleepEvent.unregister();

				senderNode = (Node) stream;				
				packetToReceive = ((LowPowerMica2Node) senderNode).packetToSend
						.clone();
				setReceptionTimestamp(packetToReceive);

				corrupted = false;
				signalStrength = level;
			}
			break;

		default:
			noiseStrength += level;
			break;
		}		
	}

	private void setReceptionTimestamp(RadioPacket packet) {
		UInt32 timestamp = getClock().getValue();
		packet.setTimestamp(timestamp);
	}

	/**
	 * Removes the noise, if a transmission is over, though if the source is the
	 * sender of the message being transmitted there is some post processing
	 * accordingly, the addressed application is notified about the incomming
	 * message.
	 * 
	 * @param stream
	 *            a reference to the incomming messagethe incomming message
	 * @param level
	 *            the level of noise
	 */
	protected void removeNoise(double level, Object stream) {
		switch (radioState) {
		
		case RECEIVING:
			if (senderNode == stream) {
				
				radioState = RadioState.IDLE;
				sleepEvent.execute();

				if (!corrupted) {
					synchronize(packetToReceive);
					this.getApplication().receiveMessage(packetToReceive);
				} else {
					System.out.println("Corrupted");
				}

				signalStrength = 0;
				senderNode = null;
				packetToReceive = null;
			}

			break;

		default:
			noiseStrength -= level;
			break;
		}
	}
	


	@Override
	public void fireEvent(Timer timer) {

		if (timer == sendTimer) {
			setNextTransmissionTime();

			switch (radioState) {

			case OFF:
				wakeUpEvent.register(wakeUpTime);
				radioState = RadioState.WAKEUP_TO_TRANSMIT;
				break;

			case IDLE:
				setEventTime();
				processingEvent.register(generateProcessingTime());
				radioState = RadioState.PROCESSING_TO_TRANSMIT;
				break;

			case WAITING_TO_RECEIVE:
				setEventTime();
				sleepEvent.unregister();
				processingEvent.register(generateProcessingTime());
				radioState = RadioState.PROCESSING_TO_TRANSMIT;
				break;

			case WAKEUP_TO_RECEIVE:
				radioState = RadioState.WAKEUP_TO_TRANSMIT;
				break;

			default:
				/** cant send packet since transmitting or receiving */
				hasPacketToSend = false;
				if(senderApplication!=null){
					senderApplication.sendMessageDone(false);
					senderApplication = null;
				}
				break;
			}		
		} else if (timer == receiveTimer) {
			switch (radioState) {
			
			case OFF:
				wakeUpEvent.register(wakeUpTime);
				radioState = RadioState.WAKEUP_TO_RECEIVE;
				break;
				
			case IDLE:
				sleepEvent.register(generateGuardTime());
				radioState = RadioState.WAITING_TO_RECEIVE;
				break;
				
			case WAKEUP_TO_RECEIVE:
				radioState = RadioState.WAKEUP_TO_RECEIVE;
				break;

			case WAITING_TO_RECEIVE:
				sleepEvent.unregister();
				sleepEvent.register(generateGuardTime());
				radioState = RadioState.WAITING_TO_RECEIVE;
				break;
				

			default:
				break;
			}
			setNextReceptionTime();
		}
	}

	private int generateGuardTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void setEventTime() {		
		UInt32 localTime = getClock().getValue();
		LogicalClock.getInstance().update(localTime);
		UInt32 globalTime = LogicalClock.getInstance().getValue(localTime);
		packetToSend.setEventTime(globalTime);
	}

	private void setNextTransmissionTime() {
		UInt32 localTime = getClock().getValue();
		LogicalClock.getInstance().update(localTime);
		UInt32 globalTime = LogicalClock.getInstance().getValue(localTime);
		
		int mod = globalTime.modulus(BEACON_RATE);
		int remainingTime = BEACON_RATE - mod + getId()*1000000;
		sendTimer.startOneshot(remainingTime);
	}

	private void setNextReceptionTime() {
		// TODO Auto-generated method stub

	}
}
