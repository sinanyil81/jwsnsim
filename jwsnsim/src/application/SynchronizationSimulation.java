package application;

import core.SimulationEvent;
import core.SimulationEventObserver;
import core.Simulation;
import core.Simulator;
import sim.node.NodeFactory;
import sim.statistics.Distribution;

public class SynchronizationSimulation extends Simulation implements SimulationEventObserver {
	
	private int PERIOD = 20000000;
	protected Logger logger;
	SimulationEvent event = new SimulationEvent(this);
	
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

		event.register((int) (PERIOD + ((Distribution.getRandom().nextInt() % 4) + 1)*1000000));
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
	public void signal(SimulationEvent event) {
		log();
		event.register((int) (PERIOD + ((Distribution.getRandom().nextInt() % 4) + 1)*1000000));
		
	}
}