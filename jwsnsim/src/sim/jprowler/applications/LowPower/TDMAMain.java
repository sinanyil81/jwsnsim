package sim.jprowler.applications.LowPower;

import java.util.Iterator;
import java.util.Vector;

import sim.jprowler.GaussianRadioModel;
import sim.jprowler.Mica2Node;
import sim.jprowler.Mica2NodeNonCSMA;
import sim.jprowler.Node;
import sim.jprowler.Position;
import sim.jprowler.Simulator;
import sim.jprowler.applications.Logger;
import sim.jprowler.applications.Topology;
import sim.jprowler.clock.ConstantDriftClock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class TDMAMain implements TimerHandler{
	
	public final static int NUMNODES = 16;
	
	static TDMAProtocol protocol[] = new TDMAProtocol[NUMNODES];
	static sim.jprowler.applications.Logger logger = null;
			
	public static void main(String[] args) throws Exception{			
		createNodes();		
		System.out.println("Nodes created");
		startLogging("deneme");
        Simulator.getInstance().run(50000);  
        killNodes();
	}

	private static void killNodes() {
		Vector<Node> nodes = Simulator.getInstance().getNodes();
		
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			TDMANode node = (TDMANode) iterator.next();
			node.destroy();
		}		
	}

	private static void startLogging(String filename) {
		// create logger application
		Timer timer = new Timer((sim.jprowler.clock.Clock)new ConstantDriftClock(1.0),new TDMAMain());
		timer.startPeriodic(20000000);
		logger = new sim.jprowler.applications.Logger(filename);
	}

	private static void createNodes() {
		GaussianRadioModel radioModel = new GaussianRadioModel(Simulator.getInstance());	
		
		System.out.println("creating nodes...");
		
		for(int i = 0; i<NUMNODES ;i++){
//			Node node = new Mica2Node(radioModel,new ConstantDriftClock());
			Node node = new TDMANode(i+1,radioModel,new ConstantDriftClock());
			node.setPosition( Topology.getNext4x4GridPosition());
			System.out.println(node.getPosition());
			((TDMANode)node).start();
			Simulator.getInstance().register(node);				
			protocol[i] = new TDMAProtocol(node);
		}
				
		radioModel.updateNeighborhoods();		
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
