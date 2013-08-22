package sim.jprowler.applications.PISync;

import java.util.Iterator;
import java.util.Vector;

import sim.jprowler.Node;
import sim.jprowler.Simulator;
import sim.jprowler.applications.Topology;
import sim.jprowler.clock.ConstantDriftClock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;
import sim.jprowler.mac.CSMAMac;

public class PIMain implements TimerHandler{
	
	public final static int NUMNODES = 20;
	
	static PIProtocol protocol[] = new PIProtocol[NUMNODES];
	static sim.jprowler.applications.Logger logger = null;
			
	public static void main(String[] args) throws Exception{			
		createNodes();		
//		createRandomNodes(300, 5);
		startLogging("deneme");
        Simulator.getInstance().run(100000);       
	}

	private static void startLogging(String filename) {
		// create logger application
		Timer timer = new Timer((sim.jprowler.clock.Clock)new ConstantDriftClock(1.0),new PIMain());
		timer.startPeriodic(20000000);
		logger = new sim.jprowler.applications.Logger(filename);
	}

//	private static void createNodes() {
//		GaussianRadioModel radioModel = new GaussianRadioModel(Simulator.getInstance());	
//		
//		System.out.println("creating nodes...");
//		
//		for(int i = 0; i<NUMNODES ;i++){
//			Node node = new Mica2Node(radioModel,new ConstantDriftClock());
//			node.setPosition( Topology.getNextLinePosition());
////			node.setPosition( Topology.getNextRingPosition(NUMNODES));
////			node.setPosition( Topology.getNextDensePosition(5));
//			System.out.println(node.getPosition());
//			node.setId(i+1);
//			Simulator.getInstance().register(node);				
//			protocol[i] = new PIProtocol(node);
//		}
//				
//		radioModel.updateNeighborhoods();
//	}
	
//	public static void createRandomNodes(double areaWidth, double maxElevation) { 
//		GaussianRadioModel radioModel = new GaussianRadioModel(Simulator.getInstance());	
//		
//		System.out.println("creating nodes...");
//		
//		for( int i=0; i< NUMNODES; ++i ){
//			Node node = new Mica2Node(radioModel,new ConstantDriftClock());
//			node.setPosition( new Position(areaWidth * Simulator.random.nextDouble(), 
//										   areaWidth * Simulator.random.nextDouble(), 
//										   maxElevation * Simulator.random.nextDouble()));
//			node.setId( 1 + i);
//			Simulator.getInstance().register(node);				
//			protocol[i] = new PIProtocol(node);           
//		}
//	}
	
	private static void createNodes() {		
		System.out.println("creating nodes...");
		
		for(int i = 0; i<NUMNODES ;i++){
			Node node = new Node(i+1,new ConstantDriftClock());
			node.setPosition( Topology.getNextLinePosition());
			CSMAMac mac = new CSMAMac(node);
						
//			node.setPosition( Topology.getNextRingPosition(NUMNODES));
//			node.setPosition( Topology.getNextDensePosition(5));
			System.out.println(node.getPosition());
			
			Simulator.getInstance().register(node);				
			protocol[i] = new PIProtocol(node,mac);
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
