package application.appFtsp;

import application.regression.LeastSquares;
import application.regression.ModifiedLeastSquares;
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

public class FtspNodeAverage extends Node implements TimerHandler{
	
	private static final int MAX_ENTRIES           = 8;              	// number of entries in the table
	private static final int BEACON_RATE           = 30000000;  	 	// how often send the beacon msg (in seconds)
	private static final int ROOT_TIMEOUT          = 5;              	//time to declare itself the root if no msg was received (in sync periods)
	private static final int IGNORE_ROOT_MSG       = 4;              	// after becoming the root ignore other roots messages (in send period)
	private static final int ENTRY_VALID_LIMIT     = 4;              	// number of entries to become synchronized
	private static final int ENTRY_SEND_LIMIT      = 3;              	// number of entries to send sync messages
	private static final int ENTRY_THROWOUT_LIMIT  = 10000;				// if time sync error is bigger than this clear the table
	
	LeastSquares ls = new LeastSquares();
	LeastSquares currentls = new LeastSquares();
	
	RegressionEntry table[] = new RegressionEntry[MAX_ENTRIES]; 
	int tableEntries = 0;	
    int numEntries;

    /* average related variables */
	float slopeTable[] = new float[MAX_ENTRIES];
	int offsetTable[] = new int[MAX_ENTRIES];
	int lineIndex = 0;
	int numLines = 0;	
	
	Timer timer0;
		
	int ROOT_ID;
	int sequence;

    int heartBeats; // the number of sucessfully sent messages
                    // since adding a new entry with lower beacon id than ours
	
    RadioPacket processedMsg = null;
    FtspMessage outgoingMsg = new FtspMessage();

	public FtspNodeAverage(int id, Position position) {
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
		
		outgoingMsg.rootid = 0xFFFF;
	}
	
	@Override
	public void receiveMessage(RadioPacket packet) {		
		processedMsg = packet;
		processMsg();			
	}

	@Override
	public void fireEvent(Timer timer) {
        
		if( outgoingMsg.rootid == 0xFFFF && ++heartBeats >= ROOT_TIMEOUT ) {
            outgoingMsg.sequence = 0;
            outgoingMsg.rootid = NODE_ID;
        }

        if( outgoingMsg.rootid != 0xFFFF ) {
           sendMsg();
        }
	}

	private void sendMsg() {
        UInt32 localTime, globalTime;

        localTime = CLOCK.getValue();
        globalTime = new UInt32(localTime);
        globalTime = ls.calculateY(globalTime);

        // we need to periodically update the reference point for the root
        // to avoid wrapping the 32-bit (localTime - localAverage) value
        if( outgoingMsg.rootid == NODE_ID ) {
//            if( (localTime.subtract(ls.getMeanX())).getValue() >= 0x20000000 )
//            {
//            		ls.setMeanX(new UInt32(localTime));
//                    ls.setMeanY(globalTime.toInteger() - localTime.toInteger());
//            }
        }
        else if( heartBeats >= ROOT_TIMEOUT ) {
            heartBeats = 0; //to allow ROOT_SWITCH_IGNORE to work
            outgoingMsg.rootid = NODE_ID;
            outgoingMsg.sequence++; // maybe set it to zero?
        }

        outgoingMsg.clock = new UInt32(globalTime);
        outgoingMsg.nodeid = NODE_ID;
        
        // we don't send time sync msg, if we don't have enough data
        if( numEntries < ENTRY_SEND_LIMIT && outgoingMsg.rootid != NODE_ID ){
            ++heartBeats;
        }
        else{
        	RadioPacket packet = new RadioPacket(new FtspMessage(outgoingMsg));
        	packet.setSender(this);
        	packet.setEventTime(new UInt32(localTime));
            MAC.sendPacket(packet);
            
            if( outgoingMsg.rootid == NODE_ID )
                ++outgoingMsg.sequence;
            
            ++heartBeats;
        }        
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_RATE);
	}	
	
	void adjustLine(){
        if(is_synced()){
        	
        	slopeTable[lineIndex] = ls.getSlope();
        	offsetTable[lineIndex] = ls.getOffset();
//        	offsetTable[lineIndex] = ls.getMeanY();
        	lineIndex = (lineIndex + 1) % MAX_ENTRIES;
        	if (numLines<MAX_ENTRIES)
        		numLines++;
        	
        	/* calculate slope average */
        	float slopeAvg = 0.0f;
        	
        	for(int i= 0; i < numLines; i++){
        		slopeAvg += slopeTable[i]/(float)numLines;
        	}
        	
        	int offsetAvg = 0;
        	int offsetAvgRemainder = 0;
        	
        	for(int i= 0; i < numLines; i++){
        		offsetAvg += offsetTable[i]/numLines;
        		offsetAvgRemainder += (offsetTable[i] % numLines);
        	}
        	
        	offsetAvg += offsetAvgRemainder/numLines;
        	        	
        	int diff = offsetAvg - ls.getOffset();
//        	int diff = offsetAvg - ls.getMeanY();
        	
        	if(diff > 1000 || diff < -1000){
        		diff--;
        	}      
        	
//        	diff = (int) ((float)diff/slopeAvg);
//        	currentls.setMeanX(ls.getMeanX().subtract(diff));
////        	currentls.setMeanY(ls.getMeanY()+diff);
        	currentls.setMeanX(ls.getMeanX());
        	currentls.setMeanY(ls.getMeanY());
        	currentls.setSlope(slopeAvg);
        }
        else{
        	currentls.setMeanX(ls.getMeanX());
        	currentls.setMeanY(ls.getMeanY());
        	currentls.setSlope(ls.getSlope());
        }
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
            if( age.getValue() >= 0x7FFFFFFFL )
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
        ls.calculate(table, tableEntries);                
        adjustLine();

        numEntries = tableEntries;
    }

	private void clearTable() {
        int i;
        
        for(i = 0; i < MAX_ENTRIES; ++i)
            table[i].free = true;

        numEntries = 0;
        
    	lineIndex = 0;
    	numLines = 0;    	   
	}
	
    void processMsg()
    {
        FtspMessage msg = (FtspMessage)processedMsg.getPayload();

        if( msg.rootid < outgoingMsg.rootid &&
            //after becoming the root, a node ignores messages that advertise the old root (it may take
            //some time for all nodes to timeout and discard the old root) 
            !(heartBeats < IGNORE_ROOT_MSG && outgoingMsg.rootid == NODE_ID)){
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

        if( outgoingMsg.rootid  < NODE_ID )
            heartBeats = 0;
        
        addNewEntry(msg,processedMsg.getEventTime());
    }

	private boolean is_synced() {
     if (numEntries>=ENTRY_VALID_LIMIT || outgoingMsg.rootid ==NODE_ID)
         return true;
       else
         return false;
	}
	
	public UInt32 local2Global() {
		UInt32 local = CLOCK.getValue();
		UInt32 time = ls.calculateY(local);
		
		return time;
	}
	
	public UInt32 local2Global(UInt32 now) {
		UInt32 time = ls.calculateY(now);
		
		return time;
	}
	
	public UInt32 myLocal2Global() {
		UInt32 local = CLOCK.getValue();
		UInt32 time = currentls.calculateY(local);
		
		return time;
	}
	
	public UInt32 myLocal2Global(UInt32 now) {
		UInt32 time = currentls.calculateY(now);
		
		return time;
	}
	
	public String toString(){
		String s = Simulator.getInstance().getSecond().toString(10);
		
		s += " " + NODE_ID;
		s += " " + myLocal2Global().toString();
		s += " " + Float.floatToIntBits(currentls.getSlope());
		
		return s;		
	}
}