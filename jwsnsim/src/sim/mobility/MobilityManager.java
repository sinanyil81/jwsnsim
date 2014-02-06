package sim.mobility;

import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.configuration.NodeConfiguration;

public class MobilityManager implements TimerHandler {
	protected Clock clock = new ConstantDriftClock(1.0);
	protected Timer timer = new Timer(clock,this);
	protected MobilityModel model;
	
	public MobilityManager(MobilityModel model) {
		clock.start();
		timer.startOneshot(1000000);	
		
		this.model = model;
	}
	
	@Override
	public void fireEvent(Timer timer) {
		for (int i = 0; i < NodeConfiguration.numNodes; i++) {
			model.getNextPos(NodeConfiguration.nodes[i]);
		}		
	}

}
