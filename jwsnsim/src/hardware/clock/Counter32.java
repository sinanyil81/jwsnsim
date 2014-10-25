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
 * Simulates 32 bit hardware counter register 
 * 
 * @author Kasım Sinan YILDIRIM (sinanyil81@gmail.com)
 *
 */

package hardware.clock;

import hardware.Register32;

public class Counter32 {

	protected static double MAX_VALUE = 4294967295.0;

	/** Constant drift of the hardware clock */
	protected double drift = 0.0;

	/** Value of the clock register */
	protected double clock = 0.0;

	/** is started? */
	protected boolean started = false;

	public void start() {
		started = true;
	}

	public void progress(double amount) {

		if (!started)
			return;

		/* Progress clock by considering the constant drift. */
		clock += amount + amount * drift;

		/* Check if wraparound has occured. */
		if (clock > MAX_VALUE) {
			clock -= MAX_VALUE;
		}
	}

	public Register32 getValue() {

		return new Register32((long) clock);
	}

	public void setValue(Register32 value) {
		this.clock = value.toDouble();
	}

	public double getDrift() {
		return drift;
	}

	public void setDrift(double drift) {
		this.drift = drift;
	}
}
