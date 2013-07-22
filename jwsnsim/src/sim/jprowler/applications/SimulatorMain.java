package sim.jprowler.applications;

import sim.jprowler.GaussianRadioModel;
import sim.jprowler.Mica2Node;
import sim.jprowler.Node;
import sim.jprowler.Simulator;
import sim.jprowler.applications.PISync.PIProtocol;
import sim.jprowler.clock.ConstantDriftClock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class SimulatorMain implements TimerHandler{
	
	public final static int NUMNODES = 2;
	
	static PIProtocol protocol[] = new PIProtocol[NUMNODES];
	static sim.jprowler.applications.Logger logger = null;
			
	public static void main(String[] args) throws Exception{			
		createNodes();		
		startLogging("deneme");
        Simulator.getInstance().run(20000);       
	}

	private static void startLogging(String filename) {
		// create logger application
		Timer timer = new Timer((sim.jprowler.clock.Clock)new ConstantDriftClock(1.0),new SimulatorMain());
		timer.startPeriodic(20000000);
		logger = new sim.jprowler.applications.Logger(filename);
	}

	private static void createNodes() {
		GaussianRadioModel radioModel = new GaussianRadioModel(Simulator.getInstance());	
		
		System.out.println("creating nodes...");
		
		for(int i = 0; i<2;i++){
			Node node = new Mica2Node(Simulator.getInstance(),radioModel,new ConstantDriftClock());
			node.setPosition( Topology.getNextLinePosition());
			node.setId(i+1);
			Simulator.getInstance().register(node);				
			protocol[i] = new PIProtocol(node);
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
