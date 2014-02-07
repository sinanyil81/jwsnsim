package application;

import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.node.NodeFactory;
import sim.simulator.Simulation;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class SynchronizationSimulation extends Simulation implements TimerHandler {
	
	private int PERIOD = 20000000;
	protected long MAXSECOND =20000;	
	protected Clock clock = new ConstantDriftClock(1.0);
	protected Timer timer = new Timer(clock,this);
	protected Logger logger;
	
	public SynchronizationSimulation(String logFile, int durationTime){
		super(durationTime);
		
		logger = new Logger(logFile);
		clock.start();
		timer.startOneshot(PERIOD);	
		
		for(int i=0;i<NodeFactory.numNodes;i++){
			try {
				NodeFactory.nodes[i].on();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Simulator.getInstance().startSimulation(this);
	}
	
	@Override
	public void exit() {
		logger.close();	
		System.out.println("Simulation finished!");
	}

	@Override
	public void fireEvent(Timer timer) {
		log();
		timer.startOneshot((int) (PERIOD + ((Distribution.getRandom().nextInt() % 4) + 1)*1000000));
	}

	private void log() {
		for(int i=0;i<NodeFactory.nodes.length;i++){
			logger.log(NodeFactory.nodes[i].toString());
		}
	}
}