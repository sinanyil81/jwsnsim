package sim.topology;

import nodes.Position;

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
	
	/*
	 * 	public static Position getNextLinePosition(){
		Position position =  new Position(counter*10,0,0);
		counter++;
		return position;
	}
	
	public static Position getNext4x4GridPosition(){
		double x = ((counter-1)%4)*14;
		double y = ((counter-1)/4)*20;
		
		Position position =  new Position(x,y,0);
		counter++;
		return position;
	}
	
	public static Position getNextRingPosition(int numNodes){
		double oneStep = 360.0 / numNodes;		
    	double radius = 15.0*360.0/(oneStep*2.0*Math.PI);

		
		Position position = new Position(radius * Math.cos(Math.toRadians(counter * oneStep)),
								radius * Math.sin(Math.toRadians(counter * oneStep)),0);
		counter++;
		return position;
	}
	
	public static Position getNextDensePosition(int density){
		double stepsize = 2.0*15.0/(double)density;
		
		Position position = new Position(counter*stepsize,counter*stepsize,0);
		counter++;
		return position;
	}

	 */
}
