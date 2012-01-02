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

public class ModifiedGtspNode extends Node implements TimerHandler{
	
	private static final int BEACON_RATE = 30000000;
	private static final int MAX_NEIGHBORS = 8;
		
	class TableEntry{
		public int id;		
		public UInt32 logicalClock 	= new UInt32();		
		public UInt32 timestamp	 	= new UInt32();
		
		double rate;
		long offset;
		
		public boolean free = true; 
		public boolean processed = true;
	}
	
	Timer beaconTimer;
				
    RadioPacket processedMsg = null;
    ModifiedGtspMessage outgoingMsg = new ModifiedGtspMessage();
    
    LogicalClock logicalClock = new LogicalClock();
    
    TableEntry neighbors[] = new TableEntry[MAX_NEIGHBORS];
    int numNeighbors = 0;
    
    double rateSum = 1;
    long offsetSum = 0;    
    int numReceivedPackets = 0;    
        
	public ModifiedGtspNode(int id, Position position) {
		super(id,position);
		
		CLOCK = new ConstantDriftClock();		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this,MAC);
		
		beaconTimer = new Timer(CLOCK,this);
		
		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new TableEntry();
		}
	}
		
	private void updateLogicalClock(){
		
		double rateSum = logicalClock.rate;
		long offsetSum = logicalClock.offset;
					
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors [i].processed == false && !neighbors [i].free){
				rateSum += neighbors[i].rate;
				offsetSum += neighbors[i].offset;
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
        ModifiedGtspMessage msg = (ModifiedGtspMessage)processedMsg.getPayload();
        UInt32 eventTime = processedMsg.getEventTime();
        UInt32 logicalTime = logicalClock.getValue(eventTime);
        
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].free == false){
				if(neighbors[i].id == msg.nodeid){
					
					neighbors[i].rate = msg.logicalClock.subtract(neighbors[i].logicalClock).toDouble()/eventTime.subtract(neighbors[i].timestamp).toDouble();					
					neighbors[i].offset = msg.logicalClock.subtract(logicalTime).toInteger();
			        
					neighbors[i].processed = false;
			        			        
			        neighbors[i].logicalClock = new UInt32(msg.logicalClock);
			        neighbors[i].timestamp = new UInt32(eventTime); 
			        			        
			        return;
				}
			}
		}
		
		addEntry(msg,eventTime);                   
    }
	
	@Override
	public void receiveMessage(RadioPacket packet) {		
		processedMsg = packet;
		processMsg();	
		
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
	public void fireEvent(Timer timer) {
		if(timer == beaconTimer)
			sendBeacon();
	}
	
	private void sendBeacon() {
        		
		UInt32 localTime = CLOCK.getValue();
		
		outgoingMsg.type = ModifiedGtspMessage.BEACON;
        outgoingMsg.nodeid = NODE_ID;
        
        outgoingMsg.logicalClock = logicalClock.getValue(localTime);
        
        RadioPacket packet = new RadioPacket(new ModifiedGtspMessage(outgoingMsg));
        packet.setSender(this);
        packet.setEventTime(localTime);
        MAC.sendPacket(packet);
	}

	private void addEntry(ModifiedGtspMessage msg, UInt32 eventTime) {
		int i, freeItem = -1, oldestItem = 0;
        UInt32 age, oldestTime = new UInt32();
        
        UInt32 localTime = CLOCK.getValue();
        
        int tableEntries = 0;
        
        for(i = 0; i < MAX_NEIGHBORS; ++i) {
        	age = new UInt32(localTime);
        	age = age.subtract(neighbors[i].timestamp);

            //logical time error compensation
            if( age.toLong() >= 0x7FFFFFFFL )
            	neighbors[i].free = true;

            if( neighbors[i].free)
                freeItem = i;
            else
            	tableEntries++;

            if( age.compareTo(oldestTime) >= 0 ) {
                oldestTime = age;
                oldestItem = i;
            }
        }

        if( freeItem < 0 )
            freeItem = oldestItem;
        else
            ++tableEntries;

    	neighbors[freeItem].free = false;
    	neighbors[freeItem].id = msg.nodeid;
    	neighbors[freeItem].logicalClock = new UInt32(msg.logicalClock);
    	neighbors[freeItem].timestamp = new UInt32(eventTime);
    	
    	numNeighbors = tableEntries;
	}

	@Override
	public void on() throws Exception {
		super.on();
		beaconTimer.startPeriodic(BEACON_RATE);
	}	
	
	public String toString(){
		String s = Simulator.getInstance().getSecond().toString(10);
		
		UInt32 currentTime = CLOCK.getValue();
		s += " " + NODE_ID;
		s += " " + logicalClock.getValue(currentTime).toString();
		s += " " + Float.floatToIntBits((float) logicalClock.rate);
		
		return s;		
	}
}
