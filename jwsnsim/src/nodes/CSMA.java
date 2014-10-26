/*
 * Copyright (c) 2014, Ege University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the copyright holder nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * @author Kasım Sinan YILDIRIM (sinanyil81@gmail.com)
 * 
 */
package nodes;

import core.SimulationEvent;
import core.SimulationEventObserver;
import hardware.transceiver.Packet;
import sim.statistics.Distribution;

public class CSMA extends MacLayer implements SimulationEventObserver {

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

	public CSMA(Node node) {
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
