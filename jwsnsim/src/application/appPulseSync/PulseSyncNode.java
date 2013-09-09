package application.appPulseSync;

import application.regression.LeastSquares;
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

public class PulseSyncNode extends Node implements TimerHandler{
	
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
	
	int ROOT_ID = 1; // fixed root
	int sequence;
	
    RadioPacket processedMsg = null;
    PulseSyncMessage outgoingMsg = new PulseSyncMessage();

    UInt32 pulse;
    UInt32 pulseTime;
    
	public PulseSyncNode(int id, Position position) {
		super(id,position);
		
		CLOCK = new ConstantDriftClock();		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this,MAC);
		
		timer0 = new Timer(CLOCK,this);		
		sequence = 0;
		
		for (int i = 0; i < table.length; i++) {
			table[i] = new RegressionEntry();
		}
		
		/* init variables */
		outgoingMsg.rootid = NODE_ID;
		outgoingMsg.sequence = 0;
		
		/* to start clock with a random value */
		CLOCK.setValue(new UInt32(Math.abs(Simulator.random.nextInt())));
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
        
        if(NODE_ID != ROOT_ID){
        	UInt32 elapsed = localTime.subtract(pulseTime);
        	elapsed = elapsed.add(elapsed.multiply(ls.getSlope()));               	        		
        	globalTime = pulse.add(elapsed);
        }
        else{
            globalTime = new UInt32(localTime);        	
        }
               
        outgoingMsg.rootid = ROOT_ID;
        outgoingMsg.nodeid = NODE_ID;
        
        if(NODE_ID == ROOT_ID)
        	outgoingMsg.sequence++; // maybe set it to zero?
        
        outgoingMsg.clock = new UInt32(globalTime);
                
        // we don't send time sync msg, if we don't have enough data
        if( numEntries >= ENTRY_SEND_LIMIT || ROOT_ID == NODE_ID){
        	RadioPacket packet = new RadioPacket(new PulseSyncMessage(outgoingMsg));
        	packet.setSender(this);
        	packet.setEventTime(new UInt32(localTime));
            MAC.sendPacket(packet);
        }        
	}

	@Override
	public void on() throws Exception {
		super.on();
		if(NODE_ID == ROOT_ID)
			timer0.startPeriodic(BEACON_RATE);
	}	
	
	private int numErrors=0;    
    void addNewEntry(PulseSyncMessage msg,UInt32 localTime)
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
        PulseSyncMessage msg = (PulseSyncMessage)processedMsg.getPayload();

        if( ROOT_ID == msg.rootid && (msg.sequence - outgoingMsg.sequence) > 0 ) {
            outgoingMsg.sequence = msg.sequence;
            
            pulse = new UInt32(msg.clock);
            pulseTime = processedMsg.getEventTime();
            
            /* for sending data */
            timer0.startOneshot(1000000);
        }
        else{
        	return;
        }
        
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
		
		return ls.calculateY(now);
	}
	
	public UInt32 local2Global(UInt32 now) {
		
		return ls.calculateY(now);
	}
	
	public String toString(){
		String s = "" + Simulator.getInstance().getSecond();
		
		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " " + Float.floatToIntBits(ls.getSlope());
		
		return s;		
	}
}
