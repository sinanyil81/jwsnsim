package application.appEgtsp;

import nodes.CSMA;
import nodes.Node;
import nodes.Position;
import core.Simulator;
import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import hardware.transceiver.Packet;
import hardware.transceiver.Transceiver;
import application.regression.LeastSquares;
import sim.clock.ConstantDriftClock;
import sim.clock.DynamicDriftClock;
import sim.statistics.Distribution;

public class GradientNode extends Node implements TimerHandler {

	private static final int MAX_NEIGHBORS = 10;
	
	private static final int BEACON_RATE = 30000000;  
	private static final long NEIGHBOR_REMOVE = BEACON_RATE * 5; 

	Neighbor neighbors[] = new Neighbor[MAX_NEIGHBORS];
	int numNeighbors = 0;

	LeastSquares ls = new LeastSquares();
	GradientClock logicalClock = new GradientClock();
	Timer timer0;

	Packet processedMsg = null;
	GradientMessage outgoingMsg = new GradientMessage();

	public GradientNode(int id, Position position) {
		super(id, position);

		CLOCK = new DynamicDriftClock();
		MAC = new CSMA(this);
		RADIO = new Transceiver(this, MAC);

		timer0 = new Timer(CLOCK, this);
		
		outgoingMsg.sequence = 0;
		outgoingMsg.rootid = this.NODE_ID;
		
		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new Neighbor();
		}
		
		/* to start clock with a random value */
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));

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

	private void addEntry(GradientMessage msg, Register32 eventTime) {

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
			neighbors[index].rootClock = new Register32(msg.globalTime);
			neighbors[index].rootRate = msg.rootMultiplier;
			neighbors[index].addNewEntry(msg.localTime,eventTime);
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
		GradientMessage msg = (GradientMessage) processedMsg.getPayload();
		
		addEntry(msg, processedMsg.getEventTime());
		
		updateLogicalClock(processedMsg.getEventTime());			
							
		if(msg.rootid < outgoingMsg.rootid){
			outgoingMsg.rootid = msg.rootid;
			outgoingMsg.sequence = msg.sequence;
		} else if (outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;		
		}
		else {
			return;
		}
		
		Register32 time = logicalClock.getValue(processedMsg.getEventTime());
		int error = time.subtract(msg.globalTime).toInteger();
		
		if( error > 1000 || error < -1000){
			logicalClock.setValue(msg.globalTime);
			logicalClock.setUpdateLocalTime(processedMsg.getEventTime());
			logicalClock.setOffset(new Register32());
		}
				
		logicalClock.setRootRate(msg.rootMultiplier);
		logicalClock.setRootOffset(msg.rootOffset);
		
	}
		
	private void updateLogicalClock(Register32 eventTime) {
		Register32 time = logicalClock.getValue(eventTime);		
		
		float rate = getClockRate();			
		Register32 offset = getOffset(time,eventTime);
		
//		logicalClock.setValue(time);
//		logicalClock.setUpdateLocalTime(eventTime);
		
		logicalClock.update(eventTime);
		
		logicalClock.setOffset(offset);
		logicalClock.setRate(rate);
		if (outgoingMsg.rootid == NODE_ID){
			logicalClock.setRootRate(rate);
		}
	}

	public Register32 getOffset(Register32 time,Register32 localTime){
		//UInt32 offset = logicalClock.getOffset();
		Register32 offset = new Register32();
		
		int diff = 0;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].free == false){
				Register32 nclock = neighbors[i].getClock(localTime);
				diff = nclock.subtract(time).toInteger();
				if(Math.abs(diff) <= 500)
					offset = offset.add(diff/(this.numNeighbors+1));								
			}
		}
		
		
		return offset.add(logicalClock.getOffset());
	}

	private float getClockRate() {
		float rateSum = (float) logicalClock.getRate();
		int numNeighbors = 0;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].free == false){
				rateSum += neighbors[i].relativeRate*neighbors[i].rate + neighbors[i].rate+neighbors[i].relativeRate;
				numNeighbors++;
			}
		}
		
		this.numNeighbors = numNeighbors;
		
		return rateSum/(float)(numNeighbors+1);		
	}

	@Override
	public void receiveMessage(Packet packet) {
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
		globalTime = local2Global();
	
		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.localTime = new Register32(localTime);
		outgoingMsg.multiplier = (float) logicalClock.getRate();
		outgoingMsg.rootMultiplier = (float) logicalClock.getRootRate();
		outgoingMsg.globalTime = new Register32(globalTime);
		
		if (outgoingMsg.rootid == NODE_ID){
			logicalClock.setRootOffset(new Register32(globalTime.subtract(localTime)));
		}
		
		outgoingMsg.rootOffset = logicalClock.getRootOffset();
				
		Packet packet = new Packet(new GradientMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register32(localTime));
		MAC.sendPacket(packet);	

		if (outgoingMsg.rootid == NODE_ID)
			++outgoingMsg.sequence;
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
		//s += " " + local2Global().toString();
		//s += " " + local2Global().toString();
//		s += " " + logicalClock.getRTValue(CLOCK.getValue()).toString();
		s += " " + logicalClock.getValue(CLOCK.getValue()).toString();
		//s += " " + logicalClock.getValue().toString();
		//s += " " + logicalClock.getOffset().toLong();
//		s += " " + Float.floatToIntBits((1.0f+logicalClock.getRate())*(float)(1.0f+CLOCK.getDrift()));		
		s += " " + Float.floatToIntBits(logicalClock.getRate());
		//s += " " + CLOCK.getValue().toString();
		//s += " " + logicalClock.getRootRate();

		return s;
	}
}
