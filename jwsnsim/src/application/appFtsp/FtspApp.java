package application.appFtsp;

import application.Application;
import application.Logger;
import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.simulator.Simulator;


public class FtspApp extends Application implements TimerHandler{

	public static final int LINE = 0;
	public static final int RING = 1;
	
	private int PERIOD = 20000000;
	int NUMNODES = 20;
	Node[] nodes = null;
	
	Clock clock = new ConstantDriftClock(1.0);
	Timer timer = new Timer(clock,this);
	Logger logger;

	public FtspApp(int numNodes,String logFile,int topology) throws Exception {
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
//		nodes = new FtspNode[NUMNODES];	
//		nodes = new FtspNodeWithoutDiscontinuity[NUMNODES];
//		nodes = new FtspNodeMinimumVariance[NUMNODES];
//		nodes = new FtspNodeAverage[NUMNODES];
		nodes = new FtspNodeLSAverage[NUMNODES];
//		nodes = new FtspNodeMedian[NUMNODES];
		
		if(topology == LINE){
			for(int i=0;i<NUMNODES;i++){
//				nodes[i] = new FtspNode(i+1,new Position(i*5,i*5,0));
//				nodes[i] = new FtspNodeWithoutDiscontinuity(i+1,new Position(i*5,i*5,0));
//				nodes[i] = new FtspNodeMinimumVariance(i+1,new Position(i*5,i*5,0));
//				nodes[i] = new FtspNodeAverage(i+1,new Position(i*5,i*5,0));
				nodes[i] = new FtspNodeLSAverage(i+1,new Position(i*5,i*5,0));
//				nodes[i] = new FtspNodeMedian(i+1,new Position(i*5,i*5,0));
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
