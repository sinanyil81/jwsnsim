package application.appFcsa;

import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import hardware.transceiver.RadioPacket;
import hardware.transceiver.SimpleRadio;
import application.appSelf.ClockSpeedAdapter;
import application.regression.LeastSquares;
import sim.clock.ConstantDriftClock;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

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
		Register32 age;

		Register32 localTime = CLOCK.getValue();

		for (i = 0; i < MAX_NEIGHBORS; ++i) {
			age = new Register32(localTime);
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

	private void addEntry(FloodingMessage msg, Register32 eventTime) {

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
			neighbors[index].addNewEntry(msg.clock,eventTime);
			neighbors[index].timestamp = new Register32(eventTime);
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

	private void updateClock(Register32 rootClock,Register32 eventTime) {
		
		logicalClock.setValue(rootClock);
		logicalClock.updateLocalTime = eventTime;
	}

	private void updateClockRate() {
		
//		if(this.NODE_ID == outgoingMsg.rootid)
//			return;
		
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
		Register32 localTime, globalTime;
		
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
		outgoingMsg.clock = new Register32(localTime);
		outgoingMsg.multiplier = (float) logicalClock.rate;
		
		outgoingMsg.rootClock = new Register32(globalTime);
		
		RadioPacket packet = new RadioPacket(new FloodingMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register32(localTime));
		MAC.sendPacket(packet);	

		if (outgoingMsg.rootid == NODE_ID)
			++outgoingMsg.sequence;
		
		++heartBeats;
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE+((Distribution.getRandom().nextInt() % 100) + 1)*10000);
	}

	public Register32 local2Global() {
		return logicalClock.getValue(CLOCK.getValue());
	}

	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " " + Float.floatToIntBits((1.0f+logicalClock.rate)*(float)(1.0f+CLOCK.getDrift()));

		System.out.println(""+NODE_ID+" "+(1.0+(double)logicalClock.rate)*(1.0+CLOCK.getDrift()));

		return s;
	}
}
