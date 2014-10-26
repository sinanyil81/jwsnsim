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

package hardware.transceiver;

import hardware.Interrupt;
import hardware.InterruptHandler;
import hardware.Register32;
import hardware.clock.Clock32;

public class Transceiver implements InterruptHandler {

	protected double MAX_RADIO_STRENGTH = 100;
	protected static int TRANSMISSION_TIME = 960;

	protected Packet packetToTransmit = null;
	protected Packet receivingPacket = null;

	protected boolean corrupted = false;

	protected boolean receiving = false;
	protected boolean transmitting = false;

	private Clock32 clock;
	private PacketListener listener;
	private Interrupt interrupt;
	
	private double receivingSignalStrength = 0.0;
	private double noiseStrength = 0.0f;

	Transceiver receivers[];

	public Transceiver(Clock32 clock, PacketListener listener) {
		this.listener = listener;
		this.clock = clock;
		this.interrupt = new Interrupt(this);
	}

	public void transmit(Packet packet, Transceiver receivers[]) {

		transmitting = true;
		packetToTransmit = packet;
		setTransmissionTimestamp();

		this.receivers = receivers;
		int i = receivers.length;
		while (--i >= 0) {
			receivers[i].receptionBegin(packet);
		}

		interrupt.register(TRANSMISSION_TIME);
	}

	private void setTransmissionTimestamp() {
		Register32 age = clock.getValue();
		age = age.subtract(packetToTransmit.getEventTime());
		packetToTransmit.setEventTime(age);
	}

	public void endTransmission() {
		int i = receivers.length;
		while (--i >= 0)
			receivers[i].receptionEnd(packetToTransmit);

		packetToTransmit = null;
		receivers = null;
		transmitting = false;
	}

	public void receptionBegin(Packet packet,double signalStrength) {

		if (receiving) {
			noiseStrength += signalStrength;
			if(Signal.isCorrupted(signalStrength, noiseStrength))
				corrupted = true;
		} else {
			if (!transmitting && Signal.isReceivable(signalStrength, noiseStrength)) {
				// start receiving
				receivingPacket = new Packet((Packet) packet);
				receivingSignalStrength = signalStrength;
				setReceptionTimestamp();
				receiving = true;
				corrupted = false;
			} else {
				noiseStrength += signalStrength;
			}
		}
	}

	private void setReceptionTimestamp() {
		Register32 timestamp = clock.getValue();
		receivingPacket.setTimestamp(timestamp);
		timestamp = timestamp.subtract(receivingPacket.getEventTime());
		receivingPacket.setEventTime(timestamp);
	}

	public void receptionEnd(Packet packet,double signalStrength) {

		if (receivingPacket != null && receivingPacket.equals(packet)) {
			receiving = false;

			if (!corrupted) {
				listener.receivePacket(receivingPacket);
			} else
				System.out.println("Corruption!");

			receivingPacket = null;
			receivingSignalStrength = 0.0;
			
		}
		else{
			noiseStrength -= signalStrength;
		}
	}

	@Override
	public void signal(Interrupt interrupt) {
		endTransmission();
	}
	
	public double getMaxSignalStrength(){
		return MAX_RADIO_STRENGTH;
	}
	
	public void setMaxSignalStrength(double maxStrength){
		this.MAX_RADIO_STRENGTH = maxStrength;
	}

	public boolean CCA(){
		return Signal.isChannelFree(noiseStrength);
	}
}
