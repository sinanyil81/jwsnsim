package application.appRateDetection;

import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import hardware.transceiver.RadioPacket;
import hardware.transceiver.SimpleRadio;
import application.regression.LeastSquares;
import sim.clock.ConstantDriftClock;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class RateNode extends Node implements TimerHandler {

	private static final int MAX_NEIGHBORS = 8;
	private static final int BEACON_RATE = 30000000;  

	Neighbor neighbors[] = new Neighbor[MAX_NEIGHBORS];
	int numNeighbors = 0;

	LeastSquares ls = new LeastSquares();
	Timer timer0;

	RadioPacket processedMsg = null;
	RateMessage outgoingMsg = new RateMessage();
    
	float myRate = 0;
	float x = 1;
	
	public RateNode(int id, Position position) {
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

	private void addEntry(RateMessage msg, Register32 eventTime) {

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
		RateMessage msg = (RateMessage) processedMsg.getPayload();

		addEntry(msg, processedMsg.getEventTime());
		updateClockRate();
		
		if ((msg.sequence - outgoingMsg.sequence) > 0) {
			outgoingMsg.sequence = msg.sequence;
			x = msg.x;			
		}
		else {
			return;
		}
	}

	private void updateClockRate() {
		float rateSum = myRate;
		int numNeighbors = 0;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].free == false){
				rateSum += neighbors[i].relativeRate*neighbors[i].rate + neighbors[i].rate+neighbors[i].relativeRate;
				numNeighbors++;
			}
		}
		
		myRate = rateSum/(float)(numNeighbors+1);
		
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
		Register32 localTime = CLOCK.getValue();

		outgoingMsg.nodeid = NODE_ID;
		outgoingMsg.clock = new Register32(localTime);
		outgoingMsg.rate = myRate;
		
		if( 1 == NODE_ID ) {
			outgoingMsg.sequence++;
			outgoingMsg.x = (myRate+1.0f);
		}
		else{
			outgoingMsg.x = x;
		}	
		
		RadioPacket packet = new RadioPacket(new RateMessage(outgoingMsg));
		packet.setSender(this);
		packet.setEventTime(new Register32(localTime));
		MAC.sendPacket(packet);	
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE+((Distribution.getRandom().nextInt() % 100) + 1)*10000);
	}

	public String toString() {
		String s = "" + Simulator.getInstance().getSecond();

		s += " " + NODE_ID;
		//s += " " + Float.floatToIntBits(1.0f + (float)CLOCK.getDrift());
//		s += " " + x/(myRate+1.0f);
		s += " " + x;
//		s += " " + (1.0f+(float) CLOCK.getDrift());
		s += " " + (myRate+1.0f)*(1.0f+(float) CLOCK.getDrift());
		//s += " " + Float.floatToIntBits(x/(myRate+1.0f));
		//s += " " + Float.floatToIntBits(1.0f + (float)CLOCK.getDrift()-x/(myRate+1.0f));

		return s;
	}
}
