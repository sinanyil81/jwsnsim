package sim.topology;

import sim.node.Position;

public class FullyConnected2D extends Topology{

	@Override
	public Position getNextPosition() {
		return new Position(0, 0, 0);
	}

}
