package sim.topology;

import sim.node.Position;

public abstract class Topology {
	
	public static final int LINE2D = 0;
	public static final int RING2D = 1;
	public static final int GRID2D = 2;
	public static final int RANDOM2D = 3;
	
	/**
	 * Method that gets galled after the instance has been created and the number of nodes has been set. It
	 * is used to set all the parameters that need to know the number of nodes that ar created.
	 */
	public void initialize(){
		// empty body - overwrite in the subclass, if needed.
	}
		
	/**
	 * Returns the next position where a node is placed.
	 * <p>
	 * You may precalculate all positions and store them in a datastructure. Then, return one after the other
	 * of these positions when this method is called.  
	 * @return The next position where a node is placed.
	 */
	public abstract Position getNextPosition(); 
}
