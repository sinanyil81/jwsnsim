package sim.topology;

import sim.configuration.AreaConfiguration;
import sim.node.Position;
import sim.statistics.Distribution;

public class RandomDeployment extends Topology{
	// The random-number generator
	private java.util.Random rand = Distribution.getRandom();
	
	/* (non-Javadoc)
	 * @see distributionModels.DistributionModelInterface#getOnePosition()
	 */
	public Position getNextPosition() {
		double randomPosX = rand.nextDouble() * AreaConfiguration.dimX;
		double randomPosY = rand.nextDouble() * AreaConfiguration.dimY;
		double randomPosZ = 0;
		if(AreaConfiguration.numDimentions == 3) {
			randomPosZ = rand.nextDouble() * AreaConfiguration.dimZ;
		}
		return new Position((int)randomPosX, (int)randomPosY, (int)randomPosZ);
	}
}
