package sim.mobility;

import sim.node.Node;
import sim.node.Position;

public abstract class MobilityModel {
		public abstract Position getNextPos(Node n); 
}
