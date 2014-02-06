package sim.mobility;

import java.util.Random;

import sim.configuration.AreaConfiguration;
import sim.configuration.MobilityConfiguration;
import sim.node.Node;
import sim.node.Position;
import sim.statistics.Distribution;
import sim.statistics.GaussianDistribution;
import sim.statistics.PoissonDistribution;

public class RandomWayPoint extends MobilityModel{
	// we assume that these distributions are the same for all nodes
	protected static Distribution speedDistribution;
	protected static Distribution waitingTimeDistribution;

	private static boolean initialized = false; // a flag set to true after initialization of the static vars of this class has been done.
	protected static Random random = Distribution.getRandom(); // a random generator of the framework 
	
	protected Position nextDestination = new Position(); // The point where this node is moving to
	protected Position moveVector = new Position(); // The vector that is added in each step to the current position of this node
	protected Position currentPosition = null; // the current position, to detect if the node has been moved by other means than this mobility model between successive calls to getNextPos()
	protected int remaining_hops = 0; // the remaining hops until a new path has to be determined
	protected int remaining_waitingTime = 0;
	
	/* (non-Javadoc)
	 * @see mobilityModels.MobilityModelInterface#getNextPos(nodes.Node)
	 */
	public Position getNextPos(Node n) {
		// restart a new move to a new destination if the node was moved by another means than this mobility model
		if(currentPosition != null) {
			if(!currentPosition.equals(n.getPosition())) {
				remaining_waitingTime = 0;
				remaining_hops = 0;
			}
		} else {
			currentPosition = new Position(0, 0, 0);
		}
		
		Position nextPosition = new Position();
		
		// execute the waiting loop
		if(remaining_waitingTime > 0) {
			remaining_waitingTime --;
			return n.getPosition();
		}

		if(remaining_hops == 0) {
			// determine the speed at which this node moves
			double speed = Math.abs(speedDistribution.nextSample()); // units per round

			// determine the next point where this node moves to
			nextDestination = getNextWayPoint();
			
			// determine the number of rounds needed to reach the target
			double dist = nextDestination.distanceTo(n.getPosition());
			double rounds = dist / speed;
			remaining_hops = (int) Math.ceil(rounds);
			// determine the moveVector which is added in each round to the position of this node
			double dx = nextDestination.xCoord - n.getPosition().xCoord;
			double dy = nextDestination.yCoord - n.getPosition().yCoord;
			double dz = nextDestination.zCoord - n.getPosition().zCoord;
			moveVector.xCoord = dx / rounds;
			moveVector.yCoord = dy / rounds;
			moveVector.zCoord = dz / rounds;
		}
		if(remaining_hops <= 1) { // don't add the moveVector, as this may move over the destination.
			nextPosition.xCoord = nextDestination.xCoord;
			nextPosition.yCoord = nextDestination.yCoord;
			nextPosition.zCoord = nextDestination.zCoord;
			// set the next waiting time that executes after this mobility phase
			remaining_waitingTime = (int) Math.ceil(waitingTimeDistribution.nextSample());
			remaining_hops = 0;
		} else {
			double newx = n.getPosition().xCoord + moveVector.xCoord; 
			double newy = n.getPosition().yCoord + moveVector.yCoord; 
			double newz = n.getPosition().zCoord + moveVector.zCoord; 
			nextPosition.xCoord = newx;
			nextPosition.yCoord = newy;
			nextPosition.zCoord = newz;
			remaining_hops --;
		}
		currentPosition.set(nextPosition);
		return nextPosition;
	}
	
	/**
	 * Determines the next waypoint where this node moves after having waited.
	 * The position is expected to be within the deployment area.
	 * @return the next waypoint where this node moves after having waited. 
	 */
	protected Position getNextWayPoint() {
		double randx = random.nextDouble() * AreaConfiguration.dimX;
		double randy = random.nextDouble() * AreaConfiguration.dimY;
		double randz = 0;
		if(AreaConfiguration.numDimentions == 3) {
			randz = random.nextDouble() * AreaConfiguration.dimZ;
		}
		return new Position(randx,randy,randz);
	}
	
	/**
	 * Creates a new random way point object, and reads the speed distribution and 
	 * waiting time distribution configuration from the XML config file.
	 * @throws CorruptConfigurationEntryException When a needed configuration entry is missing.
	 */
	public RandomWayPoint(){
		if(!initialized) {
			speedDistribution = new GaussianDistribution(MobilityConfiguration.speedMean,MobilityConfiguration.speedVariance); 
			waitingTimeDistribution = new PoissonDistribution(MobilityConfiguration.waitingLambda);
			initialized = true;
		}
	}

}
