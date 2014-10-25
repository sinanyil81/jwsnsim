package application.appFtsp;

import hardware.Register32;
import hardware.clock.Timer;
import hardware.clock.TimerHandler;
import hardware.transceiver.RadioPacket;
import hardware.transceiver.SimpleRadio;
import application.regression.LeastSquares;
import application.regression.RegressionEntry;
import sim.clock.ConstantDriftClock;
import sim.clock.DynamicDriftClock;
import sim.node.Node;
import sim.node.Position;
import sim.radio.MicaMac;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class FtspNode extends Node implements TimerHandler{
	
	private static final int MAX_ENTRIES           = 8;              	// number of entries in the table
	private static final int BEACON_RATE           = 30000000;  	 	// how often send the beacon msg (in seconds)
	private static final int ROOT_TIMEOUT          = 5;              	//time to declare itself the root if no msg was received (in sync periods)
	private static final int IGNORE_ROOT_MSG       = 4;              	// after becoming the root ignore other roots messages (in send period)
	private static final int ENTRY_VALID_LIMIT     = 4;              	// number of entries to become synchronized
	private static final int ENTRY_SEND_LIMIT      = 3;              	// number of entries to send sync messages
	private static final int ENTRY_THROWOUT_LIMIT  = Integer.MAX_VALUE;	// if time sync error is bigger than this clear the table
	
	LeastSquares ls = new LeastSquares();	
	
	RegressionEntry table[] = new RegressionEntry[MAX_ENTRIES]; 
	int tableEntries = 0;	
    int numEntries;
	
	Timer timer0;
	
	int ROOT_ID;
	int sequence;

    int heartBeats; // the number of sucessfully sent messages
                    // since adding a new entry with lower beacon id than ours
	
    RadioPacket processedMsg = null;
    FtspMessage outgoingMsg = new FtspMessage();

	public FtspNode(int id, Position position) {
		super(id,position);
		
		CLOCK = new DynamicDriftClock();		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this,MAC);
		
		timer0 = new Timer(CLOCK,this);		
		ROOT_ID = NODE_ID;
		sequence = 0;
				
		CLOCK.setValue(new Register32(Math.abs(Distribution.getRandom().nextInt())));
		
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
        Register32 localTime, globalTime;

        localTime = CLOCK.getValue();
        globalTime = new Register32(localTime);
        globalTime = ls.calculateY(globalTime);

        // we need to periodically update the reference point for the root
        // to avoid wrapping the 32-bit (localTime - localAverage) value
        if( outgoingMsg.rootid == NODE_ID ) {
            if( (localTime.subtract(ls.getMeanX())).toLong() >= 0x20000000 )
            {
            		ls.setMeanX(new Register32(localTime));
                    ls.setMeanY(globalTime.toInteger() - localTime.toInteger());
            }
        }
        else if( heartBeats >= ROOT_TIMEOUT ) {
            heartBeats = 0; //to allow ROOT_SWITCH_IGNORE to work
            outgoingMsg.rootid = NODE_ID;
            outgoingMsg.sequence++; // maybe set it to zero?
        }

        outgoingMsg.clock = new Register32(globalTime);
        outgoingMsg.nodeid = NODE_ID;
        
        // we don't send time sync msg, if we don't have enough data
        if( numEntries < ENTRY_SEND_LIMIT && outgoingMsg.rootid != NODE_ID ){
            ++heartBeats;
        }
        else{
        	RadioPacket packet = new RadioPacket(new FtspMessage(outgoingMsg));
        	packet.setSender(this);
        	packet.setEventTime(new Register32(localTime));
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
    void addNewEntry(FtspMessage msg,Register32 localTime)
    {
        int i, freeItem = -1, oldestItem = 0;
        Register32 age, oldestTime = new Register32();
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
        	age = new Register32(localTime);
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
        table[freeItem].x  = new Register32(localTime);
        table[freeItem].y = msg.clock.toInteger() -localTime.toInteger();	 
    
        /* calculate new least-squares line */
        ls.calculate(table, tableEntries);
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
	
	public Register32 local2Global() {
		Register32 now = CLOCK.getValue();
		
		return ls.calculateY(now);
	}
	
	public Register32 local2Global(Register32 now) {
		
		return ls.calculateY(now);
	}
	
	public String toString(){
		String s = "" + Simulator.getInstance().getSecond();
		
		s += " " + NODE_ID;
		s += " " + local2Global().toString();
//		s += " " + Float.floatToIntBits((float) ((1.0f + ls.getSlope())*(this.CLOCK.getDrift()+1.0f)));
		s += " " + Float.floatToIntBits((float) ls.getSlope());
		
//		if (this.NODE_ID == 20){
//			s += "\n";
//			s += ls.getSlopeWeights(table, tableEntries);
//		}
		return s;		
	}
}
