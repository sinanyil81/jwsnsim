package sim.mobility;

import nodes.Node;
import nodes.Position;

public abstract class MobilityModel {
		public abstract Position getNextPos(Node n); 
}
