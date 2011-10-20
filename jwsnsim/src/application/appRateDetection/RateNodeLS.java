package application.appRateDetection;

import application.regression.LeastSquares;
import application.regression.RegressionEntry;
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

public class RateNodeLS extends Node implements TimerHandler {

	private static final int MAX_NEIGHBORS = 8;
	private static final int BEACON_RATE = 30000000;  

	Neighbor neighbors[] = new Neighbor[MAX_NEIGHBORS];
	int numNeighbors = 0;

	LeastSquares ls = new LeastSquares();
	Timer timer0;

	RadioPacket processedMsg = null;
	RateMessage outgoingMsg = new RateMessage();
    
	float myRate = 1;
	
	public RateNodeLS(int id, Position position) {
		super(id, position);
		
		if( 1 == NODE_ID ) {
			CLOCK = new ConstantDriftClock(0);
		}
		else {
			CLOCK = new ConstantDriftClock();	
		}
		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this, MAC);

		timer0 = new Timer(CLOCK, this);

		outgoingMsg.sequence = 0;

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
	
	private int getFreeSlot() {
		int i, freeItem = -1;

		for (i = 0; i < MAX_NEIGHBORS; ++i) {
			
			if(neighbors[i].free){
				freeItem = i;
			}
		}

		return freeItem;
	}

	private float addEntry(RateMessage msg, UInt32 eventTime) {

		boolean found = false;
						
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
			neighbors[index].rate = msg.rate;			
			neighbors[index].addNewEntry(msg.clock,eventTime);
			neighbors[index].timestamp = new UInt32(eventTime);

			if(found){
				ls.calculate(neighbors[index].table, neighbors[index].tableEntries);
				neighbors[index].relativeRate = ls.getSlope();				
			}
			else{
				neighbors[index].relativeRate = 0;
			}
			
			return neighbors[index].relativeRate;
		}
		return 1.0f;
	}

	void processMsg() {
		RateMessage msg = (RateMessage) processedMsg.getPayload();

		float rr = addEntry(msg, processedMsg.getEventTime());
				
		if ((msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;
			
			if(NODE_ID !=1){
				myRate = rr*msg.rate;
			}
		}
		else {
			return;
		}
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
		UInt32 localTime = CLOCK.getValue();

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = new UInt32(localTime);
		outgoingMsg.rate = myRate;
		
		if( 1 == NODE_ID ) {
			outgoingMsg.sequence++;
		}
		
		RadioPacket packet = new RadioPacket(new RateMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new UInt32(localTime));
		MAC.sendPacket(packet);	
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE+((Simulator.random.nextInt() % 100) + 1)*10000);
	}

	public String toString() {
		String s = Simulator.getInstance().getSecond().toString(10);

		s += " " + NODE_ID;
		s += " " + (1.0f + (float)CLOCK.getDrift());
		s += " " + myRate;
		s += " " + (1.0f + (float)CLOCK.getDrift()-myRate);

		return s;
	}
}
