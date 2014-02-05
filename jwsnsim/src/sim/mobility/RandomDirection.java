package sim.mobility;

import java.util.Random;

import sim.node.Node;
import sim.node.Position;
import sim.statistics.Distribution;

public class RandomDirection {
	// we assume that these distributions are the same for all nodes
	private static Distribution speedDistribution; // how fast the nodes move
	private static Distribution waitingTimeDistribution; // how long nodes wait before starting the next mobility phase
	private static Distribution moveTimeDistribution; // for how long the node moves when it moves
	
	protected static boolean initialized = false; // a flag set to true after initialization of the static vars of this class has been done.
	protected static Random random = Distribution.getRandom(); // a random generator of the framework 
	
	private Position moveVector; // The vector that is added in each step to the current position of this node
	protected Position currentPosition = null; // the current position, to detect if the node has been moved by other means than this mobility model between successive calls to getNextPos()
	private int remaining_hops = 0; // the remaining hops until a new path has to be determined
	private int remaining_waitingTime = 0;
	
	private boolean initialize = true; // to detect very first time to start smoothly

	/**
	 * Initializes the next move by determining the next point to move to.
	 * This new destination may be outside the deployment area. This method also
	 * determines the vector along which the nodes move in each step and the number
	 * of rounds that the move will take.
	 * 
	 * @param node The node for which the new destination is determined.
	 * @param moveSpeed The speed at which the node will move.
	 * @parm moveTime The time during which the node is supposed to move
	 */
	private void initializeNextMove(Node node, double moveSpeed, double moveTime) {
		double angleXY = 2 * Math.PI * random.nextDouble(); // 0 .. 360
		double angleZ = Math.PI * (0.5 - random.nextDouble()); // -90 .. 90
		if(MobilityConfiguration.numDimentions == 2) {
			angleZ = 0; // remain in the XY-plane
		}
		double distance = moveTime * moveSpeed; // the distance to move
		
		// the relative dislocation
		double dx = distance * Math.cos(angleXY) * Math.cos(angleZ);
		double dy = distance * Math.sin(angleXY) * Math.cos(angleZ);
		double dz = distance * Math.sin(angleZ);

		// determine the number of rounds needed to reach the target
		remaining_hops = (int) Math.ceil(moveTime);
		// determine the moveVector which is added in each round to the position of this node
		moveVector = new Position(dx / moveTime, dy / moveTime, dz / moveTime);
	}
	
	/* (non-Javadoc)
	 * @see mobilityModels.MobilityModelInterface#getNextPos(nodes.Node)
	 */
	public Position getNextPos(Node n) {
		if(initialize) { // called the very first time such that not all nodes start moving in the first round of the simulation.
			// use a sample to determine in which phase we are.
			double wt = Math.abs(waitingTimeDistribution.nextSample());
			double mt = Math.abs(moveTimeDistribution.nextSample());
			double fraction = random.nextDouble() * (wt + mt);
			if(fraction < wt) {
				// the node starts waiting, but depending on fraction, may already have waited some time
				remaining_waitingTime = (int) Math.ceil(wt - fraction); // the remaining rounds to wait
				remaining_hops = 0;
			} else {
				// the node starts moving
				double speed = Math.abs(speedDistribution.nextSample()); // units per round
				initializeNextMove(n, speed, mt + wt - fraction);
			}
			currentPosition = n.getPosition(); // initially, currentPos is null
			initialize = false;
		}
		
		// restart a new move to a new destination if the node was moved by another means than this mobility model
		if(currentPosition != null) {
			if(!currentPosition.equals(n.getPosition())) {
				remaining_waitingTime = 0;
				remaining_hops = 0;
			}
		} else {
			currentPosition = new Position(0, 0, 0);
		}
		
		// execute the waiting loop
		if(remaining_waitingTime > 0) {
			remaining_waitingTime --;
			return n.getPosition();
		}
		// move
		if(remaining_hops == 0) { // we start to move, determine next random target
			// determine the next point to which this node moves to
			double speed = Math.abs(speedDistribution.nextSample()); // units per round
			double time = Math.abs(moveTimeDistribution.nextSample()); // rounds
			initializeNextMove(n, speed, time);
		}
		double newx = n.getPosition().xCoord + moveVector.xCoord; 
		double newy = n.getPosition().yCoord + moveVector.yCoord;
		double newz = n.getPosition().zCoord + moveVector.zCoord;
		
		// test that it is not outside the deployment area, otherwise reflect
		// We need to repeat the test for special cases where the node moves in really long
		// steps and is reflected more than once at the same border.
		boolean reflected = false;
		do {
			reflected = false;
			
			if(newx < 0) {
				newx *= -1;
				moveVector.xCoord *= -1;
				reflected = true;
			}
			if(newy < 0) {
				newy *= -1;
				moveVector.yCoord *= -1;
				reflected = true;
			}
			if(newz < 0) {
				newz *= -1;
				moveVector.zCoord *= -1;
				reflected = true;
			}
			if(newx > MobilityConfiguration.dimX) {
				newx = 2*MobilityConfiguration.dimX - newx;
				moveVector.xCoord *= -1;
				reflected = true;
			}
			if(newy > MobilityConfiguration.dimY) {
				newy = 2*MobilityConfiguration.dimY - newy;
				moveVector.yCoord *= -1;
				reflected = true;
			}
			if(newz > MobilityConfiguration.dimZ) {
				newz = 2*MobilityConfiguration.dimZ - newz;
				moveVector.zCoord *= -1;
				reflected = true;
			}
		} while(reflected);
		
		Position result = new Position(newx, newy, newz);

		if(remaining_hops <= 1) { // was last round of mobility
			// set the next waiting time that executes after this mobility phase
			remaining_waitingTime = (int) Math.ceil(Math.abs(waitingTimeDistribution.nextSample()));
			remaining_hops = 0;
		} else {
			remaining_hops --;
		}

		currentPosition.set(result);
		return result;
	}
	
	/**
	 * The default constructor
	 * @see RandomWayPoint
	 * @throws CorruptConfigurationEntryException
	 */
	public RandomDirection() {
		if(!initialized) {
			moveTimeDistribution = Distribution.getDistributionFromConfigFile("RandomDirection/MoveTime");
			speedDistribution = Distribution.getDistributionFromConfigFile("RandomDirection/NodeSpeed");
			waitingTimeDistribution = Distribution.getDistributionFromConfigFile("RandomDirection/WaitingTime");
			initialized = true;
		}
	}

}
