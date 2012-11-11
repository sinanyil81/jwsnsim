package application.appFcsa;

import application.Application;
import application.Logger;
import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.simulator.Simulator;


public class FloodingApp extends Application implements TimerHandler{

	public static final int LINE = 0;
	public static final int RING = 1;
	public static final int GRID = 2;
	
	private int PERIOD = 20000000;
	int NUMNODES = 20;
	Node[] nodes = null;
	
	Clock clock = new ConstantDriftClock(1.0);
	Timer timer = new Timer(clock,this);
	Logger logger;

	public FloodingApp(int numNodes,String logFile,int topology) throws Exception {
		logger = new Logger(logFile);		
		this.NUMNODES = numNodes;
			
//		createTopology(topology);
		createSelfTopology(topology);
			
		for(int i=0;i<NUMNODES;i++){
			nodes[i].on();
		}
		
		clock.start();
		timer.startOneshot(PERIOD);
		
		run();
	}

	private void createTopology(int topology) {
		nodes = new FloodingNode[NUMNODES];	
		
		if(topology == LINE){
			for(int i=0;i<NUMNODES;i++){
				nodes[i] = new FloodingNode(i+1,new Position(i*5,i*5,0));
			}			
		}
		else if(topology == GRID){
			
			int j = (int) Math.sqrt(NUMNODES);
			int id = 0;
			
			for(int i = 0;i<j;i++){
				for(int k = 0;k<j;k++){
					nodes[id] = new FloodingNode(id+1,new Position(k*10,i*10,0));
					id++;
				}				
			}
		}
	}
	
	private void createSelfTopology(int topology) {
		nodes = new SelfFloodingNode3[NUMNODES];	
		
		if(topology == LINE){
			for(int i=0;i<NUMNODES;i++){
				nodes[i] = new SelfFloodingNode3(i+1,new Position(i*5,i*5,0));
			}			
		}
		else if(topology == GRID){
			
			int j = (int) Math.sqrt(NUMNODES);
			int id = 0;
			
			for(int i = 0;i<j;i++){
				for(int k = 0;k<j;k++){
					nodes[id] = new SelfFloodingNode3(id+1,new Position(k*10,i*10,0));
					id++;
				}				
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
