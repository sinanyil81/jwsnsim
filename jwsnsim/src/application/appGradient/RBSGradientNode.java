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

public class RBSGradientNode extends Node implements TimerHandler{

	private static final int MAX_ERROR = 1000;
	private static final int MAX_ENTRIES = 8;
	private static final int ENTRY_SEND_LIMIT = 3;

	
	final int STATUS_IDLE = 0;
	final int STATUS_SENT = 1;
	final int STATUS_REPLY = 2;
	final int STATUS_RECEIVED = 4;
	
	final int BEACON_PERIOD = 30000000;
	
	Timer timer0;
	
	LeastSquares regression = new LeastSquares();
	RegressionEntry table[] = new RegressionEntry[MAX_ENTRIES]; 
	int tableEntries = 0;	
    int numEntries;
    
	int status = STATUS_IDLE;
	int ROOT_ID;
	
    RadioPacket processedMsg = null;
    RBSGradientMessage outgoingMsg = new RBSGradientMessage(NODE_ID);
    
	RBSGradientMessage beacon;
	int sequence = 0;
	
	UInt32 beaconLocal = new UInt32();
	UInt32 beaconGlobal = new UInt32();
	
	long average = 0;
	int numReply;
	
	public RBSGradientNode(int id, Position position) {
		super(id,position);
		
		CLOCK = new ConstantDriftClock();		
		MAC = new MicaMac(this);
		RADIO = new SimpleRadio(this,MAC);
		
		timer0 = new Timer(CLOCK,this);		
		
		beacon = new RBSGradientMessage(id);		
		ROOT_ID = NODE_ID;
		
		for (int i = 0; i < table.length; i++) {
			table[i] = new RegressionEntry();
		}
		clearTable();
	}
	
    void addNewEntry(RBSGradientMessage msg,UInt32 localTime)
    {
        int i, freeItem = -1, oldestItem = 0;
        UInt32 age, oldestTime = new UInt32();

        tableEntries = 0; // don't reset table size unless you're recounting
        
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
        
        regression.calculate(table, tableEntries);
        numEntries = tableEntries;
    }

	private void clearTable() {
        int i;
        
        for(i = 0; i < MAX_ENTRIES; ++i)
            table[i].free = true;

        numEntries = 0;
	}

	private void update(){    
		/* Check if we received any BEACON */
		if((status & STATUS_RECEIVED) == STATUS_RECEIVED){
			/* Check if there are neighbors which has sent REPLY to our BEACON */
		    if((status & STATUS_REPLY) == STATUS_REPLY){
		        average /= (double)(numReply+1);
		        RBSGradientMessage msg = new RBSGradientMessage(0);
		        msg.clock = new UInt32(average);
		        addNewEntry(msg, beaconLocal);
		    }
		}
	}
	
	private void newRound(){
		numReply = 0;
		beacon = new RBSGradientMessage(getID());
	}
	
	private void sendBeacon(){
			
		beaconLocal = CLOCK.getValue();
		
	   if(numEntries >= ENTRY_SEND_LIMIT){
		   beaconGlobal = regression.calculateY(beaconLocal);
		   average =  beaconGlobal.toLong();
	   }
	   else{
		   beaconGlobal = new UInt32(beaconLocal);
		   average = beaconGlobal.toLong();
	   }
	   
	   outgoingMsg.type = RBSGradientMessage.INFO;
	   outgoingMsg.nodeid = NODE_ID;
	   outgoingMsg.rootid = ROOT_ID;  
	   outgoingMsg.clock = beaconGlobal;
	   outgoingMsg.sequence = ++sequence;
	   
	   RadioPacket packet = new RadioPacket(new RBSGradientMessage(outgoingMsg));
       packet.setSender(this);
       packet.setEventTime(beaconLocal);
       MAC.sendPacket(packet);
	}	

	@Override
	public void fireEvent(Timer timer) {
		update();
		
		if( numEntries >= ENTRY_SEND_LIMIT || (ROOT_ID == getID())){
			sendBeacon();
			status = STATUS_SENT;
		}
		else{
			status = STATUS_IDLE;
		}
		    
		newRound();
	}

	public long getNetworkTime() {
		if(numEntries >= ENTRY_SEND_LIMIT){
			return (regression.calculateY(CLOCK.getValue())).toLong();
		}
		
		return 0;
	}

	@Override
	public void on() throws Exception {
		super.on();
		timer0.startPeriodic(BEACON_PERIOD);
	}	

	@Override
	public void receiveMessage(RadioPacket packet) {		
		processedMsg = packet;
		processMsg();			
	}
	
	public void processMsg() {
		RBSGradientMessage msg = (RBSGradientMessage)processedMsg.getPayload();
		
		if(msg.rootid > ROOT_ID){
			return;
		}
		else if(msg.rootid < ROOT_ID){
			clearTable();
			ROOT_ID = msg.rootid;
			status = STATUS_IDLE;
		}
		
		if(msg.type == RBSGradientMessage.INFO){
			if(numEntries < ENTRY_SEND_LIMIT){
				addNewEntry(msg, processedMsg.getEventTime());
			}
			else {
				outgoingMsg.type = RBSGradientMessage.REPLY;
				outgoingMsg.rootid = ROOT_ID;
				outgoingMsg.nodeid = msg.nodeid;
				outgoingMsg.sequence = msg.sequence;
				outgoingMsg.clock = regression.calculateY(processedMsg.getEventTime());
				
				RadioPacket packet = new RadioPacket(new RBSGradientMessage(outgoingMsg));
	        	packet.setSender(this);
	        	packet.setEventTime(CLOCK.getValue());
	            MAC.sendPacket(packet);
			}
		}
		else if(msg.type == RBSGradientMessage.REPLY){
			if(msg.nodeid == NODE_ID){
				if(msg.sequence == sequence){
					int error = (int)Math.abs(beaconGlobal.subtract(msg.clock).toInteger());                    
					if(error < MAX_ERROR){
						average  += msg.clock.toLong();
						numReply++;
				        status |= STATUS_REPLY;
					}				
				}
			}
		}
			
		status |= STATUS_RECEIVED;		
	}
	
	public UInt32 local2Global() {
		return regression.calculateY(CLOCK.getValue());
	}
	
	public String toString(){
		String s = Simulator.getInstance().getSecond().toString(10);
		
		s += " " + NODE_ID;
		s += " " + local2Global().toString();
		s += " 0";
		s += " " + Float.floatToIntBits(regression.getSlope());
		s += " 0";
		s += " 0";
		s += " 0";
		
		return s;		
	}
}
