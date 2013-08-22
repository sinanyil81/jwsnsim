package sim.jprowler.applications.LowPower;

import java.util.Iterator;
import java.util.Vector;
import sim.jprowler.Node;
import sim.jprowler.Position;
import sim.jprowler.Simulator;
import sim.jprowler.applications.Logger;
import sim.jprowler.applications.Topology;
import sim.jprowler.clock.ConstantDriftClock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;
import sim.jprowler.mac.NonCSMAMac;

public class TDMAMain implements TimerHandler{
	
	public final static int NUMNODES = 16;
	
	static TDMANode protocol[] = new TDMANode[NUMNODES];
	static sim.jprowler.applications.Logger logger = null;
			
	public static void main(String[] args) throws Exception{			
		createNodes();		
		System.out.println("Nodes created");
		startLogging("deneme");
        Simulator.getInstance().run(20000); 
        
		for(int i = 0; i<NUMNODES ;i++){
			protocol[i].destroy();
		}	
	}

	private static void startLogging(String filename) {
		// create logger application
		Timer timer = new Timer((sim.jprowler.clock.Clock)new ConstantDriftClock(1.0),new TDMAMain());
		timer.startPeriodic(20000000);
		logger = new sim.jprowler.applications.Logger(filename);
	}

	private static void createNodes() {		
		
		System.out.println("creating nodes...");
		
		for(int i = 0; i<NUMNODES ;i++){
//			Node node = new Mica2Node(radioModel,new ConstantDriftClock());
			Node node = new Node(i+1,new ConstantDriftClock());
			node.setPosition( Topology.getNext4x4GridPosition());
			System.out.println(node.getPosition());
			Simulator.getInstance().register(node);				
			
			NonCSMAMac mac = new NonCSMAMac(node);
			protocol[i] = new TDMANode(node,mac);
		}
				
		Vector<Node> nodes = Simulator.getInstance().getNodes();
		
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			node.getRadio().getNeighbors().updateNeighborhood();			
		}	
	}
	
	@Override
	public void fireEvent(Timer timer) {
		log();		
	}
	
	private void log() {
		for(int i=0;i<protocol.length;i++){
			logger.log(protocol[i].toString());
		}
	}
}
