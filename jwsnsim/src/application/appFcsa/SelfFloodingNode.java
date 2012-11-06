package application.appFcsa;

import application.appSelf.ClockSpeedAdapter;
import application.appSelf.ClockSpeedAdapter2;
import application.regression.LeastSquares;
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

public class SelfFloodingNode extends Node implements TimerHandler {

	private static final int BEACON_RATE = 30000000;  
	private static final int ROOT_TIMEOUT = 5;
	private static final int IGNORE_ROOT_MSG = 4;	

	LogicalClock logicalClock = new LogicalClock();
	Timer timer0;

	RadioPacket processedMsg = null;
	SelfFloodingMessage outgoingMsg = new SelfFloodingMessage();
	
	ClockSpeedAdapter speedAdapter = new ClockSpeedAdapter();
//	ClockSpeedAdapter2 speedAdapter = new ClockSpeedAdapter2();
    
	int heartBeats; // the number of sucessfully sent messages
    // since adding a new entry with lower beacon id than ours	

	public SelfFloodingNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);
		
		heartBeats = 0;

		outgoingMsg.sequence = 0;
		outgoingMsg.rootid = 0xFFFF;
	}
	
	UInt32 lastValue = new UInt32();
	UInt32 lastBroadcast = new UInt32();
 
	void processMsg() {
		SelfFloodingMessage msg = (SelfFloodingMessage) processedMsg.getPayload();
		
		speedAdapter.adjust(msg.nodeid,msg.clock,processedMsg.getEventTime(),msg.multiplier);
//		speedAdapter.adjust(msg.nodeid,msg.progress,processedMsg.getEventTime());
		logicalClock.rate = speedAdapter.getSpeed();
		
		if( msg.rootid < outgoingMsg.rootid &&
	            //after becoming the root, a node ignores messages that advertise the old root (it may take
	            //some time for all nodes to timeout and discard the old root) 
	            !(heartBeats < IGNORE_ROOT_MSG && outgoingMsg.rootid == NODE_ID)){
			outgoingMsg.rootid = msg.rootid;
			outgoingMsg.sequence = msg.sequence;
		} else if (outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;
		}
		else {
			return;
		}
		
		updateClock(msg.rootClock,processedMsg.getEventTime());
	}

	private void updateClock(UInt32 rootClock,UInt32 eventTime) {
		
		logicalClock.setValue(rootClock);
		logicalClock.updateLocalTime = eventTime;
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
			updateClock(globalTime, localTime);
		}
		else if( heartBeats >= ROOT_TIMEOUT ) {
            heartBeats = 0; //to allow ROOT_SWITCH_IGNORE to work
            outgoingMsg.rootid = NODE_ID;
            outgoingMsg.sequence++; // maybe set it to zero?
		}
		
		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = new UInt32(localTime);
		outgoingMsg.multiplier = (float) logicalClock.rate;
		
		outgoingMsg.rootClock = new UInt32(globalTime);
		
		outgoingMsg.progress = speedAdapter.getValue(localTime).subtract(lastValue).toInteger();
		lastValue = speedAdapter.getValue(localTime);
		
//		if(lastBroadcast.toInteger()!=0){
//			int progress = localTime.subtract(lastBroadcast).toInteger();
//			int val = outgoingMsg.progress-progress;
//			outgoingMsg.multiplier = (float)val/(float)progress;
//		}
		
		lastBroadcast = new UInt32(localTime);
		
		RadioPacket packet = new RadioPacket(new SelfFloodingMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new UInt32(localTime));
		MAC.sendPacket(packet);	

		if (outgoingMsg.rootid == NODE_ID)
			++outgoingMsg.sequence;
		
		++heartBeats;
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE+((Simulator.random.nextInt() % 100) + 1)*10000);
	}

	public UInt32 local2Global() {
		return logicalClock.getValue(CLOCK.getValue());
	}

	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " " + Float.floatToIntBits((1.0f+logicalClock.rate)*(float)(1.0f+CLOCK.getDrift()));
		System.out.println(""+NODE_ID+" "+(1.0+(double)logicalClock.rate)*(1.0+CLOCK.getDrift()));
//		System.out.println(""+NODE_ID+" "+(double)logicalClock.rate*1000000.0);
//		s += " " + Float.floatToIntBits(logicalClock.rate);

		return s;
	}
}
