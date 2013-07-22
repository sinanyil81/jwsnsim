package sim.jprowler.applications;

import sim.jprowler.GaussianRadioModel;
import sim.jprowler.Protocol;
import sim.jprowler.Simulator;
import sim.jprowler.applications.PISync.PIProtocol;
import sim.jprowler.clock.ConstantDriftClock;
import sim.jprowler.clock.Timer;
import sim.jprowler.clock.TimerHandler;

public class SimulatorMain implements TimerHandler{
	
	public final static int NUMNODES = 2;
	
	static Protocol nodes[] = new Protocol[NUMNODES];
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
		PIProtocol node1 = new PIProtocol(1,5,5,0,radioModel);
		PIProtocol node2 = new PIProtocol(2,10,10,0,radioModel);
		
		nodes[0] = node1;
		nodes[1] = node2;
		
		radioModel.updateNeighborhoods();
	}

	@Override
	public void fireEvent(Timer timer) {
		log();		
	}
	
	private void log() {
		for(int i=0;i<nodes.length;i++){
			logger.log(nodes[i].toString());
		}
	}
}
