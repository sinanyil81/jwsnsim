package application.appGradient;

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

public class FloodingNode extends Node implements TimerHandler {

	private static final int MAX_NEIGHBORS = 8;
	
	private static final int BEACON_RATE = 30000000;  
	private static final int ROOT_TIMEOUT = 5;
	private static final int IGNORE_ROOT_MSG = 4;	
	private static final long NEIGHBOR_REMOVE = BEACON_RATE * 5; 

	Neighbor neighbors[] = new Neighbor[MAX_NEIGHBORS];
	int numNeighbors = 0;

	LeastSquares ls = new LeastSquares();
	LogicalClock logicalClock = new LogicalClock();
	Timer timer0;

	RadioPacket processedMsg = null;
	FloodingMessage outgoingMsg = new FloodingMessage();
    
	int heartBeats; // the number of sucessfully sent messages
    // since adding a new entry with lower beacon id than ours	

	public FloodingNode(int id, Position position) {
		super(id, position);

		CLOCK = new ConstantDriftClock();
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);
		
		heartBeats = 0;

		outgoingMsg.sequence = 0;
		outgoingMsg.rootid = 0xFFFF;

		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new Neighbor();
		}
	}

	private int findNeighborSlot(int id) {
		for (int i = 0; i < neighbors.length; i++) {
			if ((neighbors[i].free == false) && (neighbors[i].id == id)) {
				return i;
			}
		}

		return -1;
	}
	
	private void updateNeighborhood(){
		int i;
		UInt32 age;

		UInt32 localTime = CLOCK.getValue();

		for (i = 0; i < MAX_NEIGHBORS; ++i) {
			age = new UInt32(localTime);
			age = age.subtract(neighbors[i].timestamp);
			
			if(age.toLong() >= NEIGHBOR_REMOVE && neighbors[i].free == false) {
				neighbors[i].free = true;
				neighbors[i].clearTable();
			}
		}
	}

	private int getFreeSlot() {
		int i, freeItem = -1;

		for (i = 0; i < MAX_NEIGHBORS; ++i) {
			
			if(neighbors[i].free){
				freeItem = i;
			}
		}

		return freeItem;
	}

	private void addEntry(FloodingMessage msg, UInt32 eventTime) {

		boolean found = false;
				
		updateNeighborhood();
		
		/* find and add neighbor */
		int index = findNeighborSlot(msg.nodeid);
		
		if(index >= 0){
			found = true;
		}
		else{
			index = getFreeSlot();
		}

		if (index >= 0) {

			
			neighbors[index].free = false;
			neighbors[index].id = msg.nodeid;
			neighbors[index].rate = msg.multiplier;
			neighbors[index].rootClock = new UInt32(msg.rootClock);			
			neighbors[index].addNewEntry(msg.clock,eventTime);
			neighbors[index].timestamp = new UInt32(eventTime);
			if(found){
				ls.calculate(neighbors[index].table, neighbors[index].tableEntries);
				neighbors[index].relativeRate = ls.getSlope();
			}
			else{
				neighbors[index].relativeRate = 0;
			}						
		}
	}

	void processMsg() {
		FloodingMessage msg = (FloodingMessage) processedMsg.getPayload();

		addEntry(msg, processedMsg.getEventTime());
		updateClockRate();
		
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

	private void updateClockRate() {
		float rateSum = (float) logicalClock.rate;
		int numNeighbors = 0;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].free == false){
				rateSum += neighbors[i].relativeRate*neighbors[i].rate + neighbors[i].rate+neighbors[i].relativeRate;
				numNeighbors++;
			}
		}
		
		logicalClock.rate = rateSum/(float)(numNeighbors+1);
		
		this.numNeighbors = numNeighbors;
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
		
		RadioPacket packet = new RadioPacket(new FloodingMessage(outgoingMsg));
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
	
	public UInt32 gradientClock(){
		UInt32 gclock = logicalClock.getValue(CLOCK.getValue());
		int diffSum = 0;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].free == false){
				UInt32 nclock = neighbors[i].getClock(CLOCK.getValue());
				diffSum += nclock.subtract(gclock).toInteger()/(this.numNeighbors+1);
			}
		}
		
		gclock.add(diffSum);
		
		return gclock;
	}

	public String toString() {
		String s = Simulator.getInstance().getSecond().toString(10);

		s += " " + NODE_ID;
		//s += " " + local2Global().toString();
		s += " " + gradientClock().toString();
//		s += " " + Float.floatToIntBits((1.0f+logicalClock.rate)*(float)(1.0f+CLOCK.getDrift()));
		s += " " + Float.floatToIntBits(logicalClock.rate);

		return s;
	}
}
