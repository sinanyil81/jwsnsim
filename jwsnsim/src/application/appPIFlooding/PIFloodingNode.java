package application.appPIFlooding;

import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.radio.RadioPacket;
import sim.radio.SimpleRadio;
import sim.simulator.Simulator;
import sim.type.UInt32;

public class PIFloodingNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 500000000;  
	private static final float MAX_PPM = 0.0001f;

	LogicalClock logicalClock = new LogicalClock();
	Timer timer0;

	RadioPacket processedMsg = null;
	PIFloodingMessage outgoingMsg = new PIFloodingMessage();
    
	public PIFloodingNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);
		
		/* to start clock with a random value */
		if(this.NODE_ID == 1){
			CLOCK.setValue(new UInt32(0));
//			CLOCK.setDrift(0.0001f);
			CLOCK.setDrift(0.000050f);
		}
		/* to start clock with a random value */
		else if(this.NODE_ID == 2){
			CLOCK.setValue(new UInt32(0));
		}
		else if(this.NODE_ID == 20){
			CLOCK.setValue(new UInt32(Integer.MAX_VALUE));
//			CLOCK.setDrift(0.0002f);
		}
		else
			CLOCK.setValue(new UInt32(Math.abs(Simulator.random.nextInt())));

		timer0 = new Timer(CLOCK, this);		
	
		outgoingMsg.sequence = 0;
		outgoingMsg.rootid = NODE_ID;
		outgoingMsg.nodeid = NODE_ID;
	}
	
	int calculateSkew(RadioPacket packet) {
		PIFloodingMessage msg = (PIFloodingMessage) packet.getPayload();

		UInt32 neighborClock = msg.clock;
		UInt32 myClock = logicalClock.getValue(packet.getEventTime());

		return neighborClock.subtract(myClock).toInteger();
	}
	
	private void algorithm1(RadioPacket packet) {
		UInt32 updateTime = packet.getEventTime();
		logicalClock.update(updateTime);
		PIFloodingMessage msg = (PIFloodingMessage)packet.getPayload();

		if( msg.rootid < outgoingMsg.rootid) {
			outgoingMsg.rootid = msg.rootid;
			outgoingMsg.sequence = msg.sequence;
		} else if (outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;
		}
		else {
			return;
		}
	
		int skew = calculateSkew(packet);
		float K_p = 0.5f;
		float K_i = 0.0f;
		
		float boundary = 2.0f*MAX_PPM*(float)BEACON_RATE; // + maybe a tolerance;
		
		/*  initial offset compensation */ 
		if(Math.abs(skew) > boundary){
			logicalClock.setValue(logicalClock.getValue(updateTime).add(skew),updateTime);
			return;
		}

		K_i = (boundary - Math.abs(skew))*0.00000001f/boundary;  
		logicalClock.setValue(logicalClock.getValue(updateTime).add((int)((float)skew*K_p)),updateTime);			
		logicalClock.rate += K_i*0.5f*(float)skew;
	}

	float K_i = 0.0f;	
	float K_max_20 = 0.00002f/(2.0f*MAX_PPM*(float)BEACON_RATE);	

	float K_min = 0.000004f/(2.0f*MAX_PPM*(float)BEACON_RATE);
	
	float increment = 0.0f;
	
//	private static final float TURNING_POINT1 = 2.0f*MAX_PPM*(float)BEACON_RATE;
	private static final float BOUNDARY = 2.0f*MAX_PPM*(float)BEACON_RATE;
	float K_max_10 = 0.000004f/BOUNDARY;
