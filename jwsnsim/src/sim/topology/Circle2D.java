package sim.topology;

import nodes.NodeFactory;
import nodes.Position;
import sim.configuration.TransmissionConfiguration;

public class Circle2D extends Topology{
	int counter = 1;
	
	public void initialize() {
		counter = 1;
	}
	
	public Position getNextPosition() {
		double oneStep = 360.0 / NodeFactory.numNodes;		
		double radius = (TransmissionConfiguration.MAX_RANGE-2)*360.0/(oneStep*2.0*Math.PI);

		
		Position position = new Position(radius+radius * Math.cos(Math.toRadians(counter * oneStep)),
				radius+radius * Math.sin(Math.toRadians(counter * oneStep)),0);
		counter++;
		return position;
		
	}
}
