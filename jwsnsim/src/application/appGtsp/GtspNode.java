package application.appGtsp;

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

public class GtspNode extends Node implements TimerHandler{
	
	private static final int BEACON_RATE = 30000000;
	private static final int MAX_NEIGHBOR = 2;
	
	Timer timer0;
			
    RadioPacket processedMsg = null;
    GtspMessage outgoingMsg = new GtspMessage();
    
    private class NeighborEntry{
    	public boolean free = true;
    	
    	public int id = 0;

    	UInt32 L_j; 
    	public int L_j_minus_L_i  = 0;   	
    	public double l_j = 1;
    	public double x_j_over_h_i = 1;
    	public UInt32 timestamp = new UInt32();
    	    	
    	public boolean processed = true;    	
    }
    
    NeighborEntry neighbors[] = new NeighborEntry[MAX_NEIGHBOR];
    int numNeighbors = 0;
       
    LogicalClock logicalClock = new LogicalClock();
    
	public GtspNode(int id, Position position) {
		super(id,position);
		
		CLOCK = new ConstantDriftClock();		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this,MAC);
		
		timer0 = new Timer(CLOCK,this);		
		
		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new NeighborEntry();
		}
	}
		
	private void copyMessage(GtspMessage message, NeighborEntry entry,UInt32 receiveTime){
		entry.id = message.nodeid;
		
		UInt32 nodeLogicalClock = logicalClock.getValue(receiveTime);		
		entry.L_j_minus_L_i = (message.logicalClock.subtract(nodeLogicalClock)).toInteger();
		entry.l_j = message.rate;
							
		if(entry.timestamp.getValue() != 0){
			entry.x_j_over_h_i =(message.logicalClock.subtract(entry.L_j)).toDouble()/(receiveTime.subtract(entry.timestamp)).toDouble();
		}
		
		entry.timestamp = receiveTime;
		entry.L_j = message.logicalClock;
		
		entry.processed = false;
		entry.free = false;
	}
	
	private void storeMessage(GtspMessage message,UInt32 receiveTime){
		boolean found = false;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(message.nodeid == neighbors [i].id){
				copyMessage(message, neighbors[i], receiveTime);
				found = true;
				break;
			}
		}
		
		if(!found){
			for (int i = 0; i < neighbors.length; i++) {
				if(neighbors [i].free){
					copyMessage(message, neighbors[i], receiveTime);
					numNeighbors++;
					break;
				}
			}
		}		
	}
		
	private void updateLogicalClock(){
		
		double rateSum = logicalClock.rate;
		long offsetSum = logicalClock.offset;
		//long offsetSum = 0;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors [i].processed == false && !neighbors [i].free){
				rateSum += neighbors[i].x_j_over_h_i;
				offsetSum += neighbors[i].L_j_minus_L_i;
				neighbors [i].processed = true;
			}
		}
		
		UInt32 localTime = CLOCK.getValue();
		logicalClock.value = logicalClock.getValue(localTime);
		logicalClock.rate  = rateSum/(double)(numNeighbors+1);
		logicalClock.offset =  offsetSum / (numNeighbors+1);
		logicalClock.updateLocalTime = localTime;
	}
	
    void processMsg()
    {
        GtspMessage msg = (GtspMessage)processedMsg.getPayload();
        storeMessage(msg, processedMsg.getEventTime());
        
        int numCollectedMessages = 0;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors [i].processed == false ){
				numCollectedMessages++;
			}
		}
		
		if(numCollectedMessages > 0 && numCollectedMessages == numNeighbors){
			updateLogicalClock();	
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
        UInt32 localTime;

        localTime = CLOCK.getValue();

        outgoingMsg.nodeid = NODE_ID;
        outgoingMsg.logicalClock = logicalClock.getValue(localTime);
        outgoingMsg.rate = logicalClock.rate;
        
        RadioPacket packet = new RadioPacket(new GtspMessage(outgoingMsg));
        packet.setSender(this);
        packet.setEventTime(new UInt32(localTime));
        MAC.sendPacket(packet);                            
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE);
	}	
	
	public String toString(){
		String s = Simulator.getInstance().getSecond().toString(10);
		
		UInt32 currentTime = CLOCK.getValue();
		s += " " + NODE_ID;
		s += " " + logicalClock.getValue(currentTime).toString();
		s += " 0";
		s += " " + Float.floatToIntBits((float) logicalClock.rate);
		s += " 0";
		s += " 0";
		s += " 0";
//		s += " " + logicalClock.offset;
//		s += " " + logicalClock.rate;
		
		return s;		
	}
}