//	private static final float TURNING_POINT1 = BOUNDARY*0.01f;
	private static final float TURNING_POINT1 = 0;
	
	int counter = 0;
	
	private void algorithm2(RadioPacket packet) {
		UInt32 updateTime = packet.getEventTime();
		logicalClock.update(updateTime);
		PIFloodingMessage msg = (PIFloodingMessage)packet.getPayload();

		if( msg.rootid < outgoingMsg.rootid) {
			outgoingMsg.rootid = msg.rootid;
			outgoingMsg.sequence = msg.sequence;
			counter = 2;
		} else if (outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;
		}
		else {
			return;
		}
	
		int skew = calculateSkew(packet);	
								
		/*  initial offset compensation */ 
		if(Math.abs(skew) <= BOUNDARY){	
			
//			if(this.NODE_ID == 20)
//				System.out.println(skew);
						
//			if(Math.abs(skew) < TURNING_POINT1){
////				float x = boundary - Math.abs(skew);					
////				K_i = x*K_max_10/(boundary-TURNING_POINT1);				
////				
////				K_i += (TURNING_POINT1-Math.abs(skew))/TURNING_POINT1*(K_min-K_i);
//				K_i = Math.abs(skew)*(K_max_10-K_min)/TURNING_POINT1 + K_min;
//			}
//			else{
//				float x = BOUNDARY - Math.abs(skew);					
//				K_i = x*K_max_10/(BOUNDARY-TURNING_POINT1);
////				K_i = x*K_max_10/boundary;
//			}
			
			if(Math.abs(skew) < TURNING_POINT1){
				K_i = Math.abs(skew)*K_max_10/TURNING_POINT1;
			}
			else{
				float x = BOUNDARY - Math.abs(skew);					
				K_i = x*K_max_10/(BOUNDARY-TURNING_POINT1);
			}
			
//			if(Math.abs(skew) < BOUNDARY/2){
//				float x = BOUNDARY - Math.abs(skew);
//				K_i = (K_max_10/BOUNDARY)*(x/Math.abs(skew))*Math.abs(skew);
//			}
//			else{
//				float x = BOUNDARY - Math.abs(skew);					
//				K_i = x*K_max_10/BOUNDARY;
//			}
			
//			if(Math.abs(skew) < BOUNDARY/10){
////				K_i = 2.0f*Math.abs(skew)*K_max_10/BOUNDARY;
//				float x = BOUNDARY - Math.abs(skew);					
//				K_i = x*K_max_10/BOUNDARY;
//				
//			}	
//			else{								
//				K_i = (2.0f*K_max_10/BOUNDARY)*(((Math.abs(skew)-BOUNDARY)*(Math.abs(skew)-BOUNDARY))/Math.abs(skew));
//			}
			
			logicalClock.rate += K_i*(float)skew;
			increment = K_i*(float)skew;
//			
//			if(this.NODE_ID == 2)
//				System.out.println(increment);
		}	
		else{
//			System.out.println(Simulator.getInstance().getSecond() + " " + this.NODE_ID + " " + skew + " " + BOUNDARY);
		}
				
//		logicalClock.setValue(logicalClock.getValue(updateTime).add(skew),updateTime);
		logicalClock.setValue(((PIFloodingMessage) packet.getPayload()).clock,updateTime);
		
		if(this.NODE_ID ==100 )
			System.out.println(Simulator.getInstance().getSecond() + " " + logicalClock.rate + " " + skew);
		
//		if (counter == 1){		
//			timer0.stop();
//			timer0.startPeriodic(BEACON_RATE+((Simulator.random.nextInt() % 100) + 1)*10000);
//			counter--;
//		}
//		else if(counter > 0)
//		{
			timer0.startOneshot(5000);
			counter--;
//		}
	}
	
//	float K_max = 0.00001f/(2.0f*MAX_PPM*(float)BEACON_RATE);
//	public AvtSimple alpha = new AvtSimple(0.0f, K_max, 0.0f, 0.00000000001f, 0.00001f); /* 0.00000000001f*/
//	int previousSkew = 0;
//	
//	private void algorithm3(RadioPacket packet) {
//		UInt32 updateTime = packet.getEventTime();
//		logicalClock.update(updateTime);
//		PIFloodingMessage msg = (PIFloodingMessage)packet.getPayload();
//
//		if( msg.rootid < outgoingMsg.rootid) {
//			outgoingMsg.rootid = msg.rootid;
//			outgoingMsg.sequence = msg.sequence;
//		} else if (outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0) {
//			outgoingMsg.sequence = msg.sequence;
//		}
//		else {
//			return;
//		}
//	
//		int skew = calculateSkew(packet);	
//				
//		
//		float boundary = 2.0f*MAX_PPM*(float)BEACON_RATE; // + maybe a tolerance;
//		
//		/*  initial offset compensation */ 
//		if(Math.abs(skew) <= boundary){			
//			
//			if ((skew > 0 && previousSkew > 0) || (skew < 0 && previousSkew < 0)){
//				alpha.adjustValue(AvtSimple.FEEDBACK_GREATER);
//			}
//			else if ((skew > 0 && previousSkew <= 0) || (skew < 0 && previousSkew >= 0)) {
////				alpha.adjustValue(AvtSimple.FEEDBACK_LOWER);
//				alpha.setValue(0.0f);
//				alpha.resetDelta();
//			} else {
//				alpha.adjustValue(AvtSimple.FEEDBACK_GOOD);
//			}		
//			
//			previousSkew = skew;
//									
//			logicalClock.rate += alpha.getValue()*(float)skew;					
//				
//		}
//		else{
//			previousSkew = 0;
//		}
//		
//		logicalClock.setValue(logicalClock.getValue(updateTime).add(skew),updateTime);		
//	}
		
