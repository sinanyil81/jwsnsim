package application.appTheoric;

import application.Application;
import application.Logger;
import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;

public class GradientApp extends Application implements TimerHandler{

	public static final int LINE = 0;
	public static final int RING = 1;
	
	private int PERIOD = 1024*32;
	public int NUMNODES = 20;
	GradientNode[] nodes = null;
	
	Clock clock = new ConstantDriftClock(1.0);
	Timer timer = new Timer(clock,this);
	Logger logger;

	public GradientApp(int numNodes,String logFile,int topology) throws Exception {
		logger = new Logger(logFile);		
		this.NUMNODES = numNodes;
			
		createTopology(topology);
		
		clock.start();
		timer.startOneshot(PERIOD);
		
		run();
	}

	private void createTopology(int topology) {
		nodes = new GradientNode[NUMNODES];	
		
		if(topology == LINE){
		
			for(int i=0;i<NUMNODES;i++){
				nodes[i] = new GradientNode(i+1,i*10000);
			}			
			
			for(int i=1;i<NUMNODES;i++){
				nodes[i].addNeighbor(nodes[i-1]);
			}
			
			for(int i=0;i<NUMNODES-1;i++){
				nodes[i].addNeighbor(nodes[i+1]);
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
		timer.startOneshot(PERIOD);
	}

	private void log() {
		for(int i=0;i<nodes.length;i++){
			logger.log(nodes[i].toString());
		}
	}
}
