package application.appPulseSync;

import application.Application;
import application.Logger;
import application.appFtsp.FtspNode;
import application.appFtsp.FtspNodeMinimumVariance;
import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.radio.SimpleRadio;
import sim.simulator.Simulator;


public class PulseSyncApp extends Application implements TimerHandler{

	public static final int LINE = 0;
	public static final int RING = 1;
	public static final int GRID = 2;
	public static final int DENSE = 3;
	
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
	
	public PulseSyncApp(int numNodes,String logFile,int topology,double density) throws Exception {
		logger = new Logger(logFile);		
		this.NUMNODES = numNodes;
			
		createTopology(topology,density);
			
		for(int i=0;i<NUMNODES;i++){
			nodes[i].on();
		}
		
		clock.start();
		timer.startOneshot(PERIOD);
		
		run();
	}
	
	private void createTopology(int topology,double density) {
		nodes = new PulseSyncNode[NUMNODES];	
//		nodes = new PulseSyncNodeMinimumVariance[NUMNODES];
		
		if(topology == DENSE){
			double stepsize = 2.0*(double)SimpleRadio.MAX_DISTANCE/(double)density;
			for(int i = 0; i< NUMNODES;i++){
				nodes[i] = new PulseSyncNode(i+1,new Position(i*stepsize,0,0));
//				nodes[i] = new PulseSyncNodeMinimumVariance(i+1,new Position(i*density,i*density,0));
			}			
		}
	}

	private void createTopology(int topology) {
		nodes = new PulseSyncNode[NUMNODES];
//		nodes = new PulseSyncNodeMinimumVariance[NUMNODES];
		
		if(topology == LINE){
			for(int i=0;i<NUMNODES;i++){
				nodes[i] = new PulseSyncNode(i+1,new Position(i*5,i*5,0));
//				nodes[i] = new PulseSyncNodeMinimumVariance(i+1,new Position(i*5,i*5,0));
			}			
		}
		else if(topology == RING){
			
			double oneStep = 360.0 / NUMNODES;
			double radius = SimpleRadio.MAX_DISTANCE/Math.toRadians(oneStep); 
					
			for(int i = 0; i< NUMNODES;i++){
				Position pos = new Position(radius * Math.cos(Math.toRadians(i * oneStep)),
											radius * Math.sin(Math.toRadians(i * oneStep)),0);	
				nodes[i] = new PulseSyncNode(i+1,pos);
//				nodes[i] = new PulseSyncNodeMinimumVariance(i+1,pos);
			}			
		}
		else if(topology == GRID){
			
			int j = (int) Math.sqrt(NUMNODES);
			int id = 0;
			
			for(int i = 0;i<j;i++){
				for(int k = 0;k<j;k++){
					nodes[id] = new PulseSyncNode(id+1,new Position(k*10,i*10,0));
//					nodes[id] = new PulseSyncNodeMinimumVariance(id+1,new Position(k*10,i*10,0));
//					id++;
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
