package application;

import java.lang.reflect.Constructor;
import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.Node;
import sim.node.Position;
import sim.simulator.Simulator;

public class Application implements TimerHandler {
	public static final int LINE = 0;
	public static final int RING = 1;
	public static final int DENSE = 2;
	
	private int PERIOD = 20000000;
	
	public static long MAXSECOND =20000;
	protected int NUMNODES = 20;
	protected Node[] nodes = null;
	
	protected Clock clock = new ConstantDriftClock(1.0);
	protected Timer timer = new Timer(clock,this);
	protected Logger logger;
	
	public Application(String classToLoad, int numNodes,String logFile,int topology,int density){
		logger = new Logger(logFile);
		clock.start();
		timer.startOneshot(PERIOD);
		this.NUMNODES = numNodes;
		
		createTopology(classToLoad,topology,density);
			
		for(int i=0;i<NUMNODES;i++){
			try {
				nodes[i].on();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		run();
	}
	
	private void createTopology(String classToLoad,int topology,int density) {
		nodes = new Node[NUMNODES];	
		for(int i=0;i<NUMNODES;i++){
			Position position = null;
			switch (topology) {
			
			case LINE:
				position = Topology.getNextLinePosition();
				break;
			
			case RING:
				position = Topology.getNextRingPosition(NUMNODES);
				break;
				
			case DENSE:
				position = Topology.getNextDensePosition(density);
				break;

			default:
				break;
			}
			nodes[i] = createNode(classToLoad, i+1, position);
		}
	
			
	}

	
	Node createNode(String className,int id, Position position){
		Class<?> c;
		Object object = null;
		try {
			c = Class.forName(className);
			Constructor<?> cons = c.getConstructor(int.class,Position.class);
			object = cons.newInstance(new Object[] {id,position});
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		return (Node)object;
	}
	
	public void run(){
		
		while(Simulator.getInstance().getSecond() < MAXSECOND){
			Simulator.getInstance().tick();
		}
		
		Simulator.getInstance().reset();
		
		exit();
	}
	
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