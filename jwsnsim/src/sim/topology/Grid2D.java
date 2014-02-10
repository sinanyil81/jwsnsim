package sim.topology;

import sim.configuration.AreaConfiguration;
import sim.configuration.TransmissionConfiguration;
import sim.node.NodeFactory;
import sim.node.Position;

public class Grid2D extends Topology {
	private double size; // the cell-size of the gird
	private int numNodesPerLine; // number of nodes on the x-axis
	private int i,j; // loop counters
	
	/* (non-Javadoc)
	 * @see sinalgo.models.DistributionModel#initialize()
	 */
	public void initialize() {
		double a = 1 - NodeFactory.numNodes;
		double b = - (AreaConfiguration.dimX + AreaConfiguration.dimY); // kind of a hack
		double c =  AreaConfiguration.dimX * AreaConfiguration.dimY;
		double tmp = b * b - 4 * a * c;
		if(tmp < 0) {
			System.out.println("negative sqrt");
			System.exit(-1);
		}
		size = (-b - Math.sqrt(tmp)) / (2*a);
		numNodesPerLine = (int) Math.round(AreaConfiguration.dimX / size) - 1;
		i=0; j=1;
		TransmissionConfiguration.MAX_RANGE = (int)size + 1;
	}
	
	/* (non-Javadoc)
	 * @see models.DistributionModel#getNextPosition()
	 */
	public Position getNextPosition() {
		i ++;
		if(i > numNodesPerLine) {
			i=1; j++;
		}
		return new Position(i * size, j * size, 0);
	}

}
