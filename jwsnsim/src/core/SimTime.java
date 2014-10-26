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

package core;

public class SimTime {
	
	private long timeHigh = 0;
	private double timeLow  = 0.0;
	
	public SimTime(){
		timeHigh = 0;
		timeLow = 0.0;
	}
	
	public SimTime(SimTime s){
		timeHigh = s.timeHigh;
		timeLow = s.timeLow;
	}
	
	public SimTime(double value){
		timeHigh = (long)value;
		timeLow = value - (long)value;
	}
	
	public SimTime(long high,double low){
		timeHigh = high;
		timeLow = low;
	}
	
	public long getTimeHigh() {
		return timeHigh;
	}
	
	public void setTimeHigh(long timeHigh) {
		this.timeHigh = timeHigh;
	}
	
	public double getTimeLow() {
		return timeLow;
	}
	
	public void setTimeLow(double timeLow) {
		this.timeLow = timeLow;
	}
	
	public SimTime add(SimTime time){
		double lowSum = time.getTimeLow() + timeLow;
		long highSum = timeHigh +(long)lowSum + time.getTimeHigh();
		lowSum -= (double)((long)lowSum);
		
		if((lowSum < 0) && (highSum > 0)){
			highSum--;
			lowSum += 1.0;
		}
		else if((lowSum > 0) && (highSum < 0)){
			highSum++;
			lowSum -= 1.0;
		}
		
		SimTime ret = new SimTime(highSum,lowSum);
		
		return ret;
	}
	
	public SimTime sub(SimTime time){
		
		if(timeLow < time.getTimeLow()){
			timeHigh--;
			timeLow += 1.0;
		}
		
		double lowDiff = timeLow - time.getTimeLow();
		long highDiff = timeHigh - time.getTimeHigh();
		
		if(highDiff <0){
			highDiff++;
			lowDiff -= 1.0;
		}
		
		SimTime ret = new SimTime(highDiff,lowDiff);
		
		return ret;
	}
		
	public int compareTo(SimTime time){
		
		if(this.timeHigh > time.getTimeHigh()){
			return 1;
		}
		else if(this.timeHigh == time.getTimeHigh()){
			if(this.timeLow > time.getTimeLow())
				return 1;
			else if(this.timeLow == time.getTimeLow())
				return 0;
			else
				return -1;
		}
		else
			return -1;
	}
	
	public double toDouble(){
		
		return (double)timeHigh + timeLow; 
	}
	
	public String toString(){
		return String.valueOf(timeHigh);
	}
}