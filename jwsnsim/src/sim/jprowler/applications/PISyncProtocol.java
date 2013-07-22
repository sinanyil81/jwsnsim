package sim.jprowler.applications;

import java.lang.reflect.Constructor;

import sim.jprowler.Event;
import sim.jprowler.Protocol;
import sim.jprowler.GaussianRadioModel;
import sim.jprowler.Mica2Node;
import sim.jprowler.Node;
import sim.jprowler.RadioModel;
import sim.jprowler.RadioPacket;
import sim.jprowler.Simulator;
import sim.jprowler.clock.ConstantDriftClock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;


/**
 * This is a sample application, it shows a way of utilizing the Prowler 
 * simulator. This is a broadcast application, where a mote in the middle of the 
 * field broadcasts a message which is further broadcasted by all the recepients.
 * Please note that this is not the only way of writing applications, this is 
 * just an example.
 * 
 * @author Gabor Pap, Gyorgy Balogh, Miklos Maroti
 */
public class PISyncProtocol extends Protocol implements TimerHandler{

	/** This field is true if this mote rebroadcasted the message already. */
	boolean sent = false;
	
	Timer timer0 = null;
		
	public PISyncProtocol(int nodeId, double x, double y, double z, RadioModel radio){
		super (new Mica2Node(Simulator.getInstance(),radio,new ConstantDriftClock()));	
		getNode().setPosition( x, y ,z );
		getNode().setId( nodeId );
		Simulator.getInstance().register(getNode());
		timer0 = new Timer(getNode().getClock(), this);
		timer0.startPeriodic(30000000);
	}
	
	public void receiveMessage(RadioPacket message){
		System.out.println("Received "+ (String)message.getPayload());
	}
	
	
	public void sendMessageDone(){
	}	
	
	@Override
	public void fireEvent(Timer timer) {
		if(timer == timer0){
			System.out.println(sendMessage(new RadioPacket("ID:" + getNode().getId())));
		}
		
	}
	


}

