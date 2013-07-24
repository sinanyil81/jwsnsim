package sim.jprowler.applications.LowPower;

import sim.jprowler.GaussianRadioModel;
import sim.jprowler.Node;
import sim.jprowler.Simulator;
import sim.jprowler.applications.Topology;
import sim.jprowler.clock.ConstantDriftClock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class LowPowerMain implements TimerHandler{
	
	public final static int NUMNODES = 2;
	
	static LowPowerProtocol protocol[] = new LowPowerProtocol[NUMNODES];
	static sim.jprowler.applications.Logger logger = null;
			
	public static void main(String[] args) throws Exception{			
		createNodes();		
//		createRandomNodes(300, 5);
		startLogging("deneme");
        Simulator.getInstance().run(50000);       
	}

	private static void startLogging(String filename) {
		// create logger application
		Timer timer = new Timer((sim.jprowler.clock.Clock)new ConstantDriftClock(1.0),new LowPowerMain());
		timer.startPeriodic(20000000);
		logger = new sim.jprowler.applications.Logger(filename);
	}

	private static void createNodes() {
		GaussianRadioModel radioModel = new GaussianRadioModel(Simulator.getInstance());	
		
		System.out.println("creating nodes...");
		
		for(int i = 0; i<NUMNODES ;i++){
			Node node = new LowPowerMica2Node(Simulator.getInstance(),radioModel,new ConstantDriftClock());
			node.setPosition( Topology.getNextLinePosition());
			node.setId(i+1);
			Simulator.getInstance().register(node);				
			protocol[i] = new LowPowerProtocol(node);
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
