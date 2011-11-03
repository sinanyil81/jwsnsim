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

public class FirstGtspNode extends Node implements TimerHandler{
	
	private static final int BEACON_RATE = 30000000;
	private static final int MAX_NEIGHBOR = 2;
	
	Timer timer0;
			
    RadioPacket processedMsg = null;
    GtspMessage outgoingMsg = new GtspMessage();
    
    private class NeighborEntry{
    	public boolean free = true;
    	
    	public int id = 0;

    	public int L_j_minus_L_i  = 0;   	
    	public double l_j = 1;

    	/* timestamps in order to estimate relative hardware clock rate */
    	public UInt32 x1 = new UInt32();
    	public UInt32 y1 = new UInt32();
    	public UInt32 x2 = new UInt32();
    	public UInt32 y2 = new UInt32();
    	
    	double h_j_over_h_i = 1;    	
    	public boolean processed = true;    	
    }
    
    NeighborEntry neighbors[] = new NeighborEntry[MAX_NEIGHBOR];
    int numNeighbors = 0;
       
    UInt32 logicalClock = new UInt32();
    long logicalClockOffset = 0;
    double logicalClockRate = 1;
    
    UInt32 updateLocalTime = new UInt32();    
    
	public FirstGtspNode(int id, Position position) {
		super(id,position);
		
		CLOCK = new ConstantDriftClock();		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this,MAC);
		
		timer0 = new Timer(CLOCK,this);		
		
		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new NeighborEntry();
		}
	}
	
	public UInt32 getLogicalClock(UInt32 currentTime){
		long timePassed = currentTime.subtract(updateLocalTime).getValue();
		long progress = (long)((double)timePassed*logicalClockRate) + logicalClockOffset;
		
		return logicalClock.add(new  UInt32(progress));
	}
	
	private void copyMessage(GtspMessage message, NeighborEntry entry,UInt32 receiveTime){
		entry.id = message.nodeid;
		
		UInt32 nodeLogicalClock = getLogicalClock(receiveTime);		
		entry.L_j_minus_L_i = (message.logicalClock.subtract(nodeLogicalClock)).toInteger();
		entry.l_j = message.rate;
		
		entry.x1 = new UInt32(entry.x2);
		entry.y1 = new UInt32(entry.y2);
		entry.x2 = new UInt32(receiveTime);
		/* TODO entry.y2 = new UInt32(message.hardwareClock); */
				
		if(entry.x1.getValue() != 0 && entry.y1.getValue() != 0){
			entry.h_j_over_h_i =(entry.y2.subtract(entry.y1)).toDouble()/(entry.x2.subtract(entry.x1)).toDouble();
		}
		
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
		
		double rateSum = logicalClockRate;
		long offsetSum = logicalClockOffset;
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors [i].processed == false && !neighbors [i].free){
				rateSum += neighbors[i].h_j_over_h_i*neighbors[i].l_j;
				offsetSum += neighbors[i].L_j_minus_L_i;
				neighbors [i].processed = true;
			}
		}
		
		UInt32 localTime = CLOCK.getValue();
		logicalClock = getLogicalClock(localTime);
		
		logicalClockRate   = rateSum/(double)(numNeighbors+1);
		logicalClockOffset = offsetSum / (numNeighbors+1);
		updateLocalTime = localTime;
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
        UInt32 localTime, logicalTime;

        localTime = CLOCK.getValue();
        logicalTime = getLogicalClock(localTime);

        outgoingMsg.nodeid = NODE_ID;
        outgoingMsg.logicalClock = logicalTime;
        /* TODO outgoingMsg.hardwareClock = localTime; */
        outgoingMsg.rate = logicalClockRate;
        
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
		s += " " + getLogicalClock(currentTime).toString();
		s += " 0";
		s += " " + Float.floatToIntBits((float) logicalClockRate);
		s += " 0";
		s += " 0";
		s += " 0";
		s += " " + logicalClockOffset;
		s += " " + logicalClockRate;
		
		return s;		
	}
}
