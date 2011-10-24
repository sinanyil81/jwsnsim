package application.appFtsp;

import application.regression.MinimumVarianceSlopeRegression;
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

public class FtspNodeMinimumVariance extends Node implements TimerHandler{
	
	private static final int MAX_ENTRIES           = 8;              	// number of entries in the table
	private static final int BEACON_RATE           = 30000000;  	 	// how often send the beacon msg (in seconds)
	private static final int ROOT_TIMEOUT          = 5;              	//time to declare itself the root if no msg was received (in sync periods)
	private static final int IGNORE_ROOT_MSG       = 4;              	// after becoming the root ignore other roots messages (in send period)
	private static final int ENTRY_VALID_LIMIT     = 4;              	// number of entries to become synchronized
	private static final int ENTRY_SEND_LIMIT      = 3;              	// number of entries to send sync messages
	private static final int ENTRY_THROWOUT_LIMIT  = Integer.MAX_VALUE; // if time sync error is bigger than this clear the table
	
	MinimumVarianceSlopeRegression ls = new MinimumVarianceSlopeRegression();
	RegressionEntry table[] = new RegressionEntry[MAX_ENTRIES]; 
	
	int tableEntries = 0;	
    int numEntries;
    int tableEnd = -1;
	
	Timer timer0;
	
	int ROOT_ID;
	int sequence;

    int heartBeats; // the number of sucessfully sent messages
                    // since adding a new entry with lower beacon id than ours
	
    RadioPacket processedMsg = null;
    FtspMessage outgoingMsg = new FtspMessage();

	public FtspNodeMinimumVariance(int id, Position position) {
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
            if( (localTime.subtract(ls.getMeanX())).getValue() >= 0x20000000 )
            {
            		ls.setMeanX(new UInt32(localTime));
                    ls.setMeanY(globalTime.toInteger() - localTime.toInteger());
            }
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
	
	private int numErrors=0;    
    void addNewEntry(FtspMessage msg,UInt32 localTime)
    {
        int i;
        int  timeError;

        // clear table if the received entry's been inconsistent for some time
        timeError = ls.calculateY(localTime).subtract(msg.clock).toInteger();
        
        if( is_synced() && (Math.abs(timeError) > ENTRY_THROWOUT_LIMIT) )
        {
            if (++numErrors > 3);
//                clearTable();
//            return; // don't incorporate a bad reading
        }

        numErrors = 0;
        
        if (tableEntries == MAX_ENTRIES){
            /* shift left all the entries: we get ranked  x values */
            for(i=0; i < MAX_ENTRIES-1; i++){
              table[i] = new RegressionEntry(table[i+1]);
            }
          }
          else{
        	  tableEnd++;
        	  tableEntries++;          
          }
        
    	table[tableEnd].free = false;
        table[tableEnd].x  = new UInt32(localTime);
        table[tableEnd].y = msg.clock.subtract(localTime).toInteger();	  
        
        ls.calculate(table, tableEntries);
        numEntries = tableEntries;
    }

	private void clearTable() {
        int i;
        
        for(i = 0; i < MAX_ENTRIES; ++i)
            table[i].free = true;

        numEntries = 0;
        tableEnd = -1;
        tableEntries = 0;
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
		return ls.calculateY(CLOCK.getValue());
	}
	
	public String toString(){
		String s = Simulator.getInstance().getSecond().toString(10);
		
		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " " + Float.floatToIntBits(ls.getSlope());

		return s;		
	}
}