//	float K_max = 1.0f/(float)BEACON_RATE;
//	float K_max = 1.0f/((float)BEACON_RATE*2.0f*MAX_PPM);
//	
//	float alpha = 0.0f;
//	final float INCREMENT  = 0.000001f/((float)BEACON_RATE*2.0f*MAX_PPM);
//	float incrementAmount = INCREMENT;	
//	int previousSkew = 0;
//	
//	private void algorithm3(RadioPacket packet) {
//		UInt32 updateTime = packet.getEventTime();
//		logicalClock.update(updateTime);
//		PIFloodingMessage msg = (PIFloodingMessage)packet.getPayload();
//
//		if( msg.rootid < outgoingMsg.rootid) {
//			outgoingMsg.rootid = msg.rootid;
//			outgoingMsg.sequence = msg.sequence;
//		} else if (outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0) {
//			outgoingMsg.sequence = msg.sequence;
//		}
//		else {
//			return;
//		}
//	
//		int skew = calculateSkew(packet);					
//		
//		float boundary = 2.0f*MAX_PPM*(float)BEACON_RATE + 10000; // + maybe a tolerance;
//		
//		/*  initial offset compensation */ 
//		if(Math.abs(skew) <= boundary){			
//			
//			if ((skew > 0 && previousSkew > 0) || (skew < 0 && previousSkew < 0)){
//				alpha += incrementAmount;
//				incrementAmount *= 2.0f;
//				
////				if(alpha > K_max){
////					alpha = K_max;
////					incrementAmount /= 2.0f;
////				}
//				
//			}
//			else if ((skew > 0 && previousSkew < 0) || (skew < 0 && previousSkew > 0)) {
//				alpha = 0.0f;
//				incrementAmount = INCREMENT;
//			}
//			
//			previousSkew = skew;
//			
//			logicalClock.rate += alpha*(float)skew;
//				
//		}
////		else{
////			previousSkew = 0;
////			alpha = 0.0f;
////			incrementAmount = INCREMENT;
////		}
//		
//		logicalClock.setValue(logicalClock.getValue(updateTime).add(skew),updateTime);		
//	}
//
	
	void processMsg() {
//		algorithm1(processedMsg);
		algorithm2(processedMsg);
//		algorithm3(processedMsg);
	}

	@Override
	public void receiveMessage(RadioPacket packet) {
		processedMsg = packet;
		processMsg();
	}

	@Override
	public void fireEvent(Timer timer) {
		sendMsg();
	}

	private void sendMsg() {
		UInt32 localTime, globalTime;
		
		localTime = CLOCK.getValue();
		globalTime = logicalClock.getValue(localTime);
		
		if( outgoingMsg.rootid == NODE_ID ) {
			outgoingMsg.clock = new UInt32(localTime);
		}
		else{
			outgoingMsg.clock = new UInt32(globalTime);	
		}
		
		RadioPacket packet = new RadioPacket(new PIFloodingMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new UInt32(localTime));
		MAC.sendPacket(packet);	
		
		if (outgoingMsg.rootid == NODE_ID)
			++outgoingMsg.sequence;
	}

	@Override
	public void on() throws Exception {
		super.on();
		
		if(this.NODE_ID == 1)
			timer0.startPeriodic(BEACON_RATE+((Simulator.random.nextInt() % 100) + 1)*10000);
	}

	public UInt32 local2Global() {
		return logicalClock.getValue(CLOCK.getValue());
	}

//	boolean changed = false;
	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " "
				+ Float.floatToIntBits((float) ((1.0 + logicalClock.rate) * (1.0 + CLOCK.getDrift())));
//				+ Float.floatToIntBits((float) (increment));//		
//		if(Simulator.getInstance().getSecond()>=100000)
//		{
//			/* to start clock with a random value */
//			if(this.NODE_ID == 1){
//				if(changed == false){
//					CLOCK.setDrift(0.0001f);
//					changed = true;
//				}				
//			}
//		}
//		+ Float.floatToIntBits(K_i);
//		System.out.println("" + NODE_ID + " "
//				+ (1.0 + (double) logicalClock.rate)
//				* (1.0 + CLOCK.getDrift()));

		return s;
	}
}
