package application.appGradient;

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

public class FtspNode extends Node implements TimerHandler{
	
	private static final int MAX_ENTRIES           = 8;              	// number of entries in the table
	private static final int BEACON_RATE           = 30000000;  	 	// how often send the beacon msg (in seconds)
	private static final int ROOT_TIMEOUT          = 5;              	//time to declare itself the root if no msg was received (in sync periods)
	private static final int IGNORE_ROOT_MSG       = 4;              	// after becoming the root ignore other roots messages (in send period)
	private static final int ENTRY_VALID_LIMIT     = 4;              	// number of entries to become synchronized
	private static final int ENTRY_SEND_LIMIT      = 3;              	// number of entries to send sync messages
	private static final int ENTRY_THROWOUT_LIMIT  = Integer.MAX_VALUE;	// if time sync error is bigger than this clear the table
	
	private static final int MAX_NEIGHBORS = 8;
	private static final long NEIGHBOR_REMOVE = BEACON_RATE * 5; 

	Neighbor neighbors[] = new Neighbor[MAX_NEIGHBORS];
	LeastSquares ls = new LeastSquares();
	int numNeighbors = 0;	
	
	LeastSquares myls = new LeastSquares();		
	RegressionEntry table[] = new RegressionEntry[MAX_ENTRIES]; 
	int tableEntries = 0;	
    int numEntries;
	
	Timer timer0;
	
	int ROOT_ID;
	int sequence;
	
    RadioPacket processedMsg = null;
    FtspMessage outgoingMsg = new FtspMessage();

	public FtspNode(int id, Position position) {
		super(id,position);
		
		CLOCK = new ConstantDriftClock();		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this,MAC);
		
		timer0 = new Timer(CLOCK,this);		
		ROOT_ID = NODE_ID;
		sequence = 0;
		
		for (int i = 0; i < table.length; i++) {
			table[i] = new RegressionEntry();
		}
		
		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new Neighbor();
		}
		
		outgoingMsg.rootid = 0xFFFF;
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

	private void addNeighborEntry(FtspMessage msg, UInt32 eventTime) {

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
			neighbors[index].rate = msg.rate;
			neighbors[index].logicalClock = new UInt32(msg.rootClock);
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
        globalTime = new UInt32(localTime);
        globalTime = ls.calculateY(globalTime);

        // we need to periodically update the reference point for the root
        // to avoid wrapping the 32-bit (localTime - localAverage) value
        if( outgoingMsg.rootid == NODE_ID ) {
            if( (localTime.subtract(ls.getMeanX())).toLong() >= 0x20000000 )
            {
            		ls.setMeanX(new UInt32(localTime));
                    ls.setMeanY(globalTime.toInteger() - localTime.toInteger());
            }
        }

        outgoingMsg.clock = new UInt32(localTime);
        outgoingMsg.nodeid = NODE_ID;
        
        outgoingMsg.rate = ls.getSlope();  
        outgoingMsg.rootClock = new UInt32(globalTime);  
        
        // we don't send time sync msg, if we don't have enough data
        if( numEntries < ENTRY_SEND_LIMIT && outgoingMsg.rootid != NODE_ID ){
        	
        }
        else{
        	RadioPacket packet = new RadioPacket(new FtspMessage(outgoingMsg));
        	packet.setSender(this);
        	packet.setEventTime(new UInt32(localTime));
            MAC.sendPacket(packet);
            
            if( outgoingMsg.rootid == NODE_ID )
                ++outgoingMsg.sequence;
        }        
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE);
	}	
	
	private int numErrors=0;    
    void addNewEntry(FtspMessage msg,UInt32 localTime)
    {
        int i, freeItem = -1, oldestItem = 0;
        UInt32 age, oldestTime = new UInt32();
        int  timeError;

        // clear table if the received entry's been inconsistent for some time
        timeError = local2Global(localTime).toInteger() - msg.clock.toInteger();
        
        if( is_synced() && (timeError > ENTRY_THROWOUT_LIMIT || timeError < -ENTRY_THROWOUT_LIMIT))
        {
            if (++numErrors > 3)
                clearTable();
            return; // don't incorporate a bad reading
        }
        
        tableEntries = 0; // don't reset table size unless you're recounting
        numErrors = 0;

        for(i = 0; i < MAX_ENTRIES; ++i) {  
        	age = new UInt32(localTime);
        	age = age.subtract(table[i].x);

            //logical time error compensation
            if( age.toLong() >= 0x7FFFFFFFL )
                table[i].free = true;

            if( table[i].free)
                freeItem = i;
            else
                ++tableEntries;

            if( age.compareTo(oldestTime) >= 0 ) {
                oldestTime = age;
                oldestItem = i;
            }
        }

        if( freeItem < 0 )
            freeItem = oldestItem;
        else
            ++tableEntries;

    	table[freeItem].free = false;
        table[freeItem].x  = new UInt32(localTime);
        table[freeItem].y = msg.clock.toInteger() -localTime.toInteger();	 
    
        /* calculate new least-squares line */
        myls.calculate(table, tableEntries);
        numEntries = tableEntries;
    }

	private void clearTable() {
        int i;
        
        for(i = 0; i < MAX_ENTRIES; ++i)
            table[i].free = true;

        numEntries = 0;
	}
	
    void processMsg()
    {
        FtspMessage msg = (FtspMessage)processedMsg.getPayload();

        if( msg.rootid < outgoingMsg.rootid ){
            outgoingMsg.rootid = msg.rootid;
            outgoingMsg.sequence = msg.sequence;
            clearTable();
        }
        else if( outgoingMsg.rootid == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0 ) {
            outgoingMsg.sequence = msg.sequence;
        }
        else{
        	return;
        }
        
        /* add neighbor entry */
        if(outgoingMsg.rootid == msg.rootid){
        	addNeighborEntry(msg, processedMsg.getEventTime());
        }
        
        /* add our entry */
        addNewEntry(msg,processedMsg.getEventTime());
    }

	private boolean is_synced() {
     if (numEntries>=ENTRY_VALID_LIMIT || outgoingMsg.rootid ==NODE_ID)
         return true;
       else
         return false;
	}
	
	public UInt32 local2Global() {
		UInt32 now = CLOCK.getValue();
		
		return myls.calculateY(now);
	}
	
	public UInt32 local2Global(UInt32 now) {
		
		return myls.calculateY(now);
	}
	
	public String toString(){
		String s = Simulator.getInstance().getSecond().toString(10);
		
		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " " + Float.floatToIntBits(myls.getSlope());
		
		return s;		
	}
}
