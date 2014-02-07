package application;

import sim.node.NodeFactory;
import sim.simulator.Event;
import sim.simulator.EventObserver;
import sim.simulator.Simulation;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class SynchronizationSimulation extends Simulation implements EventObserver {
	
	private int PERIOD = 20000000;
	protected Logger logger;
	Event event = new Event(this);
	
	public SynchronizationSimulation(String logFile, int durationTime){
		super(durationTime);
		
		logger = new Logger(logFile);
		
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

	private void log() {
		for(int i=0;i<NodeFactory.nodes.length;i++){
			logger.log(NodeFactory.nodes[i].toString());
		}
	}

	@Override
	public void signal(Event event) {
		log();
		event.register((int) (PERIOD + ((Distribution.getRandom().nextInt() % 4) + 1)*1000000));
		
	}
}