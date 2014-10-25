package sim.mobility;

import hardware.clock.Counter32;
import hardware.clock.Timer;

import java.lang.reflect.Constructor;

import sim.clock.ConstantDriftClock;
import sim.clock.TimerHandler;
import sim.node.NodeFactory;
import sim.node.Position;

public class MobilityManager implements TimerHandler {
	protected Counter32 clock = new ConstantDriftClock(1.0);
	protected Timer timer = new Timer(clock,this);
	protected MobilityModel[] models = null;
	
	public MobilityManager(String mobilityClassName) {
		clock.start();
		timer.startPeriodic(1000000);	
		
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
