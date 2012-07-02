package application.appPulseSync;

import application.Application;
import application.Logger;
import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.simulator.Simulator;


public class PulseSyncApp extends Application implements TimerHandler{

	public static final int LINE = 0;
	public static final int RING = 1;
	
	private int PERIOD = 20000000;
	int NUMNODES = 20;
	Node[] nodes = null;
	
	Clock clock = new ConstantDriftClock(1.0);
	Timer timer = new Timer(clock,this);
	Logger logger;

	public PulseSyncApp(int numNodes,String logFile,int topology) throws Exception {
		logger = new Logger(logFile);		
		this.NUMNODES = numNodes;
			
		createTopology(topology);
			
		for(int i=0;i<NUMNODES;i++){
			nodes[i].on();
		}
		
		clock.start();
		timer.startOneshot(PERIOD);
		
		run();
	}

	private void createTopology(int topology) {
//		nodes = new PulseSyncNode[NUMNODES];
		nodes = new PulseSyncNodeMinimumVariance[NUMNODES];	
		
		if(topology == LINE){
			for(int i=0;i<NUMNODES;i++){
//				nodes[i] = new PulseSyncNode(i+1,new Position(i*5,i*5,0));
				nodes[i] = new PulseSyncNodeMinimumVariance(i+1,new Position(i*5,i*5,0));
			}			
		}
	}

	@Override
	public void exit() {
		logger.close();		
	}

	@Override
	public void fireEvent(Timer timer) {
		log();
		timer.startOneshot((int) (PERIOD + ((Simulator.random.nextInt() % 4) + 1)*1000000));
	}

	private void log() {
		for(int i=0;i<nodes.length;i++){
			logger.log(nodes[i].toString());
		}
	}
}
