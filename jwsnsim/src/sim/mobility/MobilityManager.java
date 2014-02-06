package sim.mobility;

import java.lang.reflect.Constructor;

import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.gui.GUI;
import sim.node.NodeFactory;
import sim.node.Position;

public class MobilityManager implements TimerHandler {
	protected Clock clock = new ConstantDriftClock(1.0);
	protected Timer timer = new Timer(clock,this);
	protected MobilityModel[] models = null;
	
	public MobilityManager(String mobilityClassName) {
		clock.start();
		timer.startPeriodic(10000000);	
		
		models = new MobilityModel[NodeFactory.numNodes];
		for (int i = 0; i < NodeFactory.numNodes; i++) {
			models[i] = createModel(mobilityClassName);
		}
	}
	
	@Override
	public void fireEvent(Timer timer) {
		for (int i = 0; i < NodeFactory.numNodes; i++) {
			Position pos = models[i].getNextPos(NodeFactory.nodes[i]);
			NodeFactory.nodes[i].setPosition(pos);
		}
		
		for (int i = 0; i < NodeFactory.numNodes; i++) {
			NodeFactory.nodes[i].getRadio().updateNeighborhood();
		}
		
		GUI.refresh();
	}
	
	static MobilityModel createModel(String className){
		Class<?> c;
		Object object = null;
		try {
			c = Class.forName(className);
			Constructor<?> cons = c.getConstructor();
			object = cons.newInstance(new Object[] {});
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		return (MobilityModel)object;
	}

}
